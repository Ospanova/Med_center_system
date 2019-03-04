package mappings

import Actors.PatientActor.ActionPerformed
import model.{FullPatient, Patient}
import spray.json.DefaultJsonProtocol
import Actors._

trait JsonMappings extends DefaultJsonProtocol {
    implicit val patientFormat = jsonFormat7(Patient)
    implicit val fullPatientFormat = jsonFormat6(FullPatient)
    implicit val patientsFormat = jsonFormat1(Patients)
    implicit val ActionPerformedF = jsonFormat1(ActionPerformed)

}