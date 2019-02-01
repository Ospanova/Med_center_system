package mappings

import Actor.PatientActor.ActionPerformed
import model.{FullPatient, Patient}
import spray.json.DefaultJsonProtocol
import Actor._

trait JsonMappings extends DefaultJsonProtocol {
    implicit val patientFormat = jsonFormat5(Patient)
    implicit val fullPatientFormat = jsonFormat4(FullPatient)
    implicit val patientsFormat = jsonFormat1(Patients)
    implicit val ActionPerformedF = jsonFormat1(ActionPerformed)
}