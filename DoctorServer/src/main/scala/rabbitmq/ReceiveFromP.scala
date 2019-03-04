package RabbitQ

import Actors.PatientActor._
import Actors.{PatientActor, Patients}
import actors.DoctorActor
import actors.DoctorActor.getbyID
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, rejectEmptyResponse}
import com.rabbitmq.client._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import model.{Doctor, FullPatient, Patient}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import rabbitmq.SendToP

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class ReceiveFromP() {


    def recv(QUEUE_NAME : String) {

        implicit lazy val timeout: Timeout = Timeout(10.seconds)
        val factory = new ConnectionFactory()
        val routingKey = "request.doctor.#"
        implicit val formats = DefaultFormats
        factory.setHost("localhost")
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        declare("X:gateway.in.fanout", routingKey, true)
        def declare(exchangeName: String, routingKeyName: String, durable: Boolean, args: java.util.HashMap[String, AnyRef] = null): Unit = {
            channel.queueDeclare(QUEUE_NAME, durable, false, false, args)
            channel.queueBind(QUEUE_NAME, exchangeName, routingKey, args)
        }
        implicit val system = ActorSystem()
        implicit val materializer = ActorMaterializer()
        val dActor: ActorRef = system.actorOf(DoctorActor.props, "PatientActor")

        println(" [*] Waiting for messages")
        val deliverCallback: DeliverCallback = (_, delivery) => {
            val message = new String(delivery.getBody, "UTF-8")
            println(" [x] Received '" + message + "'")
            val sender = SendToP()
            val p: JValue = parse(message)
            val curM = p.extract[Action].action
            if (curM == "giveSchedule") {
                //sender.sendMsg("""{"action":"getSchedule"}""")
                val k = p.extract[PID].id
                println(k)
                val d = (dActor ? GetbyID(k)).mapTo[Option[Doctor]]
                d.onComplete {
                    case Success(value) => {
                        value match  {
                            case Some(d) => sender.sendMsg(d.stringCalendar)
                            case None => sender.sendMsg("NONE")
                        }
                    }
                    case Failure(exp) => sender.sendMsg(exp.getMessage)
                }
            }

        }
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, _ => {})
    }
}
