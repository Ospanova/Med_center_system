import Actors.PatientActor
import Actors.PatientActor.{ActionPerformed, AddPatient, DeletePatient, UpdatePatient}
import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import dao.PatientDAO
import model.{FullPatient, Patient}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global

class AkkaTest(_system: ActorSystem)  extends TestKit(_system)
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
    import PatientActor._
    def this() = this(ActorSystem("AkkaTestSpec"))
    val pActor = system.actorOf(PatientActor.props, "PatientActor")
    override def afterAll {
        shutdown()
    }
    val patientDao = new PatientDAO()
    var ind = -1
    val patients = patientDao.getPatients()
    patients.onComplete {
        case Success(pts) => {
            "An ConnectionActor" should {
                val patientDao = new PatientDAO()
                "return patients in getting request" in {
                    within(500 millis) {
                        pActor ! AddPatient(FullPatient("Ulzhan", "Ospan", "asd", "asdf"))
                        expectMsg(ActionPerformed("Added"))
                    }
                }
                "delete patient " in {
                    within(500 millis) {
                        if (!pts.isEmpty) {
                            pActor ! DeletePatient(pts.last.patient_id.getOrElse(-1))
                            expectMsg(ActionPerformed("Deleted"))
                        }
                        else
                            expectMsg(ActionPerformed("Failed"))
                    }
                }
                "doesn't delete patient " in {
                    within(500 millis) {
                        if (!pts.isEmpty) {
                            pActor ! DeletePatient(pts.last.patient_id.getOrElse(-2) + 1)
                            expectMsg(ActionPerformed("Failed"))
                        }
                        else
                            expectMsg(ActionPerformed("Failed"))
                    }
                }
                "get patient by ID " in {
                    within(500 millis) {
                        val id = pts.last.patient_id.getOrElse(-1)
                        if (!pts.isEmpty) {
                            pActor ! GetbyID(id)
                            expectMsg(pts.last)
                        }
                    }
                }
                "update patient " in {
                    within(500 millis) {
                        if (!pts.isEmpty) {
                            val id = pts.last.patient_id.getOrElse(-1)
                            val oldP = patientDao.getPatient(id);

                            pActor ! UpdatePatient(id, Patient(Some(id), "Dikosh", "Murzekenova", "dimur", "12345"))

                            oldP.onComplete {
                                case Success(value) => {
                                    pActor ! UpdatePatient(id, value.head)
                                    expectMsg(ActionPerformed("Updated"))
                                }
                                case Failure(exception) => expectMsg(ActionPerformed("Failed"))
                            }

                        }
                        else
                            expectMsg(ActionPerformed("Failed"))
                    }
                }

            }
        }
    }
}
