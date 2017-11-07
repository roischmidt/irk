package irk.utils

import scala.concurrent.duration.{Deadline, Duration, FiniteDuration}

object TimeUtils {
    
    def runUntil[A](duration: Duration)(f: => A) : Unit = {
        val deadLine: Deadline = FiniteDuration(duration._1,duration._2).fromNow
        while (deadLine.hasTimeLeft()) {
            f
        }
    }
    
}
