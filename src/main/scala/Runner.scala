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
            Await.result(ClientManager.runClients(),Duration(IrkConfig.conf.duration + 1,TimeUnit.SECONDS)) // blocking method
        }
        println(s"ran for ${runTime.max}")
        val elapsedTimeInSeconds = TimeUnit.NANOSECONDS.toSeconds(runTime.max)
        println(s"${Metrics.sumMeters() / elapsedTimeInSeconds} REQ/SEC")
    } else {
        System.exit(0)
    }
    
}
