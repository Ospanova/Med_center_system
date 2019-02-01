import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec, WordSpecLike}
import akka.http.scaladsl.model.{MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActors, TestKit}
import connection_point.ConnectionActor
import dao.PatientDAO
import model.{FullPatient, Patient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

//class PatientTest extends FunSuite {
//    val patientDao = new PatientDAO()
//    test("Get_Patient_by_7"){
//        val p: Future[Seq[Patient]] = patientDao.getPatient(7)
//        var ok = false
//        p.onComplete{
//            case Success(value) => {
//                if (value.head.equals(Patient(Some(7),"Dikosh", "Ospan", "asdf", "asdfg"))) {
//                    ok = true
//                }
//                assert(ok)
//            }
//            case Failure(exp) => println(exp.getMessage)
//        }
//    }
//    test("Update_Patient") {
//        val oldP = patientDao.getPatient(7);
//        val p = patientDao.updatePatient(7, Patient(Some(7), "Kura", "Ospan", "asdf", "asdfg"))
//        var ok = false
//        p.onComplete {
//            case Success(value) => {
//                oldP.onComplete {
//                    case Success(value) => {
//                        val c = patientDao.updatePatient(7, value.head)
//                        c.onComplete{
//                            case Success(value) => {
//                                ok = true
//                                assert(ok)
//                            }
//                            case Failure(exp) => println("ERROR")
//                        }
//                        //.ready(c, 5.seconds)
//                    }
//                    case Failure(exp) => println(exp.getMessage)
//                }
//            }
//            case Failure(exp) => {
//                println("UPDATE_ERROR")
//            }
//        }
//
//    }
//    test("Delete_Patient") {
//        val oldP = patientDao.getPatient(7)
//        val cur = patientDao.deletePatient(7)
//        var ok = false
//        cur.onComplete {
//            case Success(value) => {
//                oldP.onComplete {
//                    case Success(p) => {
//                        val fP = FullPatient(p.head.name, p.head.surname, p.head.login, p.head.password)
//                        val c = patientDao.addPatient(fP)
//                        //Await.ready(c, 5.seconds)
//                        ok = true
//                        assert(ok)
//                    }
//                    case Failure(exp) => {
//                        ok = false
//                        println(exp.getMessage)
//                    }
//                }
//            }
//            case Failure(exp) => println(exp.getMessage)
//        }
//
//    }

//}

class AkkaTest(_system: ActorSystem)  extends TestKit(_system)
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
    import ConnectionActor._
    def this() = this(ActorSystem("AkkaTestSpec"))
    val pActor = system.actorOf(ConnectionActor.props, "PatientActor")
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
                pActor ! DeletePatient(16)
                expectMsg(ActionPerformed("Deleted"))
            }
        }
        "doesn't delete patient " in {
            within(500 millis) {
                pActor ! DeletePatient(111)
                expectMsg(ActionPerformed("Failed"))
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
