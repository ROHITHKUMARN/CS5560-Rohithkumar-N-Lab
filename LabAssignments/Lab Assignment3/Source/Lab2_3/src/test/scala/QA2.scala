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
object main {

  System.setProperty("hadoop.home.dir","/usr/local/Cellar/apache-spark/2.1.0/bin/");

  val sparkConf = new SparkConf().setAppName("Q&A system").setMaster("local[*]").
    set("spark.driver.memory", "6g").set("spark.executor.memory", "6g")

  val sc = new SparkContext(sparkConf)

  val stopWordsInput = sc.textFile("input/englishstopwords.txt")

  val stopwords = stopWordsInput.flatMap(x=>x.split(",")).map(_.trim)

  val broadcastStopWords = sc.broadcast(stopwords.collect.toSet)

  def coreNLP(text: String): Seq[String] = {
    val props = new Properties()
    props.put("annotators", "tokenize, ssplit, pos, lemma,ner, parse, dcoref")
    val pipeline = new StanfordCoreNLP(props)
    val doc = new Annotation(text)
    pipeline.annotate(doc)
    val ners = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation])
    for (sentence <- sentences; token <- sentence.get(classOf[TokensAnnotation])) {
      val x = token.originalText().filter(!broadcastStopWords.value.contains(_))
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
    //setting spark properties

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    //Stop words removal
    val input = sc.textFile("input/mydataset")

    //Ner extraction
    val ner = input.flatMap(coreNLP(_))

    // Getting TFIDF for the lemmatised input
    val inputseq = input.map(f => {
      val lemmatised = CoreNLP.returnLemma(f)
      val splitString = lemmatised.split(" ")
      splitString.toSeq
    })

    val hashingTF = new HashingTF()

    //Creating Term Frequency of the document
    val tf1 = hashingTF.transform(inputseq)
    tf1.cache()


    val idf1 = new IDF().fit(tf1)

    //Creating Inverse Document Frequency
    val tfidf1 = idf1.transform(tf1)

    val tfidfvalues = tfidf1.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val values = ff(2).replace("]", "").replace(")", "").split(",")
      values
    })

    val tfidfindex = tfidf1.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      val indices = ff(1).replace("]", "").replace(")", "").split(",")
      indices
    })

    tfidf1.foreach(f => println(f))

    val tfidfData = tfidfindex.zip(tfidfvalues)

    var hm = new HashMap[String, Double]

    tfidfData.collect().foreach(f => {
      hm += f._1 -> f._2.toDouble
    })

    val mapp = sc.broadcast(hm)

    val documentData = inputseq.flatMap(_.toList)
    val dd = documentData.map(f => {
      val i = hashingTF.indexOf(f)
      val h = mapp.value
      (f, h(i.toString))
    })

    val significantwords = dd.distinct().sortBy(_._2, false)
    /* val dd2 = dd1.take(5).foreach(f => {
       println(f)
     }) */

    val personrdd =ner.filter(line=>line.contains("PERSON"))
    val locrdd =ner.filter(line=>line.contains("LOCATION"))
    val organizationrdd =ner.filter(line=>line.contains("ORGANIZATION"))
    // personrdd.foreach(println)
    println("Welcome to question answering system")
    while(true)
    {
      println("Please enter your question,Enter 0 to Quit")

      //performing lemmatization on the question
      val ques = readLine()
      //  Lemmatization.returnLemma(ques)
      val Lemma_ques = CoreNLP.returnLemma(ques).toUpperCase
      // println(Lemma_ques)
      if(Lemma_ques.equalsIgnoreCase("0")) {
        System.exit(0)
      }
      else if (Lemma_ques.contains("WHO") || Lemma_ques.contains("PERSON"))
      {

        personrdd.distinct.take(5).foreach(println)
      }

      else if (Lemma_ques.contains("WHERE") || Lemma_ques.contains("LOCATION"))
      {

        locrdd.distinct.take(5).foreach(println)
      }
      else if (Lemma_ques.contains("WHICH") || Lemma_ques.contains("ORGANIZATION"))
      {

        organizationrdd.distinct.take(5).foreach(println)
      }


    }

  }

}