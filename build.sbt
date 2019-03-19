name := "patients"

organization := "one.tech"

version := "0.1"

scalaVersion := "2.11.7"

val akkaVersion = "2.5.19"
val akkaHttpVersion = "10.0.11"
val circleVersion = "0.9.3"
val sprayVersion = "1.3.4"
//
libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
    "com.typesafe.slick" %% "slick" % "3.2.3",
    "com.newmotion" %% "akka-rabbitmq" % "5.0.4-beta",
    "org.json4s" %% "json4s-native" % "3.6.4",
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
    "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.0-M2",
    "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "1.0-M2"
)