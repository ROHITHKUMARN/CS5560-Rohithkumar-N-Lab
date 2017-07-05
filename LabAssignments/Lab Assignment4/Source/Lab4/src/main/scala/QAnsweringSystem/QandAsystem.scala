package QAnsweringSystem

import scala.util.control.Breaks._
import scala.io.StdIn.{readInt, readLine}
import java.util.{List, Properties}

import edu.stanford.nlp.ling.CoreAnnotations._
import org.apache.log4j.{Level, Logger}

import scala.collection.mutable.ArrayBuffer
import edu.stanford.nlp.pipeline._
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions._
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.HashMap
object QandAsystem {

  System.setProperty("hadoop.home.dir","/usr/local/Cellar/apache-spark/2.1.0/bin/");
  val sparkConfig_rk = new SparkConf().setAppName("Q&A system").setMaster("local[*]").
    set("spark.driver.memory", "6g").set("spark.executor.memory", "6g")
  val sparkcontext_rk = new SparkContext(sparkConfig_rk)

  val stopWordsfile_rk = sparkcontext_rk.textFile("src/data/stopwords")

  val stopwordslist_rk = stopWordsfile_rk.flatMap(x=>x.split(",")).map(_.trim)
  val broadcastStopWords = sparkcontext_rk.broadcast(stopwordslist_rk.collect.toSet)

  def coreNLP(text: String): Seq[String] = {
    val properties_rk = new Properties()
    properties_rk.put("annotators", "tokenize, ssplit, pos, lemma,ner, parse, dcoref")
    val pipeline = new StanfordCoreNLP(properties_rk)
    val documentlist_rk = new Annotation(text)
    pipeline.annotate(documentlist_rk)
    val nameentities = new ArrayBuffer[String]()
    val sentences_rk = documentlist_rk.get(classOf[SentencesAnnotation])
    for (sentence <- sentences_rk; token <- sentence.get(classOf[TokensAnnotation])) {
      val x = token.originalText().filter(!broadcastStopWords.value.contains(_))
      if (!x.equals(""))
      {
        val nerlist = token.ner()
        if (nerlist != "O") {
          nameentities += (nerlist + " " + token.originalText());
        }

      }
    }
    nameentities
  }


  def main(args: Array[String]) {

    val input = sparkcontext_rk.textFile("src/data/sample")

    val namedentitieslist_rk = input.flatMap(coreNLP(_))

    val openie_rk = input.map(str =>
    {
      val y=  openie.CoreNLP.returnTriplets(str)
      y.split(":").mkString("\n")
    })
    val openielist_rk =openie_rk.flatMap(l=>l.split("\n"))

    val inputlist = input.map(f => {
      val lemmatised_rk = Lemmatization.returnLemma(f)
      val strng = lemmatised_rk.split(" ")
      strng.toSeq
    })

    val hashingTF_rk = new HashingTF()

    //Creating Term Frequency of the document
    val tf_rk = hashingTF_rk.transform(inputlist)
    tf_rk.cache()

    val idf_rk = new IDF().fit(tf_rk)

    val tfidf1 = idf_rk.transform(tf_rk)

    val tfidfvalues = tfidf1.flatMap(f => {
      val xx: Array[String] = f.toString.replace(",[", ";").split(";")
      val values_rk = xx(2).replace("]", "").replace(")", "").split(",")
      values_rk
    })

    val tfidfindex = tfidf1.flatMap(f => {
      val yy: Array[String] = f.toString.replace(",[", ";").split(";")
      val indices_rk = yy(1).replace("]", "").replace(")", "").split(",")
      indices_rk
    })
    val tfidfData = tfidfindex.zip(tfidfvalues)

    var hm_rk = new HashMap[String, Double]

    tfidfData.collect().foreach(f => {
      hm_rk += f._1 -> f._2.toDouble
    })

    val mapp = sparkcontext_rk.broadcast(hm_rk)

    val documentData = inputlist.flatMap(_.toList)
    val dd = documentData.map(f => {
      val i = hashingTF_rk.indexOf(f)
      val h = mapp.value
      (f, h(i.toString))
    })

    val swords = dd.distinct().sortBy(_._2, false)


    val prdd_rk =namedentitieslist_rk.filter(line=>line.contains("PERSON"))
    val lrdd_rk =namedentitieslist_rk.filter(line=>line.contains("LOCATION"))
    val ordd_rk =namedentitieslist_rk.filter(line=>line.contains("ORGANIZATION"))

    println("Welcome to question answering system")
    while(true)
    {
      println("Please enter your question,Enter Bye to Quit")
      val ques = readLine()
      val Lemma_ques = Lemmatization.returnLemma(ques).toUpperCase
      if(Lemma_ques.trim.equalsIgnoreCase("BYE"))
      {
        System.exit(0)
      }
      else if (Lemma_ques.contains("WHO") || Lemma_ques.contains("PERSON"))
      {

        prdd_rk.distinct.take(5).foreach(println)
      }

      else if (Lemma_ques.contains("WHERE") || Lemma_ques.contains("LOCATION"))
      {

        lrdd_rk.distinct.take(5).foreach(println)
      }
      else if (Lemma_ques.contains("WHICH") || Lemma_ques.contains("ORGANIZATION"))
      {

        ordd_rk.distinct.take(5).foreach(println)
      }
      else if (Lemma_ques.contains("SIGNIFICANT") || Lemma_ques.contains("TOP"))
      {
        println("Significant words in the document :")
        swords.take(5).foreach(f => {
          println(f) })
      }
      else
      {
        val quesparse_rk = ques.split(" ")
        for(word <- quesparse_rk)
        {

          val ordd =openie_rk.filter(line=>line.contains(word))
          if(ordd.count()>0)
          {
            ordd.distinct.take(10).foreach(f=>println(f))
          }}
      }
    }
  }

}