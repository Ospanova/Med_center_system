package rabbitmq

import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import com.rabbitmq.client.{AMQP, ConnectionFactory, DefaultConsumer, Envelope}

class RabbitMQClient {
    private val requestQueueName = "rpc_queue"

    def createChannel = {
        implicit val system = ActorSystem("ActorSystem")

        val factory = new ConnectionFactory()
        factory.setHost("localhost")

        val connection = factory.newConnection
        connection.createChannel
    }

    def call(message: String) = {
        val channel = createChannel
        val replyQueueName: String = channel.queueDeclare.getQueue
        println(s"Reply queue: $replyQueueName")

        val corrId = UUID.randomUUID.toString
        println(s"Correlation Id: $corrId")

        val props = new AMQP.BasicProperties.Builder()
          .correlationId(corrId)
          .replyTo(replyQueueName)
          .build

        channel.basicPublish("",
            requestQueueName,
            props,
            message.getBytes("UTF-8")
        )

        val response: ArrayBlockingQueue[String] = new ArrayBlockingQueue[String](1)

        val ctag = channel.basicConsume(replyQueueName,
            true, new DefaultConsumer(channel) {
                override def handleDelivery(consumerTag: String,
                                            envelope: Envelope,
                                            properties: AMQP.BasicProperties,
                                            body: Array[Byte]): Unit = {
                    if (properties.getCorrelationId == corrId) response.offer(new String(body, "UTF-8"))
                }
            })

        val result: String = response.take
        println(s"Message finally received by Client App: $result")
        channel.basicCancel(ctag)
    }
}
