package mappings

import model.{FullPatient, Patient}
import spray.json.DefaultJsonProtocol
import Actors._
import routing.ActionPerformed

trait JsonMappings extends DefaultJsonProtocol {
    implicit val patientFormat = jsonFormat7(Patient)
    implicit val fullPatientFormat = jsonFormat6(FullPatient)
    implicit val patientsFormat = jsonFormat1(Patients)
    implicit val ActionPerformedF = jsonFormat1(ActionPerformed)

}