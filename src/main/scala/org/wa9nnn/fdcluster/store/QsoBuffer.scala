package org.wa9nnn.fdcluster.store

import com.codahale.metrics.{MetricRegistry, SharedMetricRegistries}
import nl.grons.metrics4.scala.{DefaultInstrumented, MetricName}
import org.wa9nnn.fdcluster.model.Qso
import scalafx.collections.ObservableBuffer

import javax.inject.{Inject, Singleton}

@Singleton
class QsoBuffer @Inject()() extends ObservableBuffer[Qso]  with DefaultInstrumented {
    override lazy val metricBaseName = MetricName("QsoBuffer")

    metrics.gauge("QsoBuffer") {
      length
    }

  import io.prometheus.client.Gauge

  val qsoCount: Gauge = Gauge.build
    .name("Qsos")
    .help("QSO count in memory.")
    .register
  qsoCount.set(size)

  onChange { (_, _) =>
    qsoCount.set(size)
  }


//  import io.prometheus.client.CollectorRegistry
//  import io.prometheus.client.dropwizard.DropwizardExports
  //  val metrics: MetricRegistry = SharedMetricRegistries.getOrCreate("default")
  //  private val exports = new DropwizardExports(metrics.registry)
  //  CollectorRegistry.defaultRegistry.register(exports)

}
