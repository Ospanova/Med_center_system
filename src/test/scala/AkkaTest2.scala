import Actors.PatientActor
import Actors.PatientActor._
import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import dao.PatientDAO
import model.{FullPatient, Patient}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class AkkaTest2 extends  TestKit(ActorSystem()) with ImplicitSender
  with WordSpecLike with Matchers {

    implicit lazy val timeout: Timeout = Timeout(10.seconds) // usually we'd obtain the timeout from the system's configuration

    val pActor: ActorRef = system.actorOf(PatientActor.props, "PatientActor")
    val patientDao = new PatientDAO()
    val testP = FullPatient("Ulzhan", "Ospan", "uljanek", "12345")
    val addPatient: Future[Patient] = (pActor ? AddPatient(testP)).mapTo[Patient]
    val patient = Await.result(addPatient, 10.seconds)
    patient.patient_id match {
        case Some(id) => {
            "An ConnectionActor" should {
                "get patient by ID " in {
                    within(500 millis) {
                        val getAction = pActor ? GetbyID(id)
                        getAction.onComplete {
                            case Success(patient) => {
                                expectMsg(Patient(Some(id), testP.surname, testP.surname, testP.login, testP.password))}
                            case Failure(exp) => expectMsg(ActionPerformed("Failed"))
                        }
                    }
                }
                "update patient " in {
                    within(500 millis) {

                        val updAction = pActor ? UpdatePatient(id, Patient(Some(id), "Dikosh", "Murzekenova", "dimur", "12345"))
                        updAction.onComplete {
                            case Success(value) =>
                                expectMsg(ActionPerformed("Updated"))
                            case Failure(exception) =>
                                expectMsg(exception.getMessage)
                        }
                    }
                }
                "delete patient" in {
                    within(500 millis) {
                        val delAction = pActor ? DeletePatient(id)
                        delAction.onComplete {
                            case Success(value) => expectMsg(ActionPerformed("Deleted"))
                            case Failure(exp) => expectMsg(exp.getMessage)
                        }
                    }
                }
            }
        }
        case _ => {
            println("Matching is not correct")
        }
    }


}
