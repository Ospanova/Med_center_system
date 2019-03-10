package  routing

trait RestMessage

case class GetPatients() extends RestMessage

case class Error(message: String)

case class Validation(message: String)

