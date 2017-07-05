
package lda

import java.io.PrintStream

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.mllib.clustering.{DistributedLDAModel, EMLDAOptimizer, LDA, OnlineLDAOptimizer}
import org.apache.spark.mllib.feature.{HashingTF, IDF}
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
    val conf = new SparkConf().setAppName(s"LDAExample with $params").setMaster("local[*]").set("spark.driver.memory", "4g").set("spark.executor.memory", "4g")
    val sc = new SparkContext(conf)

    Logger.getRootLogger.setLevel(Level.WARN)

    val topic_output = new PrintStream("output/Results1.txt")
    // Load documents, and prepare them for LDA.
    val preprocessStart = System.nanoTime()
    val (corpus, vocabArray, actualNumTokens) =
      preprocess(sc, params.input)
    corpus.cache()
    val actualCorpusSize = corpus.count()
    val actualVocabSize = vocabArray.length
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9

    println()
    println(s"Corpus summary:")
    println(s"\t Training set size: $actualCorpusSize documents")
    println(s"\t Vocabulary size: $actualVocabSize terms")
    println(s"\t Training set size: $actualNumTokens tokens")
    println(s"\t Preprocessing time: $preprocessElapsed sec")
    println()


    topic_output.println()
    topic_output.println(s"Corpus summary:")
    topic_output.println(s"\t Training set size: $actualCorpusSize documents")
    topic_output.println(s"\t Vocabulary size: $actualVocabSize terms")
    topic_output.println(s"\t Training set size: $actualNumTokens tokens")
    topic_output.println(s"\t Preprocessing time: $preprocessElapsed sec")
    topic_output.println()

    // Run LDA.
    val lda = new LDA()

    val optimizer = params.algorithm.toLowerCase match {
      case "em" => new EMLDAOptimizer
      // add (1.0 / actualCorpusSize) to MiniBatchFraction be more robust on tiny datasets.
      case "online" => new OnlineLDAOptimizer().setMiniBatchFraction(0.05 + 1.0 / actualCorpusSize)
      case _ => throw new IllegalArgumentException(
        s"Only em, online are supported but got ${params.algorithm}.")
    }

    lda.setOptimizer(optimizer)
      .setK(params.k)
      .setMaxIterations(50)

    val startTime = System.nanoTime()
    val ldaModel = lda.run(corpus)
    val elapsed = (System.nanoTime() - startTime) / 1e9

    println(s"Finished training LDA model.  Summary:")
    println(s"\t Training time: $elapsed sec")


    topic_output.println(s"Finished training LDA model.  Summary:")
    topic_output.println(s"\t Training time: $elapsed sec")

    if (ldaModel.isInstanceOf[DistributedLDAModel]) {
      val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]
      val avgLogLikelihood = distLDAModel.logLikelihood / actualCorpusSize.toDouble
      println(s"\t Training data average log likelihood: $avgLogLikelihood")
      println()
      topic_output.println(s"\t Training data average log likelihood: $avgLogLikelihood")
      topic_output.println()
    }

    // Print the topics, showing the top-weighted terms for each topic.
    val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = actualVocabSize)
    val topics = topicIndices.map { case (terms, termWeights) =>
      terms.zip(termWeights).map { case (term, weight) => (vocabArray(term.toInt), weight) }
    }
    println(s"${params.k} topics:")
    topic_output.println(s"${params.k} topics:")
    topics.zipWithIndex.foreach { case (topic, i) =>
      println(s"TOPIC $i")
      // topic_output.println(s"TOPIC $i")
      topic.foreach { case (term, weight) =>
        println(s"$term\t$weight")
        topic_output.println(s"TOPIC_$i;$term;$weight")
      }
      println()
      topic_output.println()
    }
    topic_output.close()
    sc.stop()
  }


  private def preprocess(sc: SparkContext,paths: Seq[String]): (RDD[(Long, Vector)], Array[String], Long) = {

    val stopWords_rk =sc.textFile("src/data/stopwords").collect()
    val stopWordsBroadCast_rk =sc.broadcast(stopWords_rk)
    val dataset_rk = sc.textFile(paths.mkString(",")).map(f => {
      val lemmatized_rk=CoreNLP.returnLemma(f)
      val strng_rk = lemmatized_rk.split(" ")
      strng_rk
    })
    val stopWordRemovedDF_rk=dataset_rk.map(f=>{
      val filtereddataset=f.map(_.replaceAll("[^a-zA-Z]"," "))
        .filter(ff =>{
          if(stopWordsBroadCast_rk.value.contains(ff.toLowerCase))
            false
          else
            true
        })
      filtereddataset
    })
    val dfseq=stopWordRemovedDF_rk.map(_.toSeq)
    val termCounts: Array[(String, Long)] = dfseq.flatMap(_.map( _ -> 1L)).reduceByKey(_ + _).collect().sortBy(-_._2)
    val numStopwords = 20
    val vocabArray: Array[String] =
      termCounts.takeRight(termCounts.size - numStopwords).map(_._1)
    val vocab: Map[String, Int] = vocabArray.zipWithIndex.toMap

    val documents: RDD[(Long, Vector)] =
      dfseq.zipWithIndex.map { case (tokens, id) =>
        val counts = new mutable.HashMap[Int, Double]()
        tokens.foreach { term =>
          if (vocab.contains(term)) {
            val idx = vocab(term)
            counts(idx) = counts.getOrElse(idx, 0.0) + 1.0
          }
        }
        (id, Vectors.sparse(vocab.size, counts.toSeq))
      }
    ;
    val dff= stopWordRemovedDF_rk.flatMap(f=>f)
    val vocab1=dff.distinct().collect()
    (documents, vocab1, dff.count()) // Vector, Vocab, total token count
  }
}

