package com.tech.test

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{FlowShape, IOResult}
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.GraphDSL.Implicits.port2flow
import akka.stream.scaladsl.{Balance, FileIO, Flow, GraphDSL, Merge, Sink, Source}

import java.nio.file.{Path, Paths}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class SensorOutput(min : Int, max : Int, sum: BigInt, count: BigInt, countNan: BigInt)

class SensorDataAccumulator(implicit val system: ActorSystem) {


  val foldingFlow: Flow[List[String], Map[String, SensorOutput], NotUsed] =
    Flow[List[String]].fold(Map[String, SensorOutput]())((m, e) => {
      if(e.head.isEmpty) m
      else {
        var acc = m.getOrElse(e.head, SensorOutput(-1, 0, 0, 0, 0))
        Try(Integer.parseInt(e.tail.head)) match {
          case Success(reading) =>
            val min = if (acc.min == -1 || reading < acc.min) reading else acc.min
            val max = if (reading > acc.max) reading else acc.max
            val sum = acc.sum + reading
            val count = acc.count + 1
            acc = SensorOutput(min, max, sum, count, acc.countNan)
          case Failure(_) => acc = acc.copy(countNan = acc.countNan + 1)
        }
        m + (e.head -> acc)
      }
    })

  val parallelFlow: Flow[List[String], Map[String, SensorOutput], NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    val broadcaster = builder.add(Balance[List[String]](4))
    val merger = builder.add(Merge[Map[String, SensorOutput]](4))

    broadcaster.out(0) ~> foldingFlow.async ~> merger.in(0)
    broadcaster.out(1) ~> foldingFlow.async ~> merger.in(1)
    broadcaster.out(2) ~> foldingFlow.async ~> merger.in(2)
    broadcaster.out(3) ~> foldingFlow.async ~> merger.in(3)

    FlowShape(broadcaster.in, merger.out)
  })

  def getSourceFromCSVFile(fileName: String): Source[List[String], Future[IOResult]] = {
    val csvFile: Path = Paths.get(fileName)
    FileIO.fromPath(csvFile)
      .via(CsvParsing.lineScanner(','))
      .map(_.map(_.utf8String)).drop(1).async
  }

  def buildSensorDataFromSource(source: Source[List[String], Future[IOResult]]): Future[Map[String, SensorOutput]] = {
    source.via(foldingFlow).async
      .runWith(Sink.head)
  }
}
