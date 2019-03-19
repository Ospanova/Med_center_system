package routing

import Actors.Patients
import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import org.json4s.DefaultFormats
import routing.PerRequest._
import spray.http.StatusCode
import spray.http.StatusCodes.OK
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration._

trait PerRequest extends Actor with ActorLogging with Json4sSupport {

    import context._

    val json4sFormats = DefaultFormats

    def r: RequestContext
    def target: ActorRef
    def message: RestMessage

    setReceiveTimeout(10.seconds)
    target ! message

    def receive = {
        case Patients(p) => complete(OK, p)
        case ActionPerformed(msg: String) => complete(OK, msg)
        case msg => complete(OK, s"unknown msg = $msg")
    }

    def complete[T <: AnyRef](status: StatusCode, obj: T) = {
        r.complete(status, obj)
        stop(self)
    }

    override val supervisorStrategy =
        OneForOneStrategy() {
            case e =>
                complete(OK, "some error")
                Stop
        }
}


trait PerRequestCreator {
    this: Actor =>

    def perRequest(r: RequestContext, target: ActorRef, message: RestMessage) =
        context.actorOf(Props(WithActorRef(r, target, message)))

    def perRequest(r: RequestContext, props: Props, message: RestMessage) =
        context.actorOf(Props(WithProps(r, props, message)))
}

object PerRequest {
    case class WithActorRef(r: RequestContext, target: ActorRef, message: RestMessage) extends PerRequest

    case class WithProps(r: RequestContext, props: Props, message: RestMessage) extends PerRequest {
        lazy val target = context.actorOf(props)
    }
}