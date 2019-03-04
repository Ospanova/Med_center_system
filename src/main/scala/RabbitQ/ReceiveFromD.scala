package RabbitQ

import Actors.PatientActor._
import Actors.{PatientActor, Patients}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, rejectEmptyResponse}
import com.rabbitmq.client._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import model.{FullPatient, Patient}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class ReceiveFromD() {


    def recv(QUEUE_NAME : String) {

        implicit lazy val timeout: Timeout = Timeout(10.seconds)
        val factory = new ConnectionFactory()
        val routingKey = "response.doctor.#"
        implicit val formats = DefaultFormats
        factory.setHost("localhost")
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        declare("X:gateway.out.fanout", routingKey, true)
        def declare(exchangeName: String, routingKeyName: String, durable: Boolean, args: java.util.HashMap[String, AnyRef] = null): Unit = {
            channel.queueDeclare(QUEUE_NAME, durable, false, false, args)
            channel.queueBind(QUEUE_NAME, exchangeName, routingKey, args)
        }
        println(" [*] Waiting for messages. To exit press CTRL+C")
        val deliverCallback: DeliverCallback = (_, delivery) => {
            val message = new String(delivery.getBody, "UTF-8")
            println(" [x] Received '" + message + "'")
        }
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, _ => {})
    }
}
