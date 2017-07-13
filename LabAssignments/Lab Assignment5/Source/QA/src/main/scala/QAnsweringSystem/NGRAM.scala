package QAnsweringSystem

import java.io._

import org.apache.spark.{SparkConf, SparkContext}


object NGRAM {

  def main(args: Array[String]): Unit = {System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")

    val sparkConf = new SparkConf().setAppName("TF-IDFwithoutNLP").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    //Reading the Text File
    val input = sc.textFile("src/data/sample")

    val listofgrams = input.map(f => NGRAM.getNGrams(f,2)).flatMap(_.toList).map(f=>f.mkString(" "))

    val outputfile = new File("output/bigrams.txt")

    /*val bufferedwriter = new BufferedWriter(new FileWriter(outputfile))

    listofgrams.collect.foreach(
      {
        bufferedwriter.write
      })*/
    listofgrams.saveAsTextFile("output/ngrams")
    //bufferedwriter.close()

  }
  def getNGrams(sentence: String, n:Int): Array[Array[String]] = {
    val listofwords = sentence
    val ngramslist = listofwords.split(' ').sliding(n)
    ngramslist.toArray
  }

}



