package ua.com.sensors

import akka.actor.{Actor, ActorLogging, Props, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}
import ua.com.sensors.TemperatureSensor.Start
import ua.com.values.emission.Emission

import scala.concurrent.duration._

object EmissionSensor {

  case object GetActualEmission

  case object Key

  case class SensorData(deviceID: String, measurement: BigDecimal)

  def props(deviceID: String): Props = Props(new EmissionSensor(deviceID))
}

class EmissionSensor(deviceID: String) extends Actor with ActorLogging with Timers {

  import EmissionSensor._
  import akka.pattern.pipe

  val http = Http(context.system)

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val myConf: Config = ConfigFactory.load()
  val host: String = myConf.getString("url.host")
  val path: String = myConf.getString("url.path")
  val parameter1: String = myConf.getString("url.parameter1")
  val parameter2: String = myConf.getString("url.parameter2")

  override def receive: Receive = {

    case Start =>
      timers.startPeriodicTimer(Key, GetActualEmission, 7.second)
      log.info("Started")

    case GetActualEmission =>
      val url = host + path + parameter1 + deviceID + parameter2 + Emission.getValue
      log.info("url is:" + url)
      val request = HttpRequest.apply(HttpMethods.GET, url)
      HttpRequest.apply()
      http.singleRequest(request).pipeTo(self)

    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("Got response, body: " + StatusCodes.OK)
      }

    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

  private def displayHumidityData(): Unit = {
    log.info(s"The current CO/CO2 ration is ${Emission.getValue}")
  }
}
