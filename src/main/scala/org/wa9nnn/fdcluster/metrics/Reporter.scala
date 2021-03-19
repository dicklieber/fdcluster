
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
//  reporter.start(1, TimeUnit.MINUTES)

}
