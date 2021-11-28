package com.tech.test

import akka.actor.ActorSystem

import java.time.Instant
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class OutputFormatter(implicit val system: ActorSystem) {

  val startTime: Long = Instant.now().getEpochSecond

  def printAllCSVResults(futureComp: List[Future[Map[String, SensorOutput]]]): Unit = {
    println(s"Waiting for all streams to materialized elapsed sec : ${Instant.now().getEpochSecond - startTime}")
    val noOfFileProcessed = futureComp.size
    Future.sequence(futureComp).onComplete {
      case Success(value) =>
        println(s"All streams materialization complete elapsed sec : ${Instant.now().getEpochSecond - startTime}")
        val combinedMap = value.foldLeft(Map[String, SensorOutput]())((acc, compVals) => {
          var accMap = acc
          compVals.foreach(compVal => {
            val optSensorOut = acc.get(compVal._1)
            optSensorOut match {
              case Some(out) =>
                val min = if (out.min == -1 || compVal._2.min < out.min) compVal._2.min else out.min
                val max = if (compVal._2.max > out.max) compVal._2.min else out.max
                val sum = out.sum + compVal._2.sum
                val count = out.count + compVal._2.count
                val countNaN = out.countNan + compVal._2.countNan
                accMap = accMap + (compVal._1 -> SensorOutput(min, max, sum, count, countNaN))
              case None =>
                accMap = accMap + (compVal._1 -> compVal._2)
            }
          })
          accMap
        })
        printCombinedOutput(noOfFileProcessed, combinedMap)
      case Failure(ex) => println(ex)
    }
  }

  def printCombinedOutput(noOfFiles: Int, sensorData: Map[String, SensorOutput]): Unit = {
    println(s"Total no of CSV file processed : $noOfFiles")
    println(s"Total No of measurement processed : ${calculateTotalMeasurement(sensorData)}")
    println(s"No of failed measurement processed : ${calculateFailedMeasurement(sensorData)}")
    println("\nSensors with highest avg humidity:\n\nsensor-id,min,avg,max")
    val sortedSensorData = getSortedSensorData(sensorData)

    def printStmt(name:String, min: String, max:String, avg: String): Unit = {
      println(s"$name,$min,$avg,$max")
    }

    sortedSensorData.foreach(sensorOut => {
      if(sensorOut._2.count > 0)
        printStmt(sensorOut._1, sensorOut._2.min.toString, sensorOut._2.max.toString, (sensorOut._2.sum / sensorOut._2.count).toString())
      else if(sensorOut._2.countNan > 0)
        printStmt(sensorOut._1, "NaN", "NaN", "NaN")
      else
        printStmt(sensorOut._1, "0", "0", "0")
    })
    println(s"Total time taken to process the records : ${Instant.now().getEpochSecond - startTime}")
    system.terminate()
  }

  def calculateTotalMeasurement(sensorData: Map[String, SensorOutput]): BigInt = {
    sensorData.foldLeft(BigInt(0))((acc, sOut) => {
      acc + sOut._2.countNan + sOut._2.count
    })
  }

  def calculateFailedMeasurement(sensorData: Map[String, SensorOutput]): BigInt = {
    sensorData.foldLeft(BigInt(0))((acc, sOut) => {
      acc + sOut._2.countNan
    })
  }

  def getSortedSensorData(sensorData: Map[String, SensorOutput]): Map[String, SensorOutput] = {
    def comparator(a:(String, SensorOutput), b:(String, SensorOutput)) =  {
      val a_avg: BigInt = if(a._2.count > 0) a._2.sum / a._2.count else -1
      val b_avg:BigInt = if(b._2.count > 0) b._2.sum / b._2.count else -1
      if(a_avg > b_avg) true
      else if(b._2.count == 0 && a._2.countNan < b._2.countNan) true
      else false
    }
    ListMap(sensorData.toSeq.sortWith(comparator):_*)
  }
}
