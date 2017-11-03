package irk.client


import com.typesafe.config.ConfigFactory
import irk.http.{Request, RequestContainer}
import irk.utils.Instrumented
import irk.utils.clients.{IrkClient, SttpClient}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpClient (timeout: Duration, sequenced: Boolean = false)(implicit numOfConnections: ExecutionContext) extends Instrumented {
    
    val connectionTimeout: java.time.Duration = ConfigFactory.load().getDuration("irk.client.connectionTimeout")
    // client to use
    val client : IrkClient = new SttpClient(connectionTimeout)
    
    val FINISHED = true
    
    val shouldRun: Boolean = true
    
    val duration: Deadline = FiniteDuration(timeout._1,timeout._2).fromNow
    
    
    def sendRequest(request: Request): Future[Int] = {
        metrics.counter("requestCount").inc()
        client.sendRequest(request)
    }
    
    /**
      * main irk.client function. start all requests until time ends
      */
    def run: Future[Boolean] =
        if (sequenced)
            sendSequenceOrdered
        else
            sendSequenceParallel
    
    
    
    
    private def sendSequenceParallel: Future[Boolean] = Future {
        while (duration.hasTimeLeft()) {
            sendRequest(RequestContainer.getNextRequest).onComplete {
                case Success(responseCode) =>
                    metrics.meter(s"$responseCode").mark()
                case Failure(e) =>
                    metrics.meter(s"${e.getMessage}").mark()
                    metrics.counter("requestCount").dec()
            }
        }
        client.close
        FINISHED
    }
    
    
    private def sendSequenceOrdered: Future[Boolean] = Future {
        var sequenceEnd = true
        while (duration.hasTimeLeft()) {
            if (sequenceEnd) {
                sequenceEnd = false
                RequestContainer.getAsList.foldLeft(Future(List.empty[Int]))((prevFuture,
                currentTuple) => {
                    for {
                        prev <- prevFuture
                        curr <- (sendRequest _).apply(currentTuple)
                    } yield prev :+ curr
                })
            }.onComplete {
                case Success(responseList) =>
                    responseList.foreach { responseCode =>
                        metrics.meter(s"$responseCode").mark()
                    }
                    
                    sequenceEnd = true
                case Failure(e) =>
                    metrics.meter(s"${e.getMessage}").mark()
                    metrics.counter("requestCount").dec()
            }
        }
        client.close
        FINISHED
    }
    
    
}

