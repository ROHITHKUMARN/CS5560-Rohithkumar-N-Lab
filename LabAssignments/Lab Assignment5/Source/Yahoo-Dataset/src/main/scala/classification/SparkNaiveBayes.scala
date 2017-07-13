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

package classification

import java.io.PrintStream

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel, RegexTokenizer, StopWordsRemover}
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import scopt.OptionParser

import scala.collection.immutable.HashMap
import scala.collection.immutable.HashMap

object SparkNaiveBayes {

  private case class Params(
                             input: Seq[String] = Seq.empty
                           )

  def main(args: Array[String]) {
    val defaultParams = Params()

    val parser = new OptionParser[Params]("NBExample") {
      head("NBExample: an example NB app for plain text data.")
      arg[String]("<input>...")
        .text("input paths (directories) to plain text corpora." +
          "  Each text file line should hold 1 document.")
        .unbounded()
        .required()
        .action((x, c) => c.copy(input = c.input :+ x))
    }

    parser.parse(args, defaultParams).map { params =>
      run(params)
    }.getOrElse {
      parser.showUsageAsError
      sys.exit(1)
    }
  }

  private def run(params: Params) {
    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")
    val config_rk = new SparkConf().setAppName(s"NBExample with $params").setMaster("local[*]").set("spark.driver.memory", "4g").set("spark.executor.memory", "4g")
    val scontext_rk = new SparkContext(config_rk)

    val outputfile = new PrintStream("data/NB_Results.txt")
    // Load documents, and prepare them for NB.
    val pStart = System.nanoTime()

    val (inputVector_rk, corpusData_rk, vocabArrayCount_rk) = preprocess(scontext_rk, "data/TrainDataset/Yahoo-Questions-traindata.csv")

    val data_rk = corpusData_rk.zip(inputVector_rk)

    val featureVector = data_rk.map(f => {
      new LabeledPoint(f._1._1.toString.toDouble,f._2)
    })

    val (testinputVector, testcorpusData, testvocabCount) = preprocess(scontext_rk, "data/TestDataSet/Yahoo-Question-testdata.csv")
    val data1= testcorpusData.zip(testinputVector)
    val testfeatureVector = data1.map(f => {
      new LabeledPoint(f._1._1.toDouble, f._2)
    })

    val out_rk = new PrintStream("data/Results_NBs.txt")

    val mel_rk = NaiveBayes.train(featureVector, lambda = 1.0, modelType = "multinomial")
    val term_v = testfeatureVector.map(f=>f.features)
    val abc = mel_rk.predict(term_v)
    val mymap_rk = abc.map(f=>{
      if (f == 1.0) "Business&Finance"
      else if (f == 2.0) "Computers&Internet"
      else if (f == 3.0) "Entertainment&Music"
      else if (f == 4.0) "Family&Relationships"
      else if (f == 5.0) "Education&Reference"
      else if (f == 6.0) "Health"
      else if (f == 7.0) "Science&Mathematics"
      else "Error"
    })
    val result = testcorpusData.zip(mymap_rk)
    val xyz_rk=result.collect()
    xyz_rk.foreach(f=>outputfile.println(f))

    testfeatureVector.collect().foreach(f=>out_rk.println(f))

    val pandl = testfeatureVector.map(p=>(mel_rk.predict(p.features),p.label))

    val accuracy = 1.0 * pandl.filter(x => x._1 == x._2).count() / testfeatureVector.count()

    val metrics = new MulticlassMetrics(pandl)

    outputfile.println("Confusion matrix:")
    outputfile.println(metrics.confusionMatrix)

    outputfile.println("Accuracy: " + accuracy)
    scontext_rk.stop()
  }
  /**
    * Load documents, tokenize them, create vocabulary, and prepare documents as term count vectors.
    *
    * @return (corpus, vocabulary as array, total token count in corpus)
    */
  private def preprocess(sc: SparkContext,paths: String): (RDD[Vector], RDD[(String,String)], Long) = {

    //Reading Stop Words
    val stopWords=sc.textFile("data/stopwords.txt").collect()
    val stopWordsBroadCast=sc.broadcast(stopWords)

    val df = sc.textFile(paths.toString()).map(f => {
      val split1 =f.split(",")
      val lemmatised=CoreNLP.returnLemma(split1(1))
      val splitString = lemmatised.split(" ")
      (split1(0),splitString)
    })


    val stopWordRemovedDF=df.map(f=>{
      //Filtered numeric and special characters out
      val filteredF=f._2.map(_.replaceAll("[^a-zA-Z]",""))
        //Filter out the Stop Words
        .filter(ff=>{
        if(stopWordsBroadCast.value.contains(ff.toLowerCase))
          false
        else
          true
      })
      (f._1,filteredF)
    })

    val data=stopWordRemovedDF.map(f=>{(f._1,f._2.mkString(""))})
    val dfseq=stopWordRemovedDF.map(_._2.toSeq)

    //Creating an object of HashingTF Class
    val hashingTF = new HashingTF(stopWordRemovedDF.count().toInt)  // VectorSize as the Size of the Vocab

    //Creating Term Frequency of the document
    val tf = hashingTF.transform(dfseq)
    tf.cache()

    val idf = new IDF().fit(tf)
    //Creating Inverse Document Frequency
    val tfidf1 = idf.transform(tf)
    tfidf1.cache()

    val dff= stopWordRemovedDF.flatMap(f=>f._2)
    val vocab=dff.distinct().collect()
    (tfidf1, data, dff.count()) // Vector, Data, total token count
  }
}


