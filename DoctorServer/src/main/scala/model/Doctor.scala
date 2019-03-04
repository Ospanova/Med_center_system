package model

import AbstractClasses.{Day, Session, User, WeekCalenderD}


case class Doctor (ID: Option[Int] = None, name: String, surname : String,
                   username: String = "+", password: String = "+", number: Long, stringCalendar: String) extends  User {


    //val cal: WeekCalenderD = WeekCalenderD()

    override def hashCode(): Int = {
        val prime = 31
        var result = 0
        result += prime * (if (name == null) 1 else name.hashCode())
        result += prime * (if (surname == null) 1 else surname.hashCode())
        result += prime * (if (username == null) 1 else username.hashCode())
        result += (prime * (if (password == null) 1 else password.hashCode()))
        super.hashCode() + result
    }
    def canEqual(cur: Any) = cur.isInstanceOf[Patient]
//    def toCalendar () : Unit = {
//        val list: Array[String] = this.stringCalendar.split("|")
//        for (i <- list) {
//            val oneday: Array[String] = i.split("&")
//            this.cal.schedule :+ (Day(oneday(0).toInt), Session(oneday(0).toInt, oneday(1).toInt))
//        }
//    }
    override def equals(obj: Any): Boolean = {
        if (canEqual(obj)&& obj.hashCode() == this.hashCode())
            true
        false
    }

}
case class FullDoctor (name: String, surname: String, username: String, password: String, number: Long, stringCalendar: String)

