package irk.client

import java.time
import java.util.concurrent.{Executors, TimeUnit}

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.typesafe.config.ConfigFactory
import irk.http.{Method, Request, RequestContainer}
import irk.utils.Instrumented

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpClient(numOfThreads: Int, timeoutInSeconds: Long, sequenced: Boolean = false) extends Instrumented {
    
    val FINISHED = true
    
    implicit val clientExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(numOfThreads)
    )
    
    val shouldRun: Boolean = true
    
    val connectionTimeout: time.Duration = ConfigFactory.load().getDuration("irk.sttp.connectionTimeout")
    
    implicit val sttpBackend: SttpBackend[Future, Nothing] = AsyncHttpClientFutureBackend(
        connectionTimeout = FiniteDuration(connectionTimeout.toMillis, TimeUnit.MILLISECONDS)
    )(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor()))
    
    val duration: Deadline = timeoutInSeconds.seconds.fromNow
    
    
    def sendRequest(request: Request): Future[Response[String]] = {
        val req = request.method match {
            case Method.GET =>
                sttp.get(uri"${insertSchemeIfNone(request.uri)}")
                        .headers(Request.headersToMap(request.headers))
            case Method.PUT =>
                sttp.put(uri"${insertSchemeIfNone(request.uri)}")
                        .headers(Request.headersToMap(request.headers))
                        .body(request.postData.getOrElse(""))
            case Method.POST =>
                sttp.post(uri"${insertSchemeIfNone(request.uri)}")
                        .headers(Request.headersToMap(request.headers))
                        .body(request.postData.getOrElse(""))
            case Method.DELETE =>
                sttp.delete(uri"${insertSchemeIfNone(request.uri)}")
                        .headers(Request.headersToMap(request.headers))
                        .body(request.postData.getOrElse(""))
        }
        req.send()
        
    }
    
    /**
      * main irk.client function. start all requests until time ends
      */
    def run: Future[Boolean] =
        if (sequenced)
            sendSequenceOrdered
        else
            sendSequenceParallel
    
    
    /**
      * we must hae scheme in order for sttp to work
      *
      * @param uri
      */
    private def insertSchemeIfNone(uri: String) =
        if (uri.startsWith("http")) {
            uri
        } else {
            s"http://$uri"
        }
    
    private def sendSequenceParallel: Future[Boolean] = Future {
        while (duration.hasTimeLeft()) {
            sendRequest(RequestContainer.getNextRequest).onComplete {
                case Success(response) =>
                    metrics.meter(s"${response.code}").mark()
                case Failure(e) =>
                    metrics.meter(s"${e.getMessage}").mark()
            }
        }
        FINISHED
    }
    
    
    private def sendSequenceOrdered: Future[Boolean] = Future {
        var sequenceEnd = true
        while (duration.hasTimeLeft()) {
            if (sequenceEnd) {
                sequenceEnd = false
                RequestContainer.getAsList.foldLeft(Future(List.empty[Response[String]]))((prevFuture,
                currentTuple) => {
                    for {
                        prev <- prevFuture
                        curr <- (sendRequest _).apply(currentTuple)
                    } yield prev :+ curr
                })
            }.onComplete {
                case Success(responseList) =>
                    responseList.foreach { response =>
                        metrics.meter(s"${response.code}").mark()
                    }
                    
                    sequenceEnd = true
                case Failure(e) =>
                    metrics.meter(s"${e.getMessage}").mark()
            }
        }
        FINISHED
    }
    
    
}

