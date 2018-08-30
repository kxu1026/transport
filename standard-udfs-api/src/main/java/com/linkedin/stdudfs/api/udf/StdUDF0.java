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
package com.linkedin.stdudfs.api.udf;

import com.linkedin.stdudfs.api.data.StdData;


/**
 * A Standard UDF with zero input arguments.
 *
 * @param <O>  the type of the return value of the {@link StdUDF}
 */
public abstract class StdUDF0<O extends StdData> extends StdUDF {

  /**
   * Returns the output of the {@link StdUDF} given the input arguments.
   *
   * This method is called once per input record. All UDF logic should be defined in this method.
   *
   * @return the output of the {@link StdUDF} given the input arguments.
   */
  public abstract O eval();

  /**
   * Returns an array of file paths to be localized at the worker nodes.
   *
   * The Standard UDF framework localizes the files passed through this method and provides the localized file paths to
   * {@link StdUDF#processRequiredFiles(String[])} for further processing. Users can use the pattern "#LATEST" instead
   * of a concrete directory name in the path as a way of selecting the directory with the most recent timestamp, and
   * hence obtaining the most recent version of a file.
   * Example: 'hdfs:///data/derived/dwh/prop/testMemberId/#LATEST/testMemberId.txt'
   *
   * The arguments passed to {@link #eval()} are passed to this method as well to allow users to construct
   * required file paths from arguments passed to the UDF. Since this method is called before any rows are processed,
   * only constant UDF arguments should be used to construct the file paths. Values of non-constant arguments are not
   * deterministic, and are null for most platforms. (Constant arguments are arguments whose literal values are given
   * to the UDF as opposed to non-constant arguments that are expressions which depend on columns. For example, in the
   * query {@code SELECT my_udf('my_value', T.Col1) FROM T}, {@literal my_value} is a constant argument to the UDF and
   * its value is the same for all invocations of this UDF, while {@code T.Col1} is a non-constant argument since it is
   * an expression that depends on a table column, and hence its value changes on a per-row basis).
   *
   * @return an array of file paths to be localized at the worker nodes.
   */
  public String[] getRequiredFiles() {
    return new String[]{};
  }

  protected final int numberOfArguments() {
    return 0;
  }
}
