package openie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

object SparkOpenIE {

  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val sparkConfig = new SparkConf().setAppName("OpenIE").setMaster("local[*]")

    val sparkcontext = new SparkContext(sparkConfig)

    val input = sparkcontext.textFile("src/data/sample").map(line => {

      val t= CoreNLP.returnTriplets(line)
      t
    })
    println(input.collect.mkString("\n"))
    input.saveAsTextFile("output/openie")
  }
}
