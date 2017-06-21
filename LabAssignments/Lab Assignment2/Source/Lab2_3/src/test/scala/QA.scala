/**
  * Created by rohithkumar on 6/20/17.
  */
import java.util
import scala.io._
import java.util.{List, Properties}
import edu.stanford.nlp.ling.CoreAnnotations._
import scala.collection.mutable.ArrayBuffer
import edu.stanford.nlp.pipeline._
import scala.collection.JavaConversions._
import org.apache.spark._

object QA {

  System.setProperty("hadoop.home.dir","/usr/local/Cellar/apache-spark/2.1.0/bin/");
  val sparkConf = new SparkConf().setAppName("question & answering system").setMaster("local[*]")
  val sc=new SparkContext(sparkConf)
  val props = new Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma,ner, parse, dcoref")
  val pl = new StanfordCoreNLP(props)

  def findNER(text: String): Seq[String] = {
    val d = new Annotation(text)
    pl.annotate(d)
    val array = new ArrayBuffer[String]()
    val sents = d.get(classOf[SentencesAnnotation])
    for (sentence <- sents; token <- sentence.get(classOf[TokensAnnotation])) {
      val ner = token.ner();
      if (ner != "O" ) {
        array += (ner +" " +token.originalText());
      }
    }
    array
  }


  def main(args: Array[String]) {
    val input = sc.textFile("/Users/satheeshchandra/Desktop/KDM/bbcsport/cricket/001.txt")
    val lemma = input.flatMap(findNER(_))
    val person =lemma.filter(line=>line.contains("PERSON"))
    val location =lemma.filter(line=>line.contains("LOCATION"))
    val organisation= lemma.filter(line => line.contains("ORGANISATION"))
    val date= lemma.filter(line => line.contains("DATE"))
    val money= lemma.filter(line => line.contains("MONEY"))
    val time= lemma.filter(line => line.contains("TIME"))
    println("Please ask your questions...!")
    while(true)
    {
      println("Enter 1 to ask question/0 to Quit")
      val choice = readLine()
      if(choice.equalsIgnoreCase("0"))
        System.exit(0);
      else if(choice.equalsIgnoreCase("1")){
        println("Enter your question...!")
        val question =readLine()
        if (question.contains("who".toLowerCase))
        {
          person.foreach(println)
          person.saveAsTextFile("output/person")
        }
        else if (question.contains("where".toLowerCase))
        {

          location.foreach(println)
          location.saveAsTextFile("output/location")
        }
        else if (question.contains("when".toLowerCase))
        {
          date.foreach(println)
          date.saveAsTextFile("output/date")
        }
        else if (question.contains("how much".toLowerCase))
        {
          money.foreach(println)
          money.saveAsTextFile("output/money")

        }
        else if (question.contains("At what".toLowerCase))
        {
          time.foreach(println)
          time.saveAsTextFile("output/time")
        }
      }

    }

  }

}