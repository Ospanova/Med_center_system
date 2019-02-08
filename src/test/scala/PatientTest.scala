import org.scalatest._
import akka.http.scaladsl.model.{MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActors, TestKit}
import Actors.PatientActor
import dao.PatientDAO
import model.{FullPatient, Patient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class PatientTest extends FunSuite {
    val patientDao = new PatientDAO()
    var ind = -1
    val patients = patientDao.getPatients()
    patients.onComplete {
        case Success(pts) => {

            test("Get patient by 7") {
                if (!pts.isEmpty) {
                    val p: Future[Seq[Patient]] = patientDao.getPatient(pts.last.patient_id.getOrElse(-1))
                    var ok = false
                    println(p)
                    p.onComplete {
                        case Success(value) => {
                            if (!value.isEmpty)
                                if (value.head.equals(pts.last)) {
                                    ok = true
                                }
                            assert(ok)
                        }
                        case Failure(exp) => assert(false)
                    }
                }
                else
                    assert(true)
            }

            test("Add new patient") {
                val newP = FullPatient("Ulzhan", "Ospan", "uljanek", "12345")
                val addAction: Future[Patient] = patientDao.addPatient(newP)
                addAction.onComplete {
                    case Success(value) => {
                        assert(true)
                        ind = value.patient_id.getOrElse(-1)
                    }
                    case Failure(exp) => assert(false)
                }
            }
            test("Delete_Patient") {
                val cur: Future[Try[Int]] = patientDao.deletePatient(ind)
                var ok = false
                cur.onComplete {
                    case Success(value) => {
                        value match {
                            case Success(id) => {
                                if (id > 0)
                                    ok = true
                                assert(ok)
                            }
                            case Failure(exp) => assert(false)
                        }

                    }
                    case Failure(exp) => assert(false)
                }

            }
            test("Update patient") {
                if (!pts.isEmpty) {
                    val id = pts.last.patient_id.getOrElse(-1)
                    val oldP = patientDao.getPatient(id);
                    val p = patientDao.updatePatient(id, Patient(Some(id), "Kura", "Ospan", "asdf", "asdfg"))
                    p.onComplete {
                        case Success(value) => {
                            oldP.onComplete {
                                case Success(value) => {
                                    val c = patientDao.updatePatient(id, value.head)
                                    c.onComplete {
                                        case Success(value) => {
                                            assert(true)
                                        }
                                        case Failure(exp) => assert(false)
                                    }
                                    //.ready(c, 5.seconds)
                                }
                                case Failure(exp) => assert(false)
                            }
                        }
                        case Failure(exp) => assert(false)
                    }
                }
                else
                    assert(true)
            }
        }
    }

}

