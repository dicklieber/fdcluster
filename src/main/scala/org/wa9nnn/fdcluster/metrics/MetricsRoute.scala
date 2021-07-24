package org.wa9nnn.fdcluster.metrics
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{formFields, onSuccess, path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.CookieDirectives
import akka.http.scaladsl.server.directives.RouteDirectives.complete

object MetricsRoute {
  import fr.davit.akka.http.metrics.core.scaladsl.server.HttpMetricsDirectives._
  import fr.davit.akka.http.metrics.prometheus.marshalling.PrometheusMarshallers._

  val metricsRoute =  path("metrics")(metrics(org.wa9nnn.fdcluster.javafx.FdCluster1.registry))
}
