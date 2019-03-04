package AbstractClasses

trait User {
    def name: String
    def surname: String
    def ID: Option[Int]
    def username: String
    def password: String
    def degree: Int = 0
    def number: Long
    def loginValidation(str: String) ={
        if (str.length > 2) {
            for (i <- str)
                if (!i.isLetter)
                    false
            true
        }
        else
            false
    }
    def sendRegistrationToAdmin() {}

    def getDegree() {this.degree}

    override def hashCode(): Int = super.hashCode()
    def login(username_ : String, psw: String): Boolean ={
        if (this.username.equals(username_) && this.password.equals(psw))
            true
        false
    }
}
