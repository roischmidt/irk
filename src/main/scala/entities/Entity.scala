package entities

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import entities.Method.Method
import play.api.libs.json.Json
import utils.EnumJsonUtils

object Method extends Enumeration {
    type Method = Value
    val GET,POST,PUT,DELETE = Value
    
    implicit val enumTypeFormat = EnumJsonUtils.enumFormat(Method)
}

case class Entity (
    method: Method,
    uri: String,
    headers: String,
    postData: String
)

object Entity {
    implicit val fmtJson = Json.format[Entity]
    
    val headerSeparator = "\n" //CRLF
    
    def toHttpHeaderList(entity: Entity) : List[HttpHeader] = {
        entity.headers.split(headerSeparator).map{header =>
            val keyVal = header.split(":")
            RawHeader(keyVal(0),keyVal(1))
        }.toList
    }
    
}
