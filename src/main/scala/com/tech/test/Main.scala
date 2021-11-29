package com.tech.test

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success, Try}

object Main {
  private val log = Logger("Main")

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    val csvAccumulator = new SensorDataAccumulator
    val fileUtil = new FileUtil
    val outputFormatter = new OutputFormatter
    val dir = args(0)
    //val dir = "src\\main\\resources\\csv"
    Try {
      fileUtil.getCsvFileList(dir)
    } match {
      case Success(files) =>
        val futureComp = files.map(file => {
          val source = csvAccumulator.getSourceFromCSVFile(file.toString())
          csvAccumulator.buildSensorDataFromSource(source)
        })
        outputFormatter.printAllCSVResults(futureComp.toList)
      case Failure(ex) =>
        println(s"Not able to read files from directory $dir due to exception ${ex.toString}")
        log.error(s"Not able to read files from directory $dir due to exception ${ex.toString}")
        system.terminate()
    }

  }
}