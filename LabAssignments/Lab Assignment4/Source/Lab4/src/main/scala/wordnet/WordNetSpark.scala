package wordnet

import org.apache.spark.{SparkConf, SparkContext}
import rita.RiWordNet

/**
  * Created by Mayanka on 26-06-2017.
  */
object WordNetSpark {
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")
    val conf = new SparkConf().setAppName("WordNetSpark").setMaster("local[*]").set("spark.driver.memory", "4g").set("spark.executor.memory", "4g")
    val sc = new SparkContext(conf)
    val data=sc.textFile("src/data/sample")
    val dd=data.map(f=>{
      val wordnet = new RiWordNet("/Users/satheeshchandra/Desktop/KDM/WordNet-3.0")
      val farr=f.split(" ")
      for(x <- farr){
      getSynoymns(wordnet,x)}
    })
    dd.collect.foreach(f=>println(f))
  }
  def getSynoymns(wordnet:RiWordNet,word:String): Array[String] ={
    println(word)
    val pos=wordnet.getPos(word)
    println(pos.mkString(" "))
    val syn=wordnet.getAllSynonyms(word, pos(0))
    syn
  }

}
/*output:
aeroplane
n//noun
airliner
airplane
amphibian
amphibious aircraft
attack aircraft
autogiro
autogyro
biplane
bomber
chopper
 */