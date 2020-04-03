package irk.utils


import com.codahale.metrics.SharedMetricRegistries
import nl.grons.metrics4.scala.{Counter, Meter}

import scala.collection.JavaConverters._

object Metrics {

    val metricRegistry = SharedMetricRegistries.getOrCreate("default")

    /**
      * converts to simpleName (e.g irk.client.meter.response200 to response200)
      * @return all meters as list of Strings (simpleName,meter.cunt)
      */
    def metersToSimpleNameStringList : List[(String,Long)] = metricRegistry.getMeters().asScala.toList
            .map(e => (e._1.substring(e._1.lastIndexOf('.')+1),e._2.getCount))
            .filterNot(_._1.isEmpty)
    
    def sumMeters() : Long =
        metersToSimpleNameStringList.map(e => e._2).sum
    
    def getMeterBySimpleName(simpleName: String) : Option[Meter] =
        metricRegistry.getMeters().asScala.find {
            e => e._1.contains(s".$simpleName")
        }.map(x => new Meter(x._2))
    
    def getCounterBySimpleName(simpleName: String) : Option[Counter] =
        metricRegistry.getCounters.asScala.find {
            e => e._1.contains(s".$simpleName")
        }.map(x => new Counter(x._2))
    
}

trait Instrumented extends nl.grons.metrics4.scala.InstrumentedBuilder {
    val metricRegistry = Metrics.metricRegistry
}
