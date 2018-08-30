/**
 * BSD 2-CLAUSE LICENSE
 *
 * Copyright 2018 LinkedIn Corporation.
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.linkedin.stdudfs.presto.data;

import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.block.PageBuilderStatus;
import com.facebook.presto.spi.function.OperatorType;
import com.facebook.presto.spi.type.BooleanType;
import com.facebook.presto.spi.type.MapType;
import com.facebook.presto.spi.type.Type;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.linkedin.stdudfs.api.StdFactory;
import com.linkedin.stdudfs.api.data.PlatformData;
import com.linkedin.stdudfs.api.data.StdData;
import com.linkedin.stdudfs.api.data.StdMap;
import com.linkedin.stdudfs.presto.PrestoFactory;
import com.linkedin.stdudfs.presto.PrestoWrapper;
import java.lang.invoke.MethodHandle;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static com.facebook.presto.metadata.Signature.internalOperator;
import static com.facebook.presto.spi.StandardErrorCode.GENERIC_INTERNAL_ERROR;
import static com.facebook.presto.spi.type.TypeUtils.readNativeValue;


public class PrestoMap extends PrestoData implements StdMap {

  final Type _keyType;
  final Type _valueType;
  final Type _mapType;
  final MethodHandle _keyEqualsMethod;
  final StdFactory _stdFactory;
  Block _block;

  public PrestoMap(Type mapType, StdFactory stdFactory) {
    BlockBuilder mutable = mapType.createBlockBuilder(new PageBuilderStatus().createBlockBuilderStatus(), 1);
    mutable.beginBlockEntry();
    mutable.closeEntry();
    _block = ((MapType) mapType).getObject(mutable.build(), 0);

    _keyType = ((MapType) mapType).getKeyType();
    _valueType = ((MapType) mapType).getValueType();
    _mapType = mapType;

    _stdFactory = stdFactory;
    _keyEqualsMethod = ((PrestoFactory) stdFactory).getFunctionRegistry()
        .getScalarFunctionImplementation(
            internalOperator(OperatorType.EQUAL, BooleanType.BOOLEAN, ImmutableList.of(_keyType, _keyType)))
        .getMethodHandle();
  }

  public PrestoMap(Block block, Type mapType, StdFactory stdFactory) {
    this(mapType, stdFactory);
    _block = block;
  }

  @Override
  public int size() {
    return _block.getPositionCount() / 2;
  }

  @Override
  public StdData get(StdData key) {
    Object prestoKey = ((PlatformData) key).getUnderlyingData();
    int i = seekKey(prestoKey);
    if (i != -1) {
      Object value = readNativeValue(_valueType, _block, i);
      StdData stdValue = PrestoWrapper.createStdData(value, _valueType, _stdFactory);
      return stdValue;
    } else {
      return null;
    }
  }

  // TODO: Do not copy the _mutable BlockBuilder on every update. As long as updates are append-only or for fixed-size
  // types, we can skip copying.
  @Override
  public void put(StdData key, StdData value) {
    BlockBuilder mutable = _mapType.createBlockBuilder(new PageBuilderStatus().createBlockBuilderStatus(), 1);
    BlockBuilder entryBuilder = mutable.beginBlockEntry();
    Object prestoKey = ((PlatformData) key).getUnderlyingData();
    int valuePosition = seekKey(prestoKey);
    for (int i = 0; i < _block.getPositionCount(); i += 2) {
      // Write the current key to the map
      _keyType.appendTo(_block, i, entryBuilder);
      // Find out if we need to change the corresponding value
      if (i == valuePosition - 1) {
        // Use the user-supplied value
        ((PrestoData) value).writeToBlock(entryBuilder);
      } else {
        // Use the existing value in original _block
        _valueType.appendTo(_block, i + 1, entryBuilder);
      }
    }
    if (valuePosition == -1) {
      ((PrestoData) key).writeToBlock(entryBuilder);
      ((PrestoData) value).writeToBlock(entryBuilder);
    }

    mutable.closeEntry();
    _block = ((MapType) _mapType).getObject(mutable.build(), 0);
  }

  public Set<StdData> keySet() {
    return new AbstractSet<StdData>() {
      @Override
      public Iterator<StdData> iterator() {
        return new Iterator<StdData>() {
          int i = -2;

          @Override
          public boolean hasNext() {
            return !(i + 2 == size() * 2);
          }

          @Override
          public StdData next() {
            i += 2;
            return PrestoWrapper.createStdData(readNativeValue(_keyType, _block, i), _keyType, _stdFactory);
          }
        };
      }

      @Override
      public int size() {
        return PrestoMap.this.size();
      }
    };
  }

  @Override
  public Collection<StdData> values() {
    return new AbstractCollection<StdData>() {

      @Override
      public Iterator<StdData> iterator() {
        return new Iterator<StdData>() {
          int i = -2;

          @Override
          public boolean hasNext() {
            return !(i + 2 == size() * 2);
          }

          @Override
          public StdData next() {
            i += 2;
            return PrestoWrapper.createStdData(readNativeValue(_keyType, _block, i + 1), _keyType, _stdFactory);
          }
        };
      }

      @Override
      public int size() {
        return PrestoMap.this.size();
      }
    };
  }

  @Override
  public boolean containsKey(StdData key) {
    return get(key) != null;
  }

  @Override
  public Object getUnderlyingData() {
    return _block;
  }

  @Override
  public void setUnderlyingData(Object value) {
    _block = (Block) value;
  }

  private int seekKey(Object key) {
    for (int i = 0; i < _block.getPositionCount(); i += 2) {
      try {
        if ((boolean) _keyEqualsMethod.invoke(readNativeValue(_keyType, _block, i), key)) {
          return i + 1;
        }
      } catch (Throwable t) {
        Throwables.propagateIfInstanceOf(t, Error.class);
        Throwables.propagateIfInstanceOf(t, PrestoException.class);
        throw new PrestoException(GENERIC_INTERNAL_ERROR, t);
      }
    }
    return -1;
  }

  @Override
  public void writeToBlock(BlockBuilder blockBuilder) {
    _mapType.writeObject(blockBuilder, _block);
  }
}
