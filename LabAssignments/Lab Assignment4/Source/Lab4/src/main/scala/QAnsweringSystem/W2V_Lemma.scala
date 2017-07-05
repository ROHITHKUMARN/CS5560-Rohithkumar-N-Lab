package QAnsweringSystem

import java.io.File
import QAnsweringSystem.Lemmatization
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.{HashingTF, IDF, Word2Vec, Word2VecModel}

import scala.collection.immutable.HashMap

object W2V_Lemma{
  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")

    val sparkConf = new SparkConf().setAppName("Task2ab").setMaster("local[*]").
      set("spark.driver.memory", "6g").set("spark.executor.memory", "6g")

    val sc = new SparkContext(sparkConf)

    //Reading the Text File
    val documents = sc.textFile("src/data/sample")
    //Reading stop words
    val stopWordsInput = sc.textFile("src/data/stopwords")
    // Flatten, collect, and broadcast.
    val stopWords = stopWordsInput.flatMap(x => x.split(",")).map(_.trim)
    val broadcastStopWords = sc.broadcast(stopWords.collect.toSet)

    //Getting the Lemmatised form of the words in TextFile
    val documentseq = documents.map(f => {
      val lemmati = Lemmatization.returnLemma(f)
      val sString = lemmati.split(" ").filter(!broadcastStopWords.value.contains(_)).filter( w => !w.contains(","))
      // val sStrng = f.split(" ").filter(!broadcastStopWords.value.contains(_)).filter( w => !w.contains(","))
      sString.toSeq
    })

    documentseq.foreach(f=>println(f.mkString("")))
    //Creating an object of HashingTF Class
    val hashingTF = new HashingTF()

    //Creating Term Frequency of the document
    val tf = hashingTF.transform(documentseq)
    tf.cache()


    val idf = new IDF().fit(tf)

    //Creating Inverse Document Frequency
    val tfidf = idf.transform(tf)

    val tfidfvalues = tfidf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val values = ff(2).replace("]", "").replace(")", "").split(",")
      values
    })

    val tfidfindex = tfidf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val indices = ff(1).replace("]", "").replace(")", "").split(",")
      indices
    })

    tfidf.foreach(f => println(f))

    val tfidfData = tfidfindex.zip(tfidfvalues)

    var hm = new HashMap[String, Double]

    tfidfData.collect().foreach(f => {
      hm += f._1 -> f._2.toDouble
    })

    val mapp = sc.broadcast(hm)

    val documentData = documentseq.flatMap(_.toList)
    val dd = documentData.map(f => {
      val i = hashingTF.indexOf(f)
      val h = mapp.value
      (f, h(i.toString))
    })

    val dd1 = dd.distinct().sortBy(_._2, false)
    dd1.take(4).foreach(f => {
      println(f)
    })


    //W2v
    val input = sc.textFile("src/data/sample").map(line => Lemmatization.returnLemma(line).split(" ").toSeq)

    val modelFolder = new File("cric_synonms1")

    if (modelFolder.exists()) {
      val sameModel = Word2VecModel.load(sc, "synonms1")

      dd1.take(4).foreach(f => {
        //println(f)
        val synonyms = sameModel.findSynonyms(f._1, 2)
        println("Synonyms for : " + f._1 )
        for ((synonym, cosineSimilarity) <- synonyms) {
          println(s"$synonym $cosineSimilarity")
        } })
    }
    else {
      val word2vec = new Word2Vec().setVectorSize(1000).setMinCount(1)
      val model = word2vec.fit(input)
      dd1.take(4).foreach(f => {
        println(f)
        val synonyms = model.findSynonyms(f._1, 2)
        println("Synonyms for : " + f._1 )
        for ((synonym, cosineSimilarity) <- synonyms) {
          println(s"$synonym $cosineSimilarity")
        }
        model.getVectors.foreach(f => println(f._1 + ":" + f._2.length))

        // Save and load model
        //  model.save(sc, "synonyms")

      })
    }
    println(modelFolder)

  }
}