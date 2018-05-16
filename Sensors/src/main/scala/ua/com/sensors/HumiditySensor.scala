package ua.com.sensors

import akka.actor.{Actor, ActorLogging, Props, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.{Config, ConfigFactory}
import ua.com.sensors.TemperatureSensor.Start
import ua.com.values.humidity.Humidity

import scala.concurrent.duration._

object HumiditySensor {

  case object GetActualHumidity

  case object Key

  case class SensorData(deviceID: String, measurement: Int)

  def props(deviceID: String): Props = Props(new HumiditySensor(deviceID))

}

class HumiditySensor(deviceID: String) extends Actor with ActorLogging with Timers {

  import HumiditySensor._
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
      timers.startPeriodicTimer(Key, GetActualHumidity, 5.second)
      log.info("Started")

    case GetActualHumidity => {

      val url = host + path + parameter1 + deviceID + parameter2 + Humidity.getValue
      val request = HttpRequest.apply(HttpMethods.GET, url)
      HttpRequest.apply()
      http.singleRequest(request).pipeTo(self)
    }
    case HttpResponse(StatusCodes.OK, headers, entity, _) => log.info("Got response: " + StatusCodes.OK)

    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

  private def displayHumidityData(month: Int): Unit = {
    log.info(s"The current humidity is ${Humidity.getValue}%")
  }
}
