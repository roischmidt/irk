import java.util.concurrent.TimeUnit

import irk.client.ClientManager
import irk.config.IrkConfig
import irk.utils.{Instrumented, Metrics, ScoptArgsParser}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * main runner class. use scopt [https://github.com/scopt/scopt] in order to parse app arguments
  */
object Runner extends App with Instrumented {
    
    private[this] val runTime = metrics.timer("running.time")
    
    val argsParser = new ScoptArgsParser
    if (argsParser.parseArgs(args)) {
        runTime.time {
            if (!Await.result(ClientManager.runClients(), IrkConfig.conf.duration.plus(1.seconds))) // blocking method
                System.exit(0)
        }
        val elapsedTimeInSeconds = IrkConfig.conf.duration.toSeconds//TimeUnit.NANOSECONDS.toSeconds(runTime.max)
        val requestCount = Metrics.getCounterBySimpleName("requestCount").get.getCount
        println(s"Total requests: $requestCount")
        println(s"${ requestCount / elapsedTimeInSeconds} REQ/SEC")
    }
    System.exit(0)
    
    
    def secondsToString(millis: Long): java.lang.String = {
        val hrs = MILLISECONDS.toHours(millis) % 24
        val min = MILLISECONDS.toMinutes(millis) % 60
        val sec = MILLISECONDS.toSeconds(millis) % 60
        val mls = millis % 1000
        
        f"$hrs%02d:$min%02d:$sec%02d ($mls)"
    }
    
    
}
