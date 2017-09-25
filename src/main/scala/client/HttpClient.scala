package client

import java.util.concurrent.Executors

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import http.{Method, Request, RequestContainer}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class HttpClient(numOfThreads: Int, timeoutInSeconds: Long, sequenced: Boolean = false) {
    
    implicit val clientExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numOfThreads))
    
    
    implicit val sttpBackend = AsyncHttpClientFutureBackend()
    
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
      * main client function. start all requests until time ends
      */
    def run =
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
    
    private def sendSequenceParallel: Unit =
        while (duration.hasTimeLeft()) {
            sendRequest(RequestContainer.getNextRequest).map { response =>
                println(response.code)
            }
        }
    
    
    private def sendSequenceOrdered: Unit = {
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
            }.andThen {
                case _ => sequenceEnd = true
            }
        }
    }
    
    
}

