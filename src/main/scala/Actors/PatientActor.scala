package Actors

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import akka.actor.{Actor, ActorLogging, Props}
import dao.PatientDAO
import mapping.SerDesObjects
import model.{FullPatient, Patient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

final case class Patients(patients: Seq[Patient])
object PatientActor {
  def props = Props[PatientActor]
  case class AddPatient(p: FullPatient)
  case class GetPatients()
  case class AddPatientTest(p: Patient)
  case class GetbyID(patient_id: Int)
  case class UpdatePatient( id: Int, newP: Patient)
  case class DeletePatient(id: Int)
  final case class ActionPerformed(description: String) {
      def getMsg() = this.description
  }
  final case class OpFailed (msg: String)

}
class PatientActor extends Actor with ActorLogging {
  import PatientActor._

  val patientDao = new PatientDAO()
  var patients = Set.empty[Patient]
  override def receive: Receive = {
    case GetPatients =>
      val rootsender = sender()
      patientDao.getPatients().onComplete {
          case Success(value: Seq[(Int, String, String, String, String)]) => rootsender ! Patients(value)
          case Failure(exp) => rootsender ! ActionPerformed("Fail")
      }
    case AddPatient(p: FullPatient) =>
        val rootsender = sender()
          patientDao.addPatient(p).onComplete {
              case Success(value) => rootsender ! value
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
