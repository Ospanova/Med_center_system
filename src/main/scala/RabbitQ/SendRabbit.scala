package RabbitQ

import Actors.PatientActor.GetPatients
import com.rabbitmq.client.ConnectionFactory
import org.json4s.jackson.JsonMethods._

case class SendRabbit(msg: String) {
    private val QUEUE_NAME = "Patient_query"
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    def sendMsg(): Unit ={
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        channel.queueDeclare(QUEUE_NAME, false, false, false, null)
        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes())
        println(" [x] Sent '" + msg + "'")
        channel.close()
        connection.close()
    }
}
