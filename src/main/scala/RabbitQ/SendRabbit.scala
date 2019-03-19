package RabbitQ

import com.rabbitmq.client.AMQP.Channel
import com.rabbitmq.client.{AMQP, ConnectionFactory}

case class SendRabbit(msg: String) {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val workExchange = "X:gateway.in.fanout"
    val routingKey = "response.patient.#"

    def sendMsg(): Unit ={
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        declare(workExchange, routingKey, true)
        def declare(exchangeName: String, routingKeyName: String, durable: Boolean, args: java.util.HashMap[String, AnyRef] = null): Unit = {
            channel.exchangeDeclare(exchangeName, "fanout", durable)
        }

        channel.basicPublish("X:gateway.in.fanout","routing_key", null, msg.getBytes())
        println(" [x] Sent '" + msg + "'")
        channel.close()
        connection.close()
    }
}
