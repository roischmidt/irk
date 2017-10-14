package irk.http

import irk.http.Method.Method
import play.api.libs.json.Json
import irk.utils.{EnumJsonUtils, HttpRawRequestParser}

object Method extends Enumeration {
    type Method = Value
    val GET, POST, PUT, DELETE = Value
    
    implicit val enumTypeFormat = EnumJsonUtils.enumFormat(Method)
}

case class Request(
    method: Method,
    uri: String,
    headers: List[String],
    postData: Option[String] = None
)

case class RequestException(msg: String, cause: Throwable = None.orNull) extends Exception(msg, cause)

object Request {
    implicit val fmtJson = Json.format[Request]
    
    def requestFromRawParser(parser: HttpRawRequestParser): Request = {
        if (parser == null)
            throw RequestException("parser is null")
        if (parser.getHost.isEmpty)
            throw RequestException("host is empty")
        Request(
            method = Method.withName(parser.getMethod),
            uri = parser.getHost.get.trim,
            headers = parser.getAllHeadersList,
            postData = if (parser.requestBody.length() > 0)
                Some(parser.requestBody.toString.trim)
            else
                None
        )
    }
    
    def headersToMap(headers: List[String]): Map[String, String] = {
        headers map (_.split(":")) map {
            case Array(k, v) => (k, v)
        } toMap
    }
    
    
}