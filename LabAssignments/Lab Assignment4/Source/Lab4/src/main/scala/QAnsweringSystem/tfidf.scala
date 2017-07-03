package QAnsweringSystem

import java.io.{BufferedWriter, FileWriter}

import org.apache.spark._
import org.apache.spark.mllib.feature._

import scala.collection.immutable.HashMap

/**
  * Created by rohithkumar on 6/27/17.
  */

object tfidf {

  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")

    val sparkConfig = new SparkConf().setAppName("TF_IDF").setMaster("local[*]")

    val a1 = new BufferedWriter(new FileWriter("output/tfidforiginal.txt"))

    val sparkcontext = new SparkContext(sparkConfig)

    //Reading the Text File
    val documents = sparkcontext.textFile("src/data/sample")

    //Getting the Lemmatised form of the words in TextFile
    val doclist = documents.map(f => {
      val line = f.split(" ")
      line.toSeq
    })

    //Creating an object of HashingTF Class

    val TF = new HashingTF()
    //Creating Term Frequency of the document

    val tf = TF.transform(doclist)
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

    var hm = new HashMap[String, Double]

    tfidfData.collect().foreach(f => {
      hm += f._1 -> f._2.toDouble
    })

    val map = sparkcontext.broadcast(hm)

    val documentData = doclist.flatMap(_.toList)

    val docdata = documentData.map(f => {
      val i = TF.indexOf(f)
      val h = map.value
      (f, h(i.toString))
    })

    val dd1 = docdata.distinct().sortBy(_._2, false)

    val x= dd1.collect
    x.foreach(f => {
      a1.write(f._1 + " " +f._2)
      a1.write("\n")
      a1.flush()
    })

  }

}
