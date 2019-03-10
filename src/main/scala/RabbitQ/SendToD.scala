package RabbitQ

import com.rabbitmq.client.Channel
import com.rabbitmq.client.{Connection, ConnectionFactory}

case class SendToD(connection: Connection) {
    val workExchange = "X:gateway.in.fanout"
    val routingKey = "request.doctor.#"

    def declare(channel: Channel, exchangeName: String, routingKeyName: String, durable: Boolean, args: java.util.HashMap[String, AnyRef] = null): Unit = {
        channel.exchangeDeclare(exchangeName, "fanout", durable)
    }

    def sendMsg(msg: String): Unit = {
        val channel: Channel = connection.createChannel()
        declare(channel,workExchange, routingKey, true)
        channel.basicPublish(workExchange, routingKey, null, msg.getBytes())
        println(" [x] Sent '" + msg + "'")
        channel.close()
    }
}

object SendToD {

    def createSendToD(): SendToD = {
        val factory: ConnectionFactory = new ConnectionFactory();
        return new SendToD(factory.newConnection("localhost"));
    }

}
