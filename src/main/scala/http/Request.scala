package http

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpHeader.ParsingResult.{Error, Ok}
import http.Method.Method
import play.api.libs.json.Json
import utils.{EnumJsonUtils, HttpRawRequestParser}

object Method extends Enumeration {
    type Method = Value
    val GET, POST, PUT, DELETE = Value
    
    implicit val enumTypeFormat = EnumJsonUtils.enumFormat(Method)
}

case class Request(
    method: Method,
    uri: String,
    headers: List[String],
    postData: String
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
                   postData = parser.requestBody.toString.trim
               )
    }
    
    def headerstoHttpHeaderList(headers: List[String]) : List[HttpHeader] = {
         headers.map{ e =>
             HttpHeader.parse(e.split(":")(0),e.split(":")(1)) match {
                 case Ok(header,_) => Some(header)
                 case Error(_) =>  None
             }
         }.filter(_.isDefined).map(_.get)
    }
    
}
