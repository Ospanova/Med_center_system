import Actors.PatientActor
import Actors.PatientActor.{ActionPerformed, AddPatient, DeletePatient, UpdatePatient}
import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import dao.PatientDAO
import model.{FullPatient, Patient}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
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
                pActor ! DeletePatient(23)
                expectMsg(ActionPerformed("Deleted"))
            }
        }
        "doesn't delete patient " in {
            within(500 millis) {
                pActor ! DeletePatient(111)
                expectMsg(ActionPerformed("Failed"))
            }
        }
        "get patient by ID " in {
            within(500 millis) {

            }
        }
        "update patient " in  {
            within(500 millis) {
                val oldP = patientDao.getPatient(8);
                pActor ! UpdatePatient(12, Patient(Some(8), "Dikosh", "Murzekenova", "dimur", "12345"))
                oldP.onComplete {
                    case Success(value) => {
                        pActor ! UpdatePatient(8, value.head)
                    }
                    case Failure(exception) => println(exception.getMessage)
                }
                expectMsg(ActionPerformed("Failed"))
            }
        }

    }
}
