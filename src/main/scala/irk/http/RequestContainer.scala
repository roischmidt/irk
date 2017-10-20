package irk.http

import java.io.FileNotFoundException
import java.util.NoSuchElementException

import irk.utils.HttpRawRequestParser

import scala.collection.mutable


object RequestContainer {
    
    private var circular: Iterator[Request] = Iterator.empty
    private var requestList: List[Request] = List.empty
    
    def getNextRequest: Request = {
        if (requestList.isEmpty)
            throw new NoSuchElementException("seq of entities is empty")
        circular.next()
    }
    
    def setRequestList(requests: List[Request]) =
        if(requests.nonEmpty) {
            requestList = requests
            circular = Iterator.continually(requestList).flatten
        }
    
    def isEmpty = requestList.isEmpty
    
    def getAsList: List[Request] = requestList
    
    def clear =
        requestList = List.empty
    
    
}

/**
  * EntityBuilder provides facility to create the list of entities. can be from entity file or simple url (app argument)
  */
class RequestBuilder {
    
    val ENTITY_SEPARATOR = "---END---\n"
    
    def loadFromFile(fileName: String): List[Request] = {
        val lsOut: mutable.MutableList[Request] = mutable.MutableList.empty
        try {
            val httpFileStr = scala.io.Source.fromFile(fileName, "UTF-8").mkString
            if (httpFileStr.nonEmpty) {
                httpFileStr.split(ENTITY_SEPARATOR).foreach { req =>
                    val parser = new HttpRawRequestParser()
                    parser.parseRawRequest(req)
                    lsOut += Request.requestFromRawParser(parser)
                }
            }
        } catch {
            case e : FileNotFoundException =>
                println(s"$fileName - No such file or directory")
                System.exit(0)
        }
       
        lsOut.toList
    }
    
    def loadFromUrl(url: String): List[Request] = {
        val httpRequest =
            Request(
                method = Method.GET,
                uri = url,
                headers = List.empty,
                postData = None
            )
        List(httpRequest)
    }
}
