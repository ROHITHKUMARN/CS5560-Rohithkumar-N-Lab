package lda

import java.io.PrintStream

import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.clustering.{DistributedLDAModel, EMLDAOptimizer, LDA, OnlineLDAOptimizer}
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scopt.OptionParser

/**
  * An example Latent Dirichlet Allocation (LDA) app. Run with
  * {{{
  * ./bin/run-example mllib.LDAExample [options] <input>
  * }}}
  * If you use it as a template to create your own app, please use `spark-submit` to submit your app.
  */
object SparkLDAMain {

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

    val topic_output = new PrintStream("output/Results.txt")
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

    val stopWordsdocument=sc.textFile("src/data/stopwords").collect()
    val stopWordsBroadCast=sc.broadcast(stopWordsdocument)

    val data = sc.textFile(paths.mkString(",")).map(f => {
      val lemma_rk=CoreNLP.returnLemma(f)
      val string = lemma_rk.split(" ")
      string
    })

    val stopWordRemovedDF=data.map(f=>{
      //Filtered numeric and special characters out
      val filtereddata=f.map(_.replaceAll("[^a-zA-Z]"," "))
        //Filter out the Stop Words
        .filter(ff=>{
        if(stopWordsBroadCast.value.contains(ff.toLowerCase))
          false
        else
          true
      })
      filtereddata
    })

    val dfseq=stopWordRemovedDF.map(_.toSeq)

    val hTF_rk = new HashingTF(stopWordRemovedDF.count().toInt)  // VectorSize as the Size of the Vocab

    val termfrequency_rk = hTF_rk.transform(dfseq)
    termfrequency_rk.cache()

    val inversedf_rk = new IDF().fit(termfrequency_rk)

    val tfidf = inversedf_rk.transform(termfrequency_rk).zipWithIndex().map(_.swap)

    val dataframe_rk= stopWordRemovedDF.flatMap(f=>f)

    val vocab=dataframe_rk.distinct().collect()
    (tfidf, vocab, dataframe_rk.count())
  }
}
