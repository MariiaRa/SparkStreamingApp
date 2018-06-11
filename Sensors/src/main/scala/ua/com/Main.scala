package ua.com

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import ua.com.sensors.Sensor
import ua.com.sensors.Sensor.Start
import ua.com.values.{Emission, Humidity, Parameters, Temperature}

import scala.concurrent.ExecutionContextExecutor

object Main {

  implicit def system: ActorSystem = ActorSystem("ActorSystem")

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val logger = LoggerFactory.getLogger(this.getClass)

  val params: Map[String, Parameters] = Map("T" -> Temperature, "E" -> Emission, "H" -> Humidity)
  val myConf: Config = ConfigFactory.load()
  val number: Int = myConf.getInt("actor.number")

  private def startActor(deviceID: String, value: Parameters, n: Int): Unit = {
    (1 to number).foreach { i =>
      logger.info("Actor has been created.")
      val device: ActorRef = system.actorOf(Sensor.props(deviceID + i.toString, value), deviceID)
      device ! Start
    }
  }

  def main(args: Array[String]): Unit = {
    params.foreach {
      case (key, value) =>
        startActor(key, value, number)
    }
  }
}
