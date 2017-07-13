package QAnsweringSystem

import java.io.{BufferedWriter, FileWriter}

import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.HashMap

/**
  * Created by rohithkumar on 6/27/17.
  */
object tfidflemma {
  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")

    val sparkConf = new SparkConf().setAppName("TF_IDF_Lemma").setMaster("local[*]")

    val b1 = new BufferedWriter(new FileWriter("output/tfidflemma.txt"))

    val sc = new SparkContext(sparkConf)

    //Reading the Text File
    val documents = sc.textFile("src/data/sample")

    //Getting the Lemmatised form of the words in TextFile

    val documentseq = documents.map(f => {
      val lemmatised = Lemmatization.returnLemma(f)
      val splitString = lemmatised.split(" ")
      splitString.toSeq
    })

    documentseq.foreach(println)
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

   val x=dd1.collect
      x.foreach(f => {
        b1.write(f._1 + " " +f._2)
        b1.write("\n")
        b1.flush()
    })
  }


}
