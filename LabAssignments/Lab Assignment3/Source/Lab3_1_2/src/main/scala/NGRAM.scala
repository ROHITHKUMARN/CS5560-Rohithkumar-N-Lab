import java.io.{BufferedWriter, File, FileWriter}

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Mayanka on 19-06-2017.
  */
object NGRAM {

  def main(args: Array[String]): Unit = {System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")

    val sparkConf = new SparkConf().setAppName("TF-IDFwithoutNLP").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    //Reading the Text File
    val file = sc.textFile("data/Article.txt")

    val grams = file.map(f => NGRAM.getNGrams(f,2)).flatMap(_.toList).map(f=>f.mkString(" "))

    val f = new File("output/bigrams.txt")
    val bw = new BufferedWriter(new FileWriter(f))
    grams.collect.foreach({
      bw.write
    })


    bw.close()

  }

  def getNGrams(sentence: String, n:Int): Array[Array[String]] = {
    val words = sentence
    val ngrams = words.split(' ').sliding(n)
    ngrams.toArray
  }

}


