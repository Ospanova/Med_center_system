package dao


import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Actor, Props}
import akka.stream.alpakka.slick.scaladsl._
import model.{FullPatient, Patient}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.{Await, Future}

class PatientDAO () {
    val connectionUrl = "jdbc:postgresql://localhost:5432/med_center?user=postgres&password=uljanek06"
    var allpatients = TableQuery[PatientTable]
    var db = Database.forURL(connectionUrl, driver = "org.postgresql.Driver")
    implicit val session = SlickSession.forConfig("slick-postgres")
    def getPatients(): Future[Seq[Patient]] = db.run(allpatients.result)

    def addPatient(p: FullPatient) = {

        val patient = Patient(None, p.name, p.surname, p.login, p.password)
        println(patient)
        val action =
            (allpatients returning allpatients.map(_.patient_id) += patient).map(id => patient.copy(patient_id = Some(id)))
            //(allpatients returning allpatients.map(_.patient_id) into ((pp, patient_id) => pp.copy(patient_id = Some(patient_id)))) += patient
        db.run(action)

    }
    def getPatient(id: Int) = db.run( allpatients.filter(_.patient_id === id).result)
    def updatePatient(id: Int , newP: Patient) = {

        val action = for { b <- allpatients if b.patient_id === id } yield (b.name, b.surname, b.login, b.password)
        db.run(action.update(newP.name, newP.surname, newP.login, newP.password).asTry)

    }
    def deletePatient(id: Int) = db.run(allpatients.filter(_.patient_id === id).delete.asTry)
}