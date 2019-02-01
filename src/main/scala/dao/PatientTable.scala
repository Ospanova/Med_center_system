package dao
import model.Patient
import slick.jdbc.PostgresProfile.api._

class PatientTable(tag: Tag) extends Table[Patient](tag, "patients") {
    def patient_id = column[Int]("patient_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def surname = column[String]("surname")
    def login= column[String]("login")
    def password = column[String]("password")
    def * = (patient_id.?, name, surname, login, password) <> (Patient.tupled, Patient.unapply)
    def idx = index("unique_id", patient_id, unique = true)
}