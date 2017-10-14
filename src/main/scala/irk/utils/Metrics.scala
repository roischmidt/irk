package irk.utils

import scala.collection.JavaConverters._

object Metrics {
    
    val metricRegistry = new com.codahale.metrics.MetricRegistry()
    
    /**
      * converts to simpleName (e.g irk.client.meter.response200 to response200)
      * @return all meters as list of Strings (simpleName,meter.cunt)
      */
    def metersToSimpleNameStringList : List[(String,Long)] = metricRegistry.getMeters().asScala.toList
            .map(e => (e._1.substring(e._1.lastIndexOf('.')+1),e._2.getCount))
    
    def sumMeters() : Long =
        metersToSimpleNameStringList.map(e => e._2).sum
    
}

trait Instrumented extends nl.grons.metrics.scala.InstrumentedBuilder {
    val metricRegistry = Metrics.metricRegistry
}
