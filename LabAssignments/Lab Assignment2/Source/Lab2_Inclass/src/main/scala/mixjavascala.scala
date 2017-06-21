
import org.apache.spark.{SparkContext, SparkConf}


object mixjavascala {

  def main(args: Array[String]) {

    System.setProperty("hadoop.home.dir","/usr/local/Cellar/apache-spark/2.1.0/bin/");

    val sparkConf = new SparkConf().setAppName("Lab2_1").setMaster("local[*]")

    val sc=new SparkContext(sparkConf)

    val input=sc.textFile("input/input")

    val wc=input.flatMap(inp => inp.toString.split(" ").map(word =>(word.charAt(0),word)))
    val wc2= input.map(inp => inp.toString.split(" ").map(word =>(word.charAt(0), word)))

    val output1 =wc.groupByKey()

    println("Flat Map Output")

    output1.foreach(println)

    output1.saveAsTextFile("output/flatmapoutput.txt")

    val output2 =wc.groupByKey()

    println("Map Output")

    output2.foreach(println)

    output2.saveAsTextFile("output/mapoutput.txt")

    val wc3= input.map(inp => inp.toString.split(" ").map(word =>(word.length, word)))
    val output3 =wc.groupByKey()

    println("Map reduce")

    output3.foreach(println)

    output3.saveAsTextFile("output/mapreduce.txt")


  }

}
