package ua.com

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import ua.com.sensors.TemperatureSensor.Start
import ua.com.sensors.{EmissionSensor, HumiditySensor, TemperatureSensor}

import scala.concurrent.ExecutionContextExecutor

object Main extends App {

  implicit def system: ActorSystem = ActorSystem("ActorSystem")

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val device1: ActorRef = system.actorOf(TemperatureSensor.props("T1"), "TemperatureActor")
  val device2: ActorRef = system.actorOf(TemperatureSensor.props("T2"), "TemperatureActor")
  val device3: ActorRef = system.actorOf(TemperatureSensor.props("T3"), "TemperatureActor")
  val device4: ActorRef = system.actorOf(HumiditySensor.props("H1"), "HumidityActor")
  val device5: ActorRef = system.actorOf(HumiditySensor.props("H2"), "HumidityActor")
  val device6: ActorRef = system.actorOf(HumiditySensor.props("H3"), "HumidityActor")
  val device7: ActorRef = system.actorOf(EmissionSensor.props("E1"), "EmissionActor")
  val device8: ActorRef = system.actorOf(EmissionSensor.props("E2"), "EmissionActor")
  val device9: ActorRef = system.actorOf(EmissionSensor.props("E3"), "EmissionActor")

  device9 ! Start
  device4 ! Start
  device6 ! Start
  device8 ! Start
  device5 ! Start
  device2 ! Start
  device3 ! Start
  device7 ! Start
  device1 ! Start
}
