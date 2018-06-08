package ua.com.sensors

import akka.actor.{Actor, ActorLogging, Props, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}
import ua.com.values.Parameters

import scala.concurrent.duration._

object Sensor {

  case object GetActualData

  case object Key

  case object Start

  case class SensorData(deviceID: String, measurement: BigDecimal)

  def props(deviceID: String, value: Parameters): Props = Props(new Sensor(deviceID, value))
}

class Sensor(deviceID: String, value: Parameters) extends Actor with ActorLogging with Timers {

  import Sensor._
  import akka.pattern.pipe
  import context.dispatcher

  val myConf: Config = ConfigFactory.load()
  val host: String = myConf.getString("url.host")
  val path: String = myConf.getString("url.path")
  val parameter1: String = myConf.getString("url.parameter1")
  val parameter2: String = myConf.getString("url.parameter2")
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val http = Http(context.system)

  override def receive: Receive = {
    case Start =>
      timers.startPeriodicTimer(Key, GetActualData, 5.second)
      log.info("Started!")

    case GetActualData => {
      val url = host + path + parameter1 + deviceID + parameter2 + value.getValue
      val request = HttpRequest.apply(HttpMethods.GET, url)
      HttpRequest.apply()
      http.singleRequest(request).pipeTo(self)
      log.info("Requested url: " + url)
    }

    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("Got response: " + StatusCodes.OK)
      }

    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}
