package model

case class Patient (patient_id: Option[Int] = None, name: String, surname : String,
                    login: String, password: String) {
    override def hashCode(): Int = {
        val prime = 31
        var result = 0
        result += prime * (if (name == null) 0 else name.hashCode())
        result += prime * (if (surname == null) 0 else surname.hashCode())
        result += prime * (if (login == null) 0 else login.hashCode())
        result += prime * (if (password == null) 0 else password.hashCode())
        result
    }
    def canEqual(cur: Any) = cur.isInstanceOf[Patient]
    override def equals(obj: Any): Boolean = {
        if (canEqual(obj) && obj.hashCode() == this.hashCode())
            true
        false
    }
}
case class FullPatient (name: String, surname: String, login: String, password: String)

