package mapping
import model.{Doctor, FullDoctor }
import spray.json.DefaultJsonProtocol
import actors._

trait JsonMappings extends DefaultJsonProtocol {
    implicit val doctorFormat = jsonFormat7(Doctor)
    implicit val fullPatientFormat = jsonFormat6(FullDoctor)
    //implicit val doctorsFormat = jso(Doctors) ?? mapping toJson object

}