import java.io.PrintStream

import classification.CoreNLP
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.regression.LabeledPoint

import scala.collection.immutable.HashMap

object sparkNaiveBayes {

  def main(args: Array[String]): Unit = {


    System.setProperty("hadoop.home.dir", "/usr/local/Cellar/apache-spark/2.1.0/bin/")

    val conf = new SparkConf().setAppName("NB")
      .setMaster("local[*]").set("spark.driver.memory", "4g").set("spark.executor.memory", "4g")

    val sc = new SparkContext(conf)

    val topic_output = new PrintStream("data/topic_output.txt")

    val QFV = new PrintStream("data/questions_TFIDF.txt")

    val stopWords=sc.textFile("data/stopwords.txt").collect()

    val stopWordsBroadCast=sc.broadcast(stopWords)

    val df1 = sc.textFile("data/trainingset.txt")

    val df2 = df1.map(f=>f.split(":")).map(f=>{
      val lemma1 = CoreNLP.returnLemma(f(1))
      val split1 = lemma1.split(" ")
      (f(0),split1)
    })


    val stopWordRemovedDF=df2.map(f=>{
      //Filtered numeric and special characters out
      val filteredF=f._2.map(_.replaceAll("[^a-zA-Z]",""))
        //Filter out the Stop Words
        .filter(ff=>{
        if(stopWordsBroadCast.value.contains(ff.toLowerCase))
          false
        else
          true
      })
      (f._1,filteredF)
    })

    val data=stopWordRemovedDF.map(f=>{(f._1,f._2.mkString(" "))})

    val dfseq=stopWordRemovedDF.map(_._2.toSeq)

    //Creating an object of HashingTF Class
    val hashingTF = new HashingTF(stopWordRemovedDF.count().toInt)  // VectorSize as the Size of the Vocab

    //Creating Term Frequency of the document
    val tf = hashingTF.transform(dfseq)
    tf.cache()

    val idf = new IDF().fit(tf)
    //Creating Inverse Document Frequency
    val tfidf1 = idf.transform(tf)
    tfidf1.cache()

    val dff= stopWordRemovedDF.flatMap(f=>f._2)
    val vocab=dff.distinct().collect()
    val dataf = data.zip(tfidf1)
    var hm = new HashMap[String, Int]()
    val IMAGE_CATEGORIES = List("DESC", "ENTY", "HUM", "NUM", "LOC","ABBR")
    var index = 0
    IMAGE_CATEGORIES.foreach(f => {
      hm += IMAGE_CATEGORIES(index) -> index
      index += 1
    })
    val mapping = sc.broadcast(hm)
    val featureVector = dataf.map(f => {

      new LabeledPoint(hm.get(f._1._1).get.toDouble, f._2)
    })
    val training = featureVector
      featureVector.collect().foreach(f=>QFV.println(f))

    val model = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    //Mapping Data Set
    val df1_T = sc.textFile("data/testdata.txt")
    val df2_T= df1_T.map(f=>f.split(":")).map(f=>{
      val lemma1 = CoreNLP.returnLemma(f(1))
      val split1 = lemma1.split(" ")
      (f(0),split1)
    })


    val stopWordRemovedDF_T=df2_T.map(f=>{
      //Filtered numeric and special characters out
      val filteredF=f._2.map(_.replaceAll("[^a-zA-Z]",""))
        //Filter out the Stop Words
        .filter(ff=>{
        if(stopWordsBroadCast.value.contains(ff.toLowerCase))
          false
        else
          true
      })
      (f._1,filteredF)
    })

    val data_T=stopWordRemovedDF_T.map(f=>{(f._1,f._2.mkString(" "))})
    val dfseq_T=stopWordRemovedDF_T.map(_._2.toSeq)

    //Creating an object of HashingTF Class
    val hashingTF_T= new HashingTF(stopWordRemovedDF_T.count().toInt)  // VectorSize as the Size of the Vocab

    //Creating Term Frequency of the document
    val tf_T = hashingTF_T.transform(dfseq_T)
    tf_T.cache()

    val idf_T = new IDF().fit(tf_T)
    //Creating Inverse Document Frequency
    val tfidf1_T = idf_T.transform(tf_T)
    tfidf1_T.cache()



    val dff_T= stopWordRemovedDF_T.flatMap(f=>f._2)
    val vocab_T=dff_T.distinct().collect()

    val dataf_T = data_T.zip(tfidf1_T)
    val featureVector_T = dataf_T.map(f => {

      new LabeledPoint(hm.get(f._1._1).get.toDouble, f._2)})

    val dataset = dataf_T.map(f=>f._1._2)

    val test_vector = dataf_T.map(f=>f._2)
    val t_v = featureVector_T.map(f=>f.features)
    val abc = model.predict(t_v)
    val abc_map = abc.map(f=>{
      if (f == 1.0) "DESC"
      else if (f == 2.0) "ENTY"
      else if (f == 3.0) "ABBR"
      else if (f == 4.0) "HUM"
      else if (f == 5.0) "NUM"
      else if (f == 6.0) "LOC"
      else "Error"
    }
    )
    val result = dataset.zip(abc_map)


    val abc12 =result.collect()
    abc12.foreach(f=>topic_output.println(f))

    featureVector_T.collect().foreach(f=>QFV.println(f))

  }

}