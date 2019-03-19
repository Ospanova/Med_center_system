package alpakka
import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.alpakka.amqp._
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape, KillSwitches}
import akka.util.ByteString
import akka.stream.alpakka.amqp._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._

class AlpakkaAMQPPublisher {
    implicit val system = ActorSystem()
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

    val mergedSources: Source[(Int, String), NotUsed] = (0 until fanoutSize).foldLeft(Source.empty[(Int, String)]) {
        case (source, fanoutBranch) =>
            source.merge(
                AmqpSource
                  .atMostOnceSource(
                      TemporaryQueueSourceSettings(
                          connectionProvider,
                          exchangeName
                      ).withDeclaration(exchangeDeclaration),
                      bufferSize = 1
                  )
                  .map(msg => (fanoutBranch, msg.bytes.utf8String))
            )
    }


    val completion = Promise[Done]
    val mergingFlow = mergedSources
      .viaMat(KillSwitches.single)(Keep.right)
      .to(Sink.fold(Set.empty[Int]) {
          case (seen, (branch, element)) =>
              if (seen.size == fanoutSize) completion.trySuccess(Done)
              seen + branch
      })
      .run()

    system.scheduler.scheduleOnce(5.seconds)(
        completion.tryFailure(new Error("Did not get at least one element from every fanout branch"))
    )
//
    val dataSender = Source
      .repeat("stuff")
      .viaMat(KillSwitches.single)(Keep.right)
      .map(s => ByteString(s))
      .to(amqpSink)
      .run()

   // dataSender.shutdown()
    //mergingFlow.shutdown()
}
