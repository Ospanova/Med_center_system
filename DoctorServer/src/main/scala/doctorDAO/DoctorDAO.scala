package doctorDAO

import akka.stream.alpakka.slick.scaladsl._
import model.{Doctor, DoctorTable, FullDoctor}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._

class DoctorDAO {
    val connectionUrl = "jdbc:postgresql://localhost:5432/med_center?user=postgres&password=uljanek06"
    var alldoctors = TableQuery[DoctorTable]
    var db = Database.forURL(connectionUrl, driver = "org.postgresql.Driver")
    implicit val session = SlickSession.forConfig("slick-postgres")

    def getDoctors(): Future[Seq[Doctor]] = db.run(alldoctors.result)

    def addDoctor(p: FullDoctor) = {

        val patient = Doctor(None, p.name, p.surname, p.username, p.password, p.number, p.stringCalendar)
        println(patient)
        val action =
            (alldoctors returning alldoctors.map(_.ID) += patient).map(id => patient.copy(ID = Some(id)))

        //(allpatients returning allpatients.map(_.patient_id) into ((pp, patient_id) => pp.copy(patient_id = Some(patient_id)))) += patient
        db.run(action)

    }
    def getDoctor(id: Int) = db.run( alldoctors.filter(_.ID === id).result)
    def updateDoctor(id: Int, newP: Doctor) = {

        val action = for { b <- alldoctors if b.ID === id } yield (b.name, b.surname, b.username, b.password, b.number, b.stringCalendar)
        db.run(action.update(newP.name, newP.surname, newP.username, newP.password, newP.number, newP.stringCalendar).asTry)

    }
    def deleteDoctor(id: Int) = db.run(alldoctors.filter(_.ID === id).delete.asTry)

}
