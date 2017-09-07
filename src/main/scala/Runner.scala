import config.IrkConfig
import entities.EntityContainer

object Runner extends App{
    
    // load configuration from file
    if(!IrkConfig.init) System.exit(2)
    
    //load entities
    val entityContainer = new EntityContainer(IrkConfig.irkConfig.get.entitiesPath)
    if(entityContainer.isEmpty) System.exit(2)
    
}
