package com.tech.test

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.testkit.TestKit
import org.scalatest.{AsyncWordSpecLike, BeforeAndAfterAll, Matchers, WordSpecLike}

class SensorDataAccumulatorSpec
  extends TestKit(ActorSystem("TestingAkkaStreams"))
    with AsyncWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  implicit val materializer: Materializer = Materializer(system)

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A SensorDataAccumulator" should {
    "read file from test resources" in {
      val sensorDataAccumulator = new SensorDataAccumulator()

      val source = sensorDataAccumulator.getSourceFromCSVFile("src/test/resources/csv/leader1.csv")

      source should not be null
    }

    "read file with missing entries from test resources" in {
      val sensorDataAccumulator = new SensorDataAccumulator()

      val source = sensorDataAccumulator.getSourceFromCSVFile("src/test/resources/csv/leader4.csv")

      source should not be null
    }

    "run sensor data from test resources" in {
      val sensorDataAccumulator = new SensorDataAccumulator()

      val source = sensorDataAccumulator.getSourceFromCSVFile("src/test/resources/csv/leader1.csv")

      sensorDataAccumulator.buildSensorDataFromSource(source) map {
        sensorData => assert(sensorData.size == 3)
      }
    }

    "run sensor data from incorrect test resources" in {
      val sensorDataAccumulator = new SensorDataAccumulator()

      val source = sensorDataAccumulator.getSourceFromCSVFile("src/test/resources/csv/leader4.csv")

      sensorDataAccumulator.buildSensorDataFromSource(source) map {
        sensorData => assert(sensorData.size == 4)
      }
    }
  }
}
