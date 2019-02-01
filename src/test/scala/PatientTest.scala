import org.scalatest._
import akka.http.scaladsl.model.{MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActors, TestKit}
import Actor.PatientActor
import dao.PatientDAO
import model.{FullPatient, Patient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class PatientTest extends FunSuite {
    val patientDao = new PatientDAO()
    test("Get_Patient_by_7"){
        val p: Future[Seq[Patient]] = patientDao.getPatient(7)
        var ok = false
        p.onComplete{
            case Success(value) => {
                if (value.head.equals(Patient(Some(7),"Dikosh", "Ospan", "asdf", "asdfg"))) {
                    ok = true
                }
                assert(ok)
            }
            case Failure(exp) => println(exp.getMessage)
        }
    }
    test("Update_Patient") {
        val oldP = patientDao.getPatient(7);
        val p = patientDao.updatePatient(7, Patient(Some(7), "Kura", "Ospan", "asdf", "asdfg"))
        var ok = false
        p.onComplete {
            case Success(value) => {
                oldP.onComplete {
                    case Success(value) => {
                        val c = patientDao.updatePatient(7, value.head)
                        c.onComplete{
                            case Success(value) => {
                                ok = true
                                assert(ok)
                            }
                            case Failure(exp) => println("ERROR")
                        }
                        //.ready(c, 5.seconds)
                    }
                    case Failure(exp) => println(exp.getMessage)
                }
            }
            case Failure(exp) => {
                println("UPDATE_ERROR")
            }
        }

    }
    test("Delete_Patient") {
        val oldP = patientDao.getPatient(7)
        val cur = patientDao.deletePatient(7)
        var ok = false
        cur.onComplete {
            case Success(value) => {
                oldP.onComplete {
                    case Success(p) => {
                        val fP = FullPatient(p.head.name, p.head.surname, p.head.login, p.head.password)
                        val c = patientDao.addPatient(fP)
                        //Await.ready(c, 5.seconds)
                        ok = true
                        assert(ok)
                    }
                    case Failure(exp) => {
                        ok = false
                        println(exp.getMessage)
                    }
                }
            }
            case Failure(exp) => println(exp.getMessage)
        }

    }

}

