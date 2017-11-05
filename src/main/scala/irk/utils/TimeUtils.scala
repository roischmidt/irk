package irk.utils

import scala.concurrent.duration.{Duration, FiniteDuration}

object TimeUtils {
    
    def runUntil[A](duration: Duration)(f: => A) : Unit = {
        val deadLine = FiniteDuration(duration._1,duration._2).fromNow
        while (deadLine.hasTimeLeft()) {
            f
        }
    }
    
}
