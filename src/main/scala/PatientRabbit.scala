

import Actors.PatientActor._
import Actors.{PatientActor, Patients}
import akka.actor.{Actor, ActorRef, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import model._
import com.rabbitmq.client._
import model.Patient
import spray.json._

import scala.util.{Failure, Success, Try}

case class Consume()
case class Disconnect()
case class Publish(p: Patient)

class RabbitActor(conn: Connection) extends PatientActor {

    import model._

    implicit val pActor: ActorRef = context.actorOf(PatientActor.props, "PatientActor")

    val workExchange = "books-work-xchg"
    val retryTtlProps = new AMQP.BasicProperties().builder().expiration("30000").build()
    val retryArgs = new java.util.HashMap[String, AnyRef](1) {
        put("x-dead-letter-exchange", workExchange)
    }

    val channel: Channel = conn.createChannel()
    //declare(workExchange, workQueue, routingKey, true)
    channel.basicQos(10, false)
    //declare(retryExchange, retryQueue, routingKey, false, retryArgs)

    override def receive = {
        case GetPatients =>
            val rootsender = sender()
            patientDao.getPatients().onComplete {
                case Success(value: Seq[(Int, String, String, String, String)]) => rootsender ! Patients(value)
                case Failure(exp) => rootsender ! ActionPerformed("Fail")
            }
        case AddPatient(p: FullPatient) =>
            val rootsender = sender()
            patientDao.addPatient(p).onComplete {
                case Success(value) => rootsender ! ActionPerformed(s"Added")
                case Failure(exp) => rootsender ! ActionPerformed(exp.getMessage)
            }
        case GetbyID (id: Int) =>
            val rootsender = sender()
            patientDao.getPatient(id).onComplete{
                case Success(value) => rootsender ! value.headOption
                case Failure(exp) => rootsender ! ActionPerformed("Failed")
            }
        case DeletePatient(id: Int) =>
            val rootsender = sender()
            patientDao.deletePatient(id).onComplete {
                case Success(value: Try[Int]) =>{
                    value match  {
                        case Success(id: Int) => {
                            if (id > 0)
                                rootsender ! ActionPerformed("Deleted")
                            else
                                rootsender ! ActionPerformed("Failed")
                        }
                        case Failure(exp) =>
                            rootsender ! ActionPerformed("Failed")
                    }
                }
                case Failure(exp) => rootsender ! ActionPerformed("Failed")
            }
        case UpdatePatient(id: Int , p : Patient) =>
            val rootsender = sender()
            patientDao.updatePatient(id, p).onComplete{
                case Success(value) => {
                    value match {
                        case Success(id ) => {
                            if (id > 0)
                                rootsender ! ActionPerformed("Updated")
                            else
                                rootsender ! ActionPerformed("Failed")
                        }
                        case Failure(exp) => ActionPerformed(exp.getMessage)
                    }
                }
                case Failure(exp) => rootsender ! ActionPerformed("Failed")
            }
        }

}
