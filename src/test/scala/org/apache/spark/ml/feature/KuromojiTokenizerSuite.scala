/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.ml.feature

import java.io.File

import org.atilika.kuromoji.{Tokenizer => KTokenizer}

import org.apache.spark.SparkFunSuite
import org.apache.spark.mllib.util.MLlibTestSparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

class KuromojiTokenizerSuite extends SparkFunSuite with MLlibTestSparkContext {

  test("transform") {

    val data = Seq(
      Row("天皇は、日本国の象徴であり日本国民統合の象徴であつて、この地位は、主権の存する日本国民の総意に基く。"),
      Row("皇位は、世襲のものであつて、国会の議決した皇室典範 の定めるところにより、これを継承する。"),
      Row("天皇の国事に関するすべての行為には、内閣の助言と承認を必要とし、内閣が、その責任を負ふ。"),
      Row("天皇は、この憲法の定める国事に関する行為のみを行ひ、国政に関する権能を有しない。"),
      Row("天皇は、法律の定めるところにより、その国事に関する行為を委任することができる。")
    )
    val schema = StructType(Seq(StructField("text", StringType, false)))
    val df = sqlContext.createDataFrame(sc.parallelize(data), schema)

    val kuromoji = new KuromojiTokenizer()
      .setInputCol("text")
      .setOutputCol("tokens")
      .setMode("EXTENDED")
    assert(kuromoji.getMode === "EXTENDED")

    val transformed = kuromoji.transform(df)
    val tokens = transformed.select("tokens").collect().head.getSeq(0).toSeq
    assert(tokens.size === 32)
    val tokens2 = transformed.select("tokens").collect().apply(1).getSeq(0).toSeq
    assert(tokens2.size === 28)
    val tokens3 = transformed.select("tokens").collect().apply(2).getSeq(0).toSeq
    assert(tokens3.size === 29)
    val tokens4 = transformed.select("tokens").collect().apply(3).getSeq(0).toSeq
    assert(tokens4.size === 23)
  }

  test("save/load") {
    val kuromoji = new KuromojiTokenizer()
      .setInputCol("text")
      .setOutputCol("tokens")
      .setMode("EXTENDED")
    val path = File.createTempFile("spark-kuromoji-tokenizer", "").getAbsolutePath
    kuromoji.write.overwrite().save(path)
    val loadedModel = KuromojiTokenizer.load(path)
    assert(loadedModel.getMode === "EXTENDED")
  }
}