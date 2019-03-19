package realization

import akka.actor.ActorSystem
import akka.io.IO
import spray.can.Http
import akka.util.Timeout
import routing.RestRouting
import scala.concurrent.duration._


object Boot2 extends App {
    implicit val system = ActorSystem("apr-demo")
    implicit val timeout = Timeout(5.seconds)
    val serviceActor = system.actorOf(RestRouting.props(), name = "rest-routing")

//    system.registerOnTermination {
//        system.log.info("Actor per request demo shutdown.")
//    }
    //val alp = new AlpakkaAMQPPublisher()

    IO(Http) ! Http.Bind(serviceActor, "localhost", port = 8080)
}
