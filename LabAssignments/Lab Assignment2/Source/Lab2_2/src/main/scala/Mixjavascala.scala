import org.apache.spark._

/**
  * Created by rohithkumar on 6/20/17.
  */

object Mixjavascala {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/");
    val sparkConf = new SparkConf().setAppName("Mixjavascala").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)
    val input = sc.textFile("input/mydataset")
    val caller:MyJavaFunction = new MyJavaFunction();
    val lemma = input.map(data=>caller.getlemmas(data))
    val mapped = lemma.flatMap(line=>line.split(" ")).filter(nullstring=>(!(nullstring.isEmpty))).map(word=>(word.charAt(0),word))
    val o = mapped.groupByKey()
    o.foreach(println)
    o.saveAsTextFile("output/output.txt")
  }
}
