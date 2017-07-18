package OpenIE

import edu.knowitall.openie.OpenIE
import edu.knowitall.openie.OpenIECli.{ColumnFormat, SimpleFormat}
import java.io.{PrintWriter, StringWriter}
object OpenIEHelloWorld {

  val openie = new OpenIE
  val sentence = "U.S. President Obama gave a speech"
  val instances = openie.extract(sentence)

  def main(args: Array[String]): Unit = {
    println("Hello, OpenIE world.")
    exampleUsage1()
    exampleUsage2()
    exampleUsage3()
  }

  def exampleUsage1() : Unit = {
    println("Example Usage 1:")
    println("")
    var s = new StringBuilder()
    for (instance <- instances) {
      s.append("Instance: " + instance.toString() + "\n")
    }
    println(s.toString())
  }

  def exampleUsage2() : Unit = {
    println("Example Usage 2:")
    println("")
    val sw = new StringWriter()
    SimpleFormat.print(new PrintWriter(sw), sentence, instances)
    println(sw.toString())
  }

  def exampleUsage3() : Unit = {
    println("Example Usage 3:")
    println("")
    val sw = new StringWriter()
    ColumnFormat.print(new PrintWriter(sw), sentence, instances)
    println(sw.toString())
  }
}
