package model
import model.Doctor
import slick.jdbc.PostgresProfile.api._

class DoctorTable(tag: Tag) extends Table[Doctor](tag, "doctors") {
        def ID = column[Int]("doctor_id", O.PrimaryKey, O.AutoInc)
        def name = column[String]("name")
        def surname = column[String]("surname")
        def username= column[String]("login")
        def password = column[String]("password")
        def number = column[Long]("number")
        def stringCalendar = column[String]("stringCalendar")
        def * = (ID.?, name, surname, username, password, number, stringCalendar) <> (Doctor.tupled, Doctor.unapply)
        def idx = index("unique_id", ID, unique = true)
}
