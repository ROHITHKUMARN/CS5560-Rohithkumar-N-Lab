/**
  * Created by rohithkumar on 6/27/17.
  */

import scala.io._
import java.util._

import edu.stanford.nlp.ling.CoreAnnotations._
import org.apache.log4j.{Level, Logger}

import scala.collection.mutable.ArrayBuffer
import edu.stanford.nlp.pipeline._
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions._
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.HashMap

object QA2 {

  System.setProperty("hadoop.home.dir","/usr/local/Cellar/apache-spark/2.1.0/bin/");

  val sparkConfig_rk = new SparkConf().setAppName("Q&A system").setMaster("local[*]").
    set("spark.driver.memory", "6g").set("spark.executor.memory", "6g")

  val sparkcontext_rk = new SparkContext(sparkConfig_rk)

  val stopinput = sparkcontext_rk.textFile("input/englishstopwords.txt")

  val stop_rk = stopinput.flatMap(x=>x.split(",")).map(_.trim)

  val bcstop_rk = sparkcontext_rk.broadcast(stopinput.collect.toSet)

  def findNER(text: String): Seq[String] = {
    val properties = new Properties()
    properties.put("annotators", "tokenize, ssplit, pos, lemma,ner, parse, dcoref")
    val pline = new StanfordCoreNLP(properties)
    val docs = new Annotation(text)
    pline.annotate(docs)
    val ners = new ArrayBuffer[String]()
    val sentences = docs.get(classOf[SentencesAnnotation])
    for (sentence <- sentences; token <- sentence.get(classOf[TokensAnnotation])) {
      val x = token.originalText().filter(!bcstop_rk.value.contains(_))
      if (!x.equals(""))
      {
        val ner = token.ner()
        if (ner != "O") {
          ners += (ner + " " + token.originalText());
        }

      }
    }
    ners
  }


  def main(args: Array[String]) {


    val input = sparkcontext_rk.textFile("input/mydataset")

    val ner = input.flatMap(findNER(_))

    val inputlines = input.map(f => {
      val lsd = CoreNLP.returnLemma(f)
      val string = lsd.split(" ")
      string.toSeq
    })

    val hashingTF = new HashingTF()

    //Creating Term Frequency of the document
    val TF_rk = hashingTF.transform(inputlines)
    TF_rk.cache()


    val IDF_rk = new IDF().fit(TF_rk)

    //Creating Inverse Document Frequency
    val tfidf_rk = IDF_rk.transform(TF_rk)

    val vl = tfidf_rk.flatMap(n => {
      val xyz: Array[String] = n.toString.replace(",[", ";").split(";")
      val stu = xyz(2).replace("]", "").replace(")", "").split(",")
      stu
    })

    val ix = tfidf_rk.flatMap(u => {
      val lmn: Array[String] = u.toString.replace(",[", ";").split(";")
      val hgi = lmn(1).replace("]", "").replace(")", "").split(",")
      hgi
    })

    tfidf_rk.foreach(f => println(f))

    val tfidfData = vl.zip(ix)

    var hmap = new HashMap[String, Double]

    tfidfData.take(10).foreach(f => {
      hmap += f._1 -> f._2.toDouble
    })

    val m = sparkcontext_rk.broadcast(hmap)

    val documentData = inputlines.flatMap(_.toList)
    val ddata_rk = documentData.map(f => {
      val r = hashingTF.indexOf(f)
      val k = m.value
      (f, k(r.toString))
    })

    val impwords = ddata_rk.distinct().sortBy(_._2, false)

    val personrdd =ner.filter(line=>line.contains("PERSON"))
    val locrdd =ner.filter(line=>line.contains("LOCATION"))
    val organizationrdd =ner.filter(line=>line.contains("ORGANIZATION"))


    println("Welcome to QA system")

    while(true)
    {
      println("Enter 1 to ask question/0 to Quit")
      val choice = readLine()
      if(choice.equalsIgnoreCase("0"))
        System.exit(0);
      else if(choice.equalsIgnoreCase("1")){
        println("Enter your question...!")
      val question = readLine()
      val qlemma = CoreNLP.returnLemma(question).toLowerCase
         if (qlemma.contains("who") || qlemma.contains("person"))
      {

        personrdd.distinct.take(5).foreach(println)
      }

      else if (qlemma.contains("where") || qlemma.contains("location"))
      {

        locrdd.distinct.take(5).foreach(println)
      }
      else if (qlemma.contains("which") || qlemma.contains("organization"))
      {
        organizationrdd.distinct.take(5).foreach(println)
      }
    }
    }

  }

}