import RabbitQ.{ReceiveFromP, SendRabbit}
import actors.DoctorActor._
import actors.{DoctorActor, Doctors}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import model.{Doctor, FullDoctor}
import rabbitmq.SendToP
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import mapping.JsonMappings
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import doctorDAO.DoctorDAO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class Router(implicit val system: ActorSystem, val patientActor: ActorRef) extends JsonMappings {
    // Required by the `ask` (?) method below
    implicit lazy val timeout: Timeout = Timeout(10.seconds) // usually we'd obtain the timeout from the system's configuration

    implicit def myExceptionHandler: ExceptionHandler = ExceptionHandler {
        case _: Exception =>
            /*extractUri { uri =>
              println(s"Request to $uri could not be handled normally")
              complete((StatusCodes.BadRequest, ""))
            }*/
            complete((StatusCodes.BadRequest, ""))
    }

    val route: Route =
        path("doctors") {
              post {
                  entity(as[FullDoctor]) { p =>
                      val patient = (patientActor ? AddDoctor(p)).mapTo[Doctor]
                      onSuccess(patient) { performed =>
                          complete(s"OK ${performed}")
                      }

                  }
              }
        } ~
          (path("doctors"/IntNumber) & get) { id =>
              get {
                  println(s"we are going to get user with id ${id}")
                  val patient = (patientActor ? getbyID(id)).mapTo[Option[Doctor]]
                  SendRabbit(s"""{"action":"getByID","id": ${id}}""").sendMsg()
                  rejectEmptyResponse{
                      complete(patient)
                  }
              }
          } ~
          (path("doctors"/IntNumber) & put) { id => entity(as[Doctor]) { p =>
              println(s"we are going to update user with id ${id}")
              val patient = (patientActor ? UpdateDoctor(id, p)).mapTo[ActionPerformed]
              onSuccess(patient) { performed =>
                  complete(s"Updated")
              }
          } } ~
          (path("doctors"/IntNumber) & delete) { id =>
              println(s"we are going to delete user with id ${id}")
              val patient = (patientActor ? DeleteDoctor(id)).mapTo[ActionPerformed]
              onSuccess(patient) { performed =>
                  complete(s"Deleted")
              }
          }
}

object Boot extends App {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val pActor: ActorRef = system.actorOf(DoctorActor.props, "DoctorActor")

//    val patientRoute = new Router()
//    val bindingFuture = Http().bindAndHandle(patientRoute.route, "localhost", 8080)

    val dao = new DoctorDAO()
   dao.addDoctor(FullDoctor("Dana","DFGH", "fghj", "FGHJK", 888, "DFGHJ")).onComplete {
       case Success(value) => println(value)
       case Failure(exception) => println(exception.getMessage)
   }

    ReceiveFromP().recv("response.doctor.dar")
}
