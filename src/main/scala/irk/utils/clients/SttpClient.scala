package irk.utils.clients

import java.util.concurrent.TimeUnit

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import irk.http.{Method, Request}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class SttpClient(connectionTimeout: java.time.Duration)(implicit executionContext: ExecutionContext) extends IrkClient {
    
    
    
    implicit val sttpBackend: SttpBackend[Future, Nothing] = AsyncHttpClientFutureBackend(
        connectionTimeout = FiniteDuration(connectionTimeout.toMillis, TimeUnit.MILLISECONDS)
    )(ExecutionContext.Implicits.global)
    
    override def sendRequest(request: Request): Future[Int] = {
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
        
        req.send().map(_.code)
    }
    
    override def close: Unit = {
        sttpBackend.close()
    }
}
