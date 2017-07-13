package wordnet

import org.apache.spark.{SparkConf, SparkContext}
import rita.RiWordNet

object WordNetSpark_RK {
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")
    val config_rk = new SparkConf().setAppName("WordNetSpark").setMaster("local[*]").set("spark.driver.memory", "4g").set("spark.executor.memory", "4g")
    val sparkcontext_rk = new SparkContext(config_rk)
    val mydata_rk=sparkcontext_rk.textFile("src/data/sample")
    val datacollection=mydata_rk.map(f=>{
      val wordnet_rk = new RiWordNet("/Users/satheeshchandra/Desktop/KDM/WordNet-3.0")
      val array_rk=f.split(" ")
      for(v <- array_rk){
       val synonym_rk= getSynoynms_rk(wordnet_rk,v)
        if(synonym_rk==null){}
        else{
      println("synonym for word " +v+"="+synonym_rk.mkString(" "))}}}
    )
    datacollection.collect
  }
  def getSynoynms_rk(wordnet:RiWordNet,word:String): Array[String] ={
    val s=wordnet.getAllSynonyms(word,"n",10)
    s
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