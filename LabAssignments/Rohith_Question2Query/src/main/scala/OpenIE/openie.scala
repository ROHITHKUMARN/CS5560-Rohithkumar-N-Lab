package OpenIE

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

object openie {

  def main(args: Array[String]) {
    val sparkConfig_rk = new SparkConf().setAppName("OpenIE").setMaster("local[*]")

    val sparkcontext_rk = new SparkContext(sparkConfig_rk)

    val input_rk = sparkcontext_rk.textFile("data/sample").map(line => {

      val t= TripletExtraction.returnTriplets(line)
      t
    })
    println(input_rk.collect.mkString("\n"))
    input_rk.saveAsTextFile("data/output/openie")
  }
}
