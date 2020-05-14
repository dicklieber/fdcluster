
package org.wa9nnn.fdcluster.metrics

import java.net.InetSocketAddress

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import javax.inject.{Inject, Singleton}
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.model.NodeAddress

@Singleton
class Reporter @Inject()(nodeAddress: NodeAddress) extends DefaultInstrumented {

  import com.codahale.metrics.MetricFilter
  import java.util.concurrent.TimeUnit

  val graphite = new Graphite(new InetSocketAddress("localhost", 2003));

  val reporter: GraphiteReporter = GraphiteReporter
    .forRegistry(metricRegistry)
    .prefixedWith(s"fdcluster.${nodeAddress.graphiteName}")
    .convertRatesTo(TimeUnit.SECONDS)
    .convertDurationsTo(TimeUnit.MILLISECONDS)
    .filter(MetricFilter.ALL).build(graphite)
  reporter.start(1, TimeUnit.MINUTES)

}
