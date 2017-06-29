import java.io.{BufferedWriter, File, FileWriter}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.{HashingTF, IDF, Word2Vec, Word2VecModel}

import scala.collection.immutable.HashMap

/**
  * Created by rohithkumar on 6/27/17.
  */
object w2vtfidflemma {
  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")

    val sparkConfig = new SparkConf().setAppName("TFIDFW2V").setMaster("local[*]").
      set("spark.driver.memory", "6g").set("spark.executor.memory", "6g")

    val sparkcontext = new SparkContext(sparkConfig)
    val c1 = new BufferedWriter(new FileWriter("output/w2vtfidflemma.txt"))

    //Reading the Text File
    val documents = sparkcontext.textFile("data/mylab")

    val stopwordsfile = sparkcontext.textFile("data/englishstopwords.txt")

    // Flatten, collect, and broadcast.
    val stopWords = stopwordsfile.flatMap(x => x.split(",")).map(_.trim)

    val broadcastStopWords = sparkcontext.broadcast(stopWords.collect.toSet)

    //Getting the Lemmatised form of the words in TextFile
    val docseq = documents.map(f => {
       val lemmatized = CoreNLP.returnLemma(f)
       val slist= lemmatized.split(" ").filter(!broadcastStopWords.value.contains(_)).filter( w => !w.contains(","))
       slist.toSeq
    })

    docseq.foreach(f=>println(f.mkString("")))
    //Creating an object of HashingTF Class
    val TF = new HashingTF()

    //Creating Term Frequency of the document
    val tf = TF.transform(docseq)
    tf.cache()

    val IDF = new IDF().fit(tf)

    //Creating Inverse Document Frequency
    val tfidf = IDF.transform(tf)

    val values = tfidf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val values = ff(2).replace("]", "").replace(")", "").split(",")
      values
    })

    val index = tfidf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val indices = ff(1).replace("]", "").replace(")", "").split(",")
      indices
    })

    tfidf.foreach(f => println(f))

    val tfidfData = index.zip(values)

    var hmap = new HashMap[String, Double]

    tfidfData.collect().foreach(f => {
      hmap+= f._1 -> f._2.toDouble
    })

    val map = sparkcontext.broadcast(hmap)

    val documentData = docseq.flatMap(_.toList)
    val ddata = documentData.map(f => {
      val i = TF.indexOf(f)
      val h = map.value
      (f, h(i.toString))
    })

    val dd1 = ddata.distinct().sortBy(_._2, false)
    val x= dd1.take(5)
      x.foreach(f => {
        c1.write(f._1 + " " +f._2)
        c1.write("\n")
        c1.flush()
    })

    val input = sparkcontext.textFile("data/mylab").map(line => CoreNLP.returnLemma(line).split(" ").toSeq)

    val modelFolder = new File("sym1")

    if (modelFolder.exists()) {
      val sameModel = Word2VecModel.load(sparkcontext, "synonyms")
      dd1.foreach(f => {
        val synonyms = sameModel.findSynonyms(f._1, 2)
        println("Synonyms for lemma: " + f._1 )
        for ((synonym, cosineSimilarity) <- synonyms) {
          println(s"$synonym $cosineSimilarity")
        } })
    }
    else {
      val w2v = new Word2Vec().setVectorSize(1000).setMinCount(1)
      val model = w2v.fit(input)
      dd1.foreach(f => {
        // println(f)
        val synonyms = model.findSynonyms(f._1, 2)
        println("Synonyms for lemma : " + f._1 )
        for ((synonym, cosineSimilarity) <- synonyms) {
          println(s"$synonym $cosineSimilarity")
        }
        model.getVectors.foreach(f => println(f._1 + ":" + f._2.length))
        // Save and load model
        model.save(sparkcontext, "sym1")
      })
    }
  }
}
