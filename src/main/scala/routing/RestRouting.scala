package routing

import Actors.PatientManagerActor
import spray.httpx.Json4sSupport
import spray.routing.{HttpServiceActor, Route}
import akka.actor.{ActorRef, ActorSystem, Props}
import org.json4s.{DefaultFormats, Formats}

object RestRouting {
    def props(): Props = Props(new RestRouting)
}

class RestRouting extends HttpServiceActor with PerRequestCreator with Json4sSupport {
    override def actorRefFactory = context
//    val patientActor : ActorRef = actorRefFactory.actorOf(,  "postgresql-patient-actor")

    def receive =
        runRoute(route)


    val route: Route = {
        get {
            path("patients") {
                getPatientHandler(GetPatients())
            }
        }
    }

    def getPatientHandler(message : RestMessage): Route = { ctx =>
        perRequest(ctx, PatientManagerActor.props, message)
    }
    override implicit def json4sFormats: Formats = DefaultFormats
}