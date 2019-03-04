package actors

import akka.actor.{Actor, ActorLogging, Props}
import model.{Doctor, FullDoctor}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import doctorDAO.DoctorDAO
object Doctors {

    var doctors: Vector[Doctor] = Vector()
}

object DoctorActor {
    def props = Props[DoctorActor]
    case class getbyID(id: Int)
    case class AddDoctor(d: FullDoctor)
    case class UpdateDoctor( id: Int, newD: Doctor)
    case class DeleteDoctor(id: Int)
    final case class ActionPerformed(description: String) {
        def getMsg() = this.description
    }
    final case class OpFailed (msg: String)
}
class DoctorActor extends Actor with ActorLogging {
    import DoctorActor._
    val doctorDAO = new DoctorDAO()
    override def receive: Receive = {
        case AddDoctor(p: FullDoctor) =>
            val rootsender = sender()
            doctorDAO.addDoctor(p).onComplete {
                case Success(value) => rootsender ! value
                case Failure(exp) => rootsender ! ActionPerformed(exp.getMessage)
            }
        case getbyID(id: Int) =>
            val rootsender = sender()
            doctorDAO.getDoctor(id).onComplete {
                case Success(value: Seq[Doctor]) => rootsender ! value.headOption
                case Failure(exp) => rootsender ! ActionPerformed("Failed")
            }
        case DeleteDoctor(id: Int) =>
            val rootsender = sender()
            doctorDAO.deleteDoctor(id).onComplete {
                case Success(value: Try[Int]) => {
                    value match {
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
        case UpdateDoctor(id: Int, p: Doctor) => {
            val rootsender = sender()
            doctorDAO.updateDoctor(id, p).onComplete {
                case Success(value: Try[Int]) => {
                    value match {
                        case Success(id) => {
                            if (id > 0) {
                                rootsender ! ActionPerformed("Updated")
                            }
                            else
                                rootsender ! ActionPerformed("Failed")
                        }
                        case Failure(exp) => ActionPerformed("Failed")
                    }
                }
                case Failure(exp) => rootsender ! ActionPerformed("Failed")
            }
        }

    }
}
