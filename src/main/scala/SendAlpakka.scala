import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, KillSwitches}
import akka.stream.alpakka.amqp._
import akka.stream.alpakka.amqp.javadsl._
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import scala.concurrent.duration._
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global

class SendAlpakka {
    implicit val system = ActorSystem(this.getClass.getSimpleName + System.currentTimeMillis())
    implicit val materializer = ActorMaterializer()

    val connectionProvider = AmqpLocalConnectionProvider
    val exchangeName = "X:gateway.in.fanout"
    val exchangeDeclaration = ExchangeDeclaration(exchangeName, "fanout")
    val amqpSink = AmqpSink.simple(
        AmqpSinkSettings(connectionProvider)
          .withExchange(exchangeName)
          .withDeclaration(exchangeDeclaration)
    )
    val fanoutSize = 4

//    val mergedSources = (0 until fanoutSize).foldLeft(Source.empty[(Int, String)]) {
//        case (source, fanoutBranch) =>
//            source.merge(
//                AmqpSource
//                  .atMostOnceSource(
//                      TemporaryQueueSourceSettings(
//                          connectionProvider,
//                          exchangeName
//                      ).withDeclaration(exchangeDeclaration),
//                      bufferSize = 1
//                  )
//                  .map(msg => (fanoutBranch, msg.bytes.utf8String))
//            )
//    }
    val completion = Promise[Done]
//    val mergingFlow = mergedSources
//      .viaMat(KillSwitches.single)(Keep.right)
//      .to(Sink.fold(Set.empty[Int]) {
//          case (seen, (branch, element)) =>
//              if (seen.size == fanoutSize) completion.trySuccess(Done)
//              seen + branch
//      })
//      .run()

    system.scheduler.scheduleOnce(5.seconds)(
        completion.tryFailure(new Error("Did not get at least one element from every fanout branch"))
    )

    val dataSender = Source
      .repeat("stuff")
      .viaMat(KillSwitches.single)(Keep.right)
      .map(s => ByteString(s))
      .to(amqpSink)
      .run()

    dataSender.shutdown()
    //mergingFlow.shutdown()
}
