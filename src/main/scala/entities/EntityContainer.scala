package entities

import java.util.NoSuchElementException

class EntityContainer(entityFileName: String) {
    
    private val circular = {
        readEntityFile(entityFileName) match {
            case seq if seq.isEmpty =>
                Iterator.empty
            case seq =>
                Iterator.continually(seq).flatten
        }
        
    }
    
    def getNextEntity: Entity = {
        if (circular.isEmpty)
            throw new NoSuchElementException("seq of entities is empty")
        circular.next()
    }
    
    def isEmpty = circular.isEmpty
    
    private def convertLineToEntity(line: String): Option[Entity] = {
        try {
            line.split(",").toList match {
                case m :: u :: h :: d :: Nil =>
                    Some(Entity(Method.withName(m.toUpperCase), u, h, d))
                case m :: u :: h :: Nil =>
                    Some(Entity(Method.withName(m.toUpperCase), u, h, ""))
                case m :: u :: Nil =>
                    Some(Entity(Method.withName(m.toUpperCase), u, "", ""))
                case _ =>
                    None
            }
        } catch {
            case e: Exception => None
        }
    }
    
    private def readEntityFile(filename: String): Seq[Entity] = {
        try {
            val bufferedSource = io.Source.fromFile(filename)
            val lines = (for (line <- bufferedSource.getLines()) yield line).toList
            bufferedSource.close
            lines.map(line => convertLineToEntity(line)).filter(_.isDefined).map(_.get)
        } catch {
            case e: Exception =>
                println(s"there was an error when trying to upload entities from file $filename : $e")
                Seq.empty
        }
    }
    
}
