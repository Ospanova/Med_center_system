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
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion, 
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "io.circe" %% "circe-core" % circleVersion,
    "io.circe" %% "circe-generic" % circleVersion,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "io.circe" %% "circe-parser" % circleVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
    "com.typesafe.slick" %% "slick" % "3.2.3",
    "ch.qos.logback"      %  "logback-classic"                      % "1.1.2" % "test",
    "junit"           	  % "junit"                                 % "4.12" % "test",
    "com.newmotion" %% "akka-rabbitmq" % "5.0.4-beta",
    "org.json4s" %% "json4s-native" % "3.6.4",
    "org.json4s" %% "json4s-jackson" % "3.6.4",
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
    "org.flywaydb" % "flyway-core" % "3.2.1",
    "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.0-M2",
    "com.typesafe.akka" %% "akka-http-caching" % "10.1.7",
    "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "1.0-M2"
)