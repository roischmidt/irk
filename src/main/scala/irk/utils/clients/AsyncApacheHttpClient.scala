package irk.utils.clients

import java.net.URI
import java.nio.CharBuffer
import java.util.concurrent.{Future => JFuture}

import irk.http.{Method, Request}
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClients}
import org.apache.http.nio.IOControl
import org.apache.http.nio.client.methods.AsyncCharConsumer
import org.apache.http.protocol.HttpContext

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global


class AsyncApacheHttpClient(connectionTimeout: java.time.Duration,maxConnections: Int) extends IrkClient {

  var httpclient: CloseableHttpAsyncClient = _

 // def AsyncApacheHttpClient() = {

    val requestConfig = RequestConfig
      .custom
      .setSocketTimeout(connectionTimeout.toMillis.toInt)
      .setConnectTimeout(connectionTimeout.toMillis.toInt)
      .build
    httpclient = HttpAsyncClients
      .custom
      .setDefaultRequestConfig(requestConfig)
        .setMaxConnTotal(maxConnections)
      .build
    httpclient.start()
    /*
     try {
            httpclient.start();
            final HttpGet[] requests = new HttpGet[] {
                    new HttpGet("http://httpbin.org/ip"),
                    new HttpGet("https://httpbin.org/ip"),
                    new HttpGet("http://httpbin.org/headers")
            };
            final CountDownLatch latch = new CountDownLatch(requests.length);
            for (final HttpGet request: requests) {
                httpclient.execute(request, new FutureCallback<HttpResponse>() {

                    @Override
                    public void completed(final HttpResponse response) {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + "->" + response.getStatusLine());
                    }

                    @Override
                    public void failed(final Exception ex) {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + "->" + ex);
                    }

                    @Override
                    public void cancelled() {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + " cancelled");
                    }

                });
            }
            latch.await();
            System.out.println("Shutting down");
        } finally {
            httpclient.close();
        }
        System.out.println("Done");
    }
     */
 // }

  override def sendRequest(request: Request): Future[Int] = {
    val req: HttpRequestBase = request.method match {
      case Method.GET =>
        val r = new HttpGet(s"http://${request.uri}")
        Request.headersToMap(request.headers).foreach {
          e => r.addHeader(e._1, e._2)
        }
        r
      case Method.PUT =>
        val r = new HttpPut(request.uri)
        Request.headersToMap(request.headers).foreach {
          e => r.addHeader(e._1, e._2)
        }
        r.setEntity(new StringEntity(request.postData.getOrElse("")))
        r
      case Method.POST =>
        val r = new HttpPost(request.uri)
        Request.headersToMap(request.headers).foreach {
          e => r.addHeader(e._1, e._2)
        }
        r.setEntity(new StringEntity(request.postData.getOrElse("")))
        r
      case Method.DELETE =>
        val r = new HttpDelete(request.uri)
        Request.headersToMap(request.headers).foreach {
          e => r.addHeader(e._1, e._2)
        }
        r
    }
    val p = Promise[Int]
    httpclient.execute(req, new FutureCallback[HttpResponse]() {
      override def failed(ex: Exception) = {
        p.failure(throw ex)
      }

      override def completed(result: HttpResponse) = {
        p.complete(Success(result.getStatusLine.getStatusCode))
      }

      override def cancelled() = {
        p.failure(throw new Exception(s"operation canceled for $request"))
      }
    })
    p.future
  }



  override def close: Unit =
    Future {
      while(httpclient.isRunning){}
      httpclient.close()
    }


  private[clients] class MyResponseConsumer extends AsyncCharConsumer[Boolean] {
    protected override def onResponseReceived(response: HttpResponse) {
    }

    protected override def onCharReceived(buf: CharBuffer, ioctrl: IOControl) {
      while (buf.hasRemaining) {
        val chr = buf.get()
        System.out.print(chr.toString.length + "\n")
      }
    }

    protected override def releaseResources {
    }

    protected override def buildResult(context: HttpContext): Boolean = {
      true
    }
  }

}
