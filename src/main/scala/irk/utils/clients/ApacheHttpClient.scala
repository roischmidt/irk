package irk.utils.clients
import irk.http.{Method, Request}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class ApacheHttpClient(connectionTimeout: java.time.Duration, maxConnections: Int) extends IrkClient {

  val requestConfig = RequestConfig
    .custom
    .setSocketTimeout(connectionTimeout.toMillis.toInt)
    .setConnectTimeout(connectionTimeout.toMillis.toInt)
    .build

  val httpclient: CloseableHttpClient = HttpClients
    .custom
    .setDefaultRequestConfig(requestConfig)
    .setMaxConnPerRoute(maxConnections)
    .setMaxConnTotal(maxConnections)
    .build



  override def sendRequest(request: Request): Future[Int] =
    Future {
      httpclient.execute(createHttpRequest(request))
        .getStatusLine
        .getStatusCode
    }


  private def createHttpRequest(request: Request) : HttpRequestBase = {
    val updatedRequest = if (request.uri.startsWith("http")) {
      request
    } else {
      request.copy(uri = s"http://${request.uri}")
    }
    val req: HttpRequestBase = updatedRequest.method match {
      case Method.GET =>
        new HttpGet(updatedRequest.uri)
      case Method.PUT =>
        val r = new HttpPut(updatedRequest.uri)
        r.setEntity(new StringEntity(updatedRequest.postData.getOrElse("")))
        r
      case Method.POST =>
        val r = new HttpPost(updatedRequest.uri)
        r.setEntity(new StringEntity(updatedRequest.postData.getOrElse("")))
        r
      case Method.DELETE =>
        new HttpDelete(updatedRequest.uri)
    }
    // set headers
    Request.headersToMap(updatedRequest.headers).foreach {
      e => req.addHeader(e._1, e._2)
    }
    req
  }

  override def close: Unit =
    httpclient.close()
}
