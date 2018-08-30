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
package com.linkedin.stdudfs.hive;

import com.linkedin.stdudfs.api.StdFactory;
import com.linkedin.stdudfs.api.data.StdData;
import com.linkedin.stdudfs.api.types.StdType;
import com.linkedin.stdudfs.hive.data.HiveArray;
import com.linkedin.stdudfs.hive.data.HiveBoolean;
import com.linkedin.stdudfs.hive.data.HiveInteger;
import com.linkedin.stdudfs.hive.data.HiveLong;
import com.linkedin.stdudfs.hive.data.HiveMap;
import com.linkedin.stdudfs.hive.data.HiveString;
import com.linkedin.stdudfs.hive.data.HiveStruct;
import com.linkedin.stdudfs.hive.types.HiveArrayType;
import com.linkedin.stdudfs.hive.types.HiveBooleanType;
import com.linkedin.stdudfs.hive.types.HiveIntegerType;
import com.linkedin.stdudfs.hive.types.HiveLongType;
import com.linkedin.stdudfs.hive.types.HiveMapType;
import com.linkedin.stdudfs.hive.types.HiveStringType;
import com.linkedin.stdudfs.hive.types.HiveStructType;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BooleanObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.LongObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.VoidObjectInspector;


public final class HiveWrapper {

  private HiveWrapper() {
  }

  public static StdData createStdData(Object hiveData, ObjectInspector hiveObjectInspector, StdFactory stdFactory) {
    if (hiveObjectInspector instanceof IntObjectInspector) {
      return new HiveInteger(hiveData, (IntObjectInspector) hiveObjectInspector, stdFactory);
    } else if (hiveObjectInspector instanceof LongObjectInspector) {
      return new HiveLong(hiveData, (LongObjectInspector) hiveObjectInspector, stdFactory);
    } else if (hiveObjectInspector instanceof BooleanObjectInspector) {
      return new HiveBoolean(hiveData, (BooleanObjectInspector) hiveObjectInspector, stdFactory);
    } else if (hiveObjectInspector instanceof StringObjectInspector) {
      return new HiveString(hiveData, (StringObjectInspector) hiveObjectInspector, stdFactory);
    } else if (hiveObjectInspector instanceof ListObjectInspector) {
      ListObjectInspector listObjectInspector = (ListObjectInspector) hiveObjectInspector;
      return new HiveArray(hiveData, listObjectInspector, stdFactory);
    } else if (hiveObjectInspector instanceof MapObjectInspector) {
      return new HiveMap(hiveData, hiveObjectInspector, stdFactory);
    } else if (hiveObjectInspector instanceof StructObjectInspector) {
      return new HiveStruct(((StructObjectInspector) hiveObjectInspector).getStructFieldsDataAsList(hiveData).toArray(),
          hiveObjectInspector, stdFactory);
    } else if (hiveObjectInspector instanceof VoidObjectInspector) {
      return null;
    }
    assert false : "Unrecognized Hive ObjectInspector: " + hiveObjectInspector.getClass();
    return null;
  }

  public static StdType createStdType(ObjectInspector hiveObjectInspector) {
    if (hiveObjectInspector instanceof IntObjectInspector) {
      return new HiveIntegerType((IntObjectInspector) hiveObjectInspector);
    } else if (hiveObjectInspector instanceof LongObjectInspector) {
      return new HiveLongType((LongObjectInspector) hiveObjectInspector);
    } else if (hiveObjectInspector instanceof BooleanObjectInspector) {
      return new HiveBooleanType((BooleanObjectInspector) hiveObjectInspector);
    } else if (hiveObjectInspector instanceof StringObjectInspector) {
      return new HiveStringType((StringObjectInspector) hiveObjectInspector);
    } else if (hiveObjectInspector instanceof ListObjectInspector) {
      return new HiveArrayType((ListObjectInspector) hiveObjectInspector);
    } else if (hiveObjectInspector instanceof MapObjectInspector) {
      return new HiveMapType((MapObjectInspector) hiveObjectInspector);
    } else if (hiveObjectInspector instanceof StructObjectInspector) {
      return new HiveStructType((StructObjectInspector) hiveObjectInspector);
    } else if (hiveObjectInspector instanceof VoidObjectInspector) {
      return null;
    }
    assert false : "Unrecognized Hive ObjectInspector: " + hiveObjectInspector.getClass();
    return null;
  }
}
