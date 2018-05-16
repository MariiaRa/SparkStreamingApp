package ua.com

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import ua.com.sensors.TemperatureSensor.Start
import ua.com.sensors.{EmissionSensor, HumiditySensor, TemperatureSensor}
import scala.concurrent.duration._
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

  system.scheduler.scheduleOnce(2 seconds, device4, Start)
  system.scheduler.scheduleOnce(4 seconds,  device6, Start)
  system.scheduler.scheduleOnce(6 seconds,  device8, Start)
  system.scheduler.scheduleOnce(8 seconds, device5, Start)
  system.scheduler.scheduleOnce(10 seconds,  device2, Start)
  system.scheduler.scheduleOnce(12 seconds,  device3, Start)
  system.scheduler.scheduleOnce(14 seconds,  device7, Start)
  system.scheduler.scheduleOnce(16 seconds,  device1, Start)


}
