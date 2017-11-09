package irk.utils.clients

import java.util.concurrent.{Future => JFuture}

import irk.http.{Method, Request}
import org.apache.http.{HttpRequest, HttpResponse}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClients}

import scala.concurrent.{Future, Promise}
import scala.util.Success


class AsyncApacheHttpClient(connectionTimeout: java.time.Duration, maxConnections: Int) extends IrkClient {


    val requestConfig = RequestConfig
      .custom
      .setSocketTimeout(connectionTimeout.toMillis.toInt)
      .setConnectTimeout(connectionTimeout.toMillis.toInt)
      .build

    val httpclient: CloseableHttpAsyncClient = HttpAsyncClients
      .custom
      .setDefaultRequestConfig(requestConfig)
      .setMaxConnPerRoute(maxConnections)
      .setMaxConnTotal(maxConnections)
      .build

    httpclient.start()



  override def sendRequest(request: Request): Future[Int] = {
    val p = Promise[Int]
    try {
      httpclient.execute(createHttpRequest(request), new FutureCallback[HttpResponse]() {
        override def failed(ex: Exception): Unit = {
          println(s"failed with exception :${ex.getMessage}")
          p.failure(throw ex)
        }

        override def completed(result: HttpResponse): Unit = {
          p.complete(Success(result.getStatusLine.getStatusCode))
        }

        override def cancelled(): Unit = {
          p.failure(throw new Exception(s"operation canceled for $request"))
        }
      })
    } catch {
      case ex: Exception =>
        println(s"failed with exception :${ex.getMessage}")
        p.failure(throw ex)
    }
    p.future
  }

  private def createHttpRequest(request: Request) : HttpRequestBase = {
    val updatedRequest = request.uri.startsWith("http") match {
      case true => request
      case false => request.copy(uri = s"http://${request.uri}")
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


  override def close: Unit = {
    httpclient.close()
  }

}
