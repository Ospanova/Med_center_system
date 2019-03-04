package RabbitQ

import Actors.PatientActor._
import Actors.{PatientActor, Patients}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, rejectEmptyResponse}
import com.rabbitmq.client._
import realization.Boot2._
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

case class Recv() {


    def recv(QUEUE_NAME : String) {

        implicit lazy val timeout: Timeout = Timeout(10.seconds)
        val factory = new ConnectionFactory()
        val routingKey = "response.patient.#"
        implicit val formats = DefaultFormats
        factory.setHost("localhost")
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        declare("X:gateway.in.fanout", routingKey, true)
        def declare(exchangeName: String, routingKeyName: String, durable: Boolean, args: java.util.HashMap[String, AnyRef] = null): Unit = {
            channel.queueDeclare(QUEUE_NAME, durable, false, false, args)
            channel.queueBind(QUEUE_NAME, exchangeName, routingKey, args)
        }
        println(" [*] Waiting for messages. To exit press CTRL+C")
        val deliverCallback: DeliverCallback = (_, delivery) => {
            val message = new String(delivery.getBody, "UTF-8")
            println(" [x] Received '" + message + "'")

            val p: JValue = parse(message)
            val curM = p.extract[Action].action
            if ( curM == "getPatients") {
                println("REE")
                val act: Future[Patients] = (pActor ? GetPatients).mapTo[Patients]
                act.onComplete {
                    case Success(value) => println(value)
                    case Failure(exception) => println("Failed")
                }
            }
            else if (curM == "getByID") {
               val k = p.extract[PID].id
                val patient: Future[Option[Patient]] = (pActor  ? GetbyID(k)).mapTo[Option[Patient]]

                patient.onComplete {
                    case Success(value) => println(value)
                    case Failure(exception) => println("Failed")
                }
            }
            else if (curM == "delete") {
                val patient: Future[ActionPerformed] = (pActor ? DeletePatient(p.extract[PID].id)).mapTo[ActionPerformed]
                patient.onComplete {
                    case Success(value) => println(value.description)
                    case Failure(exception) => println("Failed")
                }
            }
            else if (curM == "addPatient") {
                val patient: FullPatient = p.extract[FullPatient]
                val act: Future[Patient] = (pActor ? AddPatient(patient)).mapTo[Patient]
                act.onComplete {
                    case Success(value) => println("Added")
                    case Failure(exception) => println("Fail")
                }
            }
            else if (curM == "getSchedule") {
               println("GETTING SCHEDEULE")
            }
            else {
                val patient= p.extract[Patient]
                val id = patient.ID
                val act = (pActor ? UpdatePatient(id.getOrElse(0), patient)).mapTo[ActionPerformed]
                act.onComplete {
                    case Success(value) => println(value.description)
                    case Failure(exception) => println("Failed")
                }
            }
        }
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, _ => {})
    }
}
