name := "Lab2_2"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(

  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
  "edu.stanford.nlp" % "stanford-parser" % "3.6.0",
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "org.apache.spark" % "spark-core_2.10" % "1.6.1" % "provided",
  "org.apache.spark" % "spark-streaming_2.10" % "1.6.1",
  "org.apache.spark" % "spark-mllib_2.10" % "1.6.1"
)

        