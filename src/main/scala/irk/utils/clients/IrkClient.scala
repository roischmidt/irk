package irk.utils.clients

import irk.http.Request

import scala.concurrent.Future

trait IrkClient {
    
    /*
        get irk.http.Request and returns response code
     */
    def sendRequest(request: Request) : Future[Int]
    
    def close : Unit
    
    /**
      * we must hae scheme in order for sttp to work
      *
      * @param uri
      */
    def insertSchemeIfNone(uri: String) =
        if (uri.startsWith("http")) {
            uri
        } else {
            s"http://$uri"
        }
    
}
