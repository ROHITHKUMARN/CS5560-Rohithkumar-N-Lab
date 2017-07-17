
package kMeans

import java.io.PrintStream

import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.clustering.{DistributedLDAModel, EMLDAOptimizer, LDA, OnlineLDAOptimizer}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scopt.OptionParser

import scala.collection.mutable

object DATANLPSPLDA {

  private case class Params(
                             input: Seq[String] = Seq.empty,
                             k: Int = 20,
                             algorithm: String = "em")

  def main(args: Array[String]) {
    val defaultParams = Params()

    val parser = new OptionParser[Params]("LDAExample") {
      head("LDAExample: an example LDA app for plain text data.")
      opt[Int]("k")
        .text(s"number of topics. default: ${defaultParams.k}")
        .action((x, c) => c.copy(k = x))
      opt[String]("algorithm")
        .text(s"inference algorithm to use. em and online are supported." +
          s" default: ${defaultParams.algorithm}")
        .action((x, c) => c.copy(algorithm = x))
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
    val config_rk = new SparkConf().setAppName(s"LDAExample with $params").setMaster("local[*]").set("spark.driver.memory", "4g").set("spark.executor.memory", "4g")
    val sc_rk = new SparkContext(config_rk)
    val output_rk = new PrintStream("data/LDA_Results.txt")
    // Load documents, and prepare them for LDA.

    val preprocessStart_rk = System.nanoTime()

    val (corpus_rk, vocabArray_rk, actualNumTokens_rk) = preprocess(sc_rk, params.input)
    corpus_rk.cache()

    val actualCorpusSize_rk = corpus_rk.count()
    val actualVocabSize_rk = vocabArray_rk.count().toInt
    val preprocessElapsed_rk = (System.nanoTime() - preprocessStart_rk) / 1e9

    output_rk.println()
    output_rk.println(s"Corpus summary:")
    output_rk.println(s"\t Training set size: $actualCorpusSize_rk documents")
    output_rk.println(s"\t Vocabulary size: $actualVocabSize_rk terms")
    output_rk.println(s"\t Training set size: $actualNumTokens_rk tokens")
    output_rk.println(s"\t Preprocessing time: $preprocessElapsed_rk sec")
    output_rk.println()

    // Run LDA.
    val lda_rk = new LDA()

    val optimizer_rk = params.algorithm.toLowerCase match {
      case "em" => new EMLDAOptimizer
      // add (1.0 / actualCorpusSize) to MiniBatchFraction be more robust on tiny datasets.
      case "online" => new OnlineLDAOptimizer().setMiniBatchFraction(0.05 + 1.0 / actualCorpusSize_rk)
      case _ => throw new IllegalArgumentException(
        s"Only em, online are supported but got ${params.algorithm}.")
    }

    lda_rk.setOptimizer(optimizer_rk)
      .setK(params.k)
      .setMaxIterations(50)

    val startTime_rk = System.nanoTime()
    val ldaModel_rk = lda_rk.run(corpus_rk)
    val elapsed_rk = (System.nanoTime() - startTime_rk) / 1e9

    output_rk.println(s"Finished training LDA model.  Summary:")
    output_rk.println(s"\t Training time: $elapsed_rk sec")

    if (ldaModel_rk.isInstanceOf[DistributedLDAModel]) {
      val distLDAModel_rk = ldaModel_rk.asInstanceOf[DistributedLDAModel]
      val avgLogLikelihood_rk = distLDAModel_rk.logLikelihood / actualCorpusSize_rk.toDouble
      output_rk.println(s"\t Training data average log likelihood: $avgLogLikelihood_rk")
      output_rk.println()
    }

    // Print the topics, showing the top-weighted terms for each topic.
    val topicIndices_rk = ldaModel_rk.describeTopics(maxTermsPerTopic = actualVocabSize_rk)
    val topics = topicIndices_rk.map { case (t_rk, w) =>
      t_rk.zip(w).map { case (t_rk, w) => (vocabArray_rk.collect(), w) }
    }
    println(s"${params.k} topics:")
    output_rk.println(s"${params.k} topics:")
    topics.zipWithIndex.foreach { case (topic, i) =>
      println(s"TOPIC $i")
      // topic_output.println(s"TOPIC $i")
      topic.foreach { case (term, weight) =>
        println(s"${term.mkString(" ")}\t$weight")
        output_rk.println(s"TOPIC_$i;${term.mkString(" ")};$weight")
      }
      output_rk.println()
    }
    output_rk.close()
    sc_rk.stop()
  }


  private def preprocess(sc: SparkContext,paths: Seq[String]): (RDD[(Long, Vector)], RDD[String], Long) = {

    val sWords_rk =sc.textFile("data/stopwords.txt").collect()
    val sBroadCast_rk =sc.broadcast(sWords_rk)
    val dataset_rk = sc.textFile(paths.mkString(",")).map(f => {
      val lemmatized_rk=Classification.Lemmatisation.returnLemma(f)
      val strng_rk = lemmatized_rk.split(" ")
      strng_rk
    })
    val stopRemovedDF_rk=dataset_rk.map(f=>{
      val filtereddataset=f.map(_.replaceAll("[^a-zA-Z]"," "))
        .filter(ff =>{
          if(sBroadCast_rk.value.contains(ff.toLowerCase))
            false
          else
            true
        })
      filtereddataset
    })
    val dfseq_rk=stopRemovedDF_rk.map(_.toSeq)
    val tc: Array[(String, Long)] = dfseq_rk.flatMap(_.map( _ -> 1L)).reduceByKey(_ + _).collect().sortBy(-_._2)
    val numStopwords_rk = 20
    val vArray: Array[String] =
      tc.takeRight(tc.size - numStopwords_rk).map(_._1)
    val vb: Map[String, Int] = vArray.zipWithIndex.toMap

    val dc: RDD[(Long, Vector)] =
      dfseq_rk.zipWithIndex.map { case (tk, i) =>
        val cs_rk = new mutable.HashMap[Int, Double]()
        tk.foreach { tm =>
          if (vb.contains(tm)) {
            val idx = vb(tm)
            cs_rk(idx) = cs_rk.getOrElse(idx, 0.0) + 1.0
          }
        }
        (i, Vectors.sparse(vb.size, cs_rk.toSeq))
      }
    ;
    val df_rl= stopRemovedDF_rk.flatMap(f=>f)
    val v_rk=stopRemovedDF_rk.map(f=>f.mkString(" "))
    //val vocab1=dff.distinct().collect()
    (dc,v_rk, df_rl.count()) // Vector, Vocab, total token count
  }
}

