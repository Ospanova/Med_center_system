package RabbitQ

import Actors.PatientActor.GetPatients
import com.rabbitmq.client.AMQP.Channel
import com.rabbitmq.client.{AMQP, ConnectionFactory}
import org.json4s.jackson.JsonMethods._

case class SendToD() {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val workExchange = "X:gateway.in.fanout"
    val routingKey = "request.doctor.#"

    def sendMsg(msg: String): Unit ={
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        declare(workExchange, routingKey, true)
        def declare(exchangeName: String, routingKeyName: String, durable: Boolean, args: java.util.HashMap[String, AnyRef] = null): Unit = {
            channel.exchangeDeclare(exchangeName, "fanout", durable)
        }

        channel.basicPublish("X:gateway.in.fanout",routingKey, null, msg.getBytes())
        println(" [x] Sent '" + msg + "'")
        channel.close()
        connection.close()
    }
}
