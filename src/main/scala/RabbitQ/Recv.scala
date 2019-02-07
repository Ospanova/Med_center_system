package RabbitQ

import Actors.PatientActor._
import Actors.{PatientActor, Patients}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, rejectEmptyResponse}
import com.rabbitmq.client._
import mainn.Boot._
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

    private val QUEUE_NAME = "patient_grud"

    def recv() {

        implicit lazy val timeout: Timeout = Timeout(10.seconds)
        val factory = new ConnectionFactory()
        implicit val formats = DefaultFormats
        factory.setHost("localhost")
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        channel.queueDeclare(QUEUE_NAME, false, false, false, null)
        println(" [*] Waiting for messages. To exit press CTRL+C")
        val deliverCallback: DeliverCallback = (_, delivery) => {
            val message = new String(delivery.getBody, "UTF-8")
            println(" [x] Received '" + message + "'")
            val p: JValue = parse(message)
            val curM = p.extract[Action].action
            if ( curM == "getPatients") {
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
                val patient: Future[ActionPerformed] = (pActor ? DeletePatient(p.extract[Patient].patient_id.get)).mapTo[ActionPerformed]
                patient.onComplete {
                    case Success(value) => println("Deleted")
                    case Failure(exception) => println("Failed")
                }
            }
            else if (curM == "addPatient") {
                val patient: FullPatient = p.extract[FullPatient]
                val act = (pActor ? AddPatient(patient)).mapTo[ActionPerformed]
                act.onComplete {
                    case Success(value) => println("Added")
                    case Failure(exception) => println("Failed")
                }

            }
            else {
                val patient= p.extract[Patient]
                val id = patient.patient_id
                val act = (pActor ? UpdatePatient(id.getOrElse(0), patient)).mapTo[ActionPerformed]
                act.onComplete {
                    case Success(value) => println("Updated")
                    case Failure(exception) => println("Failed")
                }
            }
        }
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, _ => {})
    }
}
