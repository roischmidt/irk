package entities

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
}
