import java.util.concurrent.TimeUnit

import irk.client.ClientManager
import irk.utils.{Instrumented, Metrics, ScoptArgsParser}

/**
  * main runner class. use scopt [https://github.com/scopt/scopt] in order to parse app arguments
  */
object Runner extends App with Instrumented {
    
    private[this] val runTime = metrics.timer("running.time")
    
    val argsParser = new ScoptArgsParser
    if (argsParser.parseArgs(args)) {
        runTime.time {
            ClientManager.runClients()
        }
        val elapsedTimeInSeconds = TimeUnit.NANOSECONDS.toSeconds(runTime.max)
        println(s"${Metrics.sumMeters() / elapsedTimeInSeconds} REQ/SEC")
    } else {
        System.exit(0)
    }
    
}
