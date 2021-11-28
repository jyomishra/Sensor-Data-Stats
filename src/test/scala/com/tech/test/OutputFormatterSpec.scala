package com.tech.test

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OutputFormatterSpec  extends TestKit(ActorSystem("TestingAkkaStreams"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  private val outContent = new ByteArrayOutputStream
  private val originalOut = System.out

  override def beforeAll(): Unit = {
    System.setOut(new PrintStream(outContent))
  }

  val outputFormatter = new OutputFormatter

  override def afterAll(): Unit = {
    System.setOut(originalOut)
    TestKit.shutdownActorSystem(system)
  }

  "An output formatter " should {
    "calculate no of all measurements for printing " in {
      val sensorOutputMap = Map("s2" -> SensorOutput(78,80,158,2,0), "s3" -> SensorOutput(-1,0,0,0,1),
        "s1" -> SensorOutput(98,98,98,1,0), "s6" -> SensorOutput(-1,0,0,0,1))
      val totalMeasureCount = outputFormatter.calculateTotalMeasurement(sensorOutputMap)
      totalMeasureCount shouldBe 5
    }

    "calculate no of failed measurements for printing " in {
      val sensorOutputMap = Map("s2" -> SensorOutput(78,80,158,2,0), "s3" -> SensorOutput(-1,0,0,0,1),
        "s1" -> SensorOutput(98,98,98,1,0), "s6" -> SensorOutput(-1,0,0,0,1))
      val totalMeasureCount = outputFormatter.calculateFailedMeasurement(sensorOutputMap)
      totalMeasureCount shouldBe 2
    }

    "return sorted sensor data for printing" in {
      val sensorOutputMap = Map("s2" -> SensorOutput(78,80,158,2,0), "s3" -> SensorOutput(-1,0,0,0,1),
        "s1" -> SensorOutput(98,98,98,1,0), "s6" -> SensorOutput(-1,0,0,0,1))
      val sortedOutput = outputFormatter.getSortedSensorData(sensorOutputMap)
      sortedOutput.head._2.max shouldBe 98
      sortedOutput.tail.head._2.max shouldBe 80
    }

    "print sorted no of records from sensor data" in {
      val sensorOutputMap = Map("s2" -> SensorOutput(78,80,158,2,0), "s3" -> SensorOutput(-1,0,0,0,1),
        "s1" -> SensorOutput(98,98,98,1,0), "s6" -> SensorOutput(-1,0,0,0,1))
      outputFormatter.printCombinedOutput(1, sensorOutputMap)
      outContent.toString() contains "CSV file processed : 1"
      outContent.reset()
    }

    "print all csv read data" in {
      val sensorOutputMap = Map("s2" -> SensorOutput(78,80,158,2,0), "s3" -> SensorOutput(-1,0,0,0,1),
        "s1" -> SensorOutput(98,98,98,1,0), "s6" -> SensorOutput(-1,0,0,0,1))
      val futureData = Future(sensorOutputMap)
      outputFormatter.printAllCSVResults(List.fill(2)(futureData))
      outContent.toString() contains "CSV file processed : 2"
      outContent.reset()
    }
  }

}
