package utils

import java.io.{BufferedReader, StringReader}

case class HttpFormatException(msg: String, cause: Throwable = None.orNull) extends Exception(msg,cause)

/**
  * Class for HTTP request parsing as defined by RFC 2612:
  *
  * Request = Request-Line ; Section 5.1 (( general-header ; Section 4.5 |
  * request-header ; Section 5.3 | entity-header ) CRLF) ; Section 7.1 CRLF [
  * message-body ] ; Section 4.3
  *
  * class was based on a java class from stackOverflow, written by gor Zelaya (https://stackoverflow.com/questions/13255622/parsing-raw-http-request)
  * I translated to scala and added extra functionality
**/
class HttpRawRequestParser {

  var requestLine : String = ""
  var requestHeaders : Map[String,String] = Map.empty
  var requestBody : StringBuffer = new StringBuffer()


  def parseRawRequest(request: String) = {
    val reader = new BufferedReader(new StringReader(request))
    // parse request line first
    setRequestLine(reader.readLine)
    // parse headers
    var header = reader.readLine
    while(header.nonEmpty){
      appendHeaderParameter(header)
      header = reader.readLine
    }
    //parse body
    var bodyLine = reader.readLine
    while(bodyLine != null){
      appendBody(bodyLine)
      bodyLine = reader.readLine
    }
  }

  /**
    *
    * 5.1 Request-Line The Request-Line begins with a method token, followed by
    * the Request-URI and the protocol version, and ending with CRLF. The
    * elements are separated by SP characters. No CR or LF is allowed except in
    * the final CRLF sequence.
    *
    *
    */
  private def setRequestLine(requestLine: String) = {
    if (requestLine == null || requestLine.isEmpty)
      throw HttpFormatException("Invalid Request-Line: " + requestLine)
    this.requestLine = requestLine
  }

  private def appendHeaderParameter(header: String) = {
    val idx = header.indexOf(":")
    if (idx == -1)
      throw HttpFormatException("Invalid Header Parameter: " + header)
    requestHeaders = requestHeaders.updated(header.substring(0, idx), header.substring(idx + 1, header.length))
  }

  /**
    * The message-body (if any) of an HTTP message is used to carry the
    * entity-body associated with the request or response. The message-body
    * differs from the entity-body only when a transfer-coding has been
    * applied, as indicated by the Transfer-Encoding header field (section
    * 14.41).
    *
    *
    */

  private def appendBody(bodyLine: String) =
    requestBody.append(bodyLine).append("\r\n")

  /**
    * returns all headers in one list
    * @return list of headers key:value
    */
  def getAllHeadersList: List[String] = {
    requestHeaders.toList.map(e => s"${e._1}:${e._2}".trim)
  }

  /**
    * For list of available headers refer to sections: 4.5, 5.3, 7.1 of RFC 2616
    *
    * @param headerName Name of header
    * @return Some(String) with the value of the header or None if not found.
    */
  def getHeaderByName(headerName: String) : Option[String] = requestHeaders.get(headerName)

  def getHost: Option[String] = requestHeaders.get("Host").orElse(requestHeaders.get("host").orElse(requestHeaders.get("HOST")))

  def getMethod : String  = requestLine.trim.split(" ").head
}
