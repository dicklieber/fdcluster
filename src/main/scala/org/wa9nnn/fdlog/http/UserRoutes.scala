package org.wa9nnn.fdlog.http

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model.sync.QsoHour
import org.wa9nnn.fdlog.store.network.FdHour
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.{Failure, Success}
//#user-routes-class
trait UserRoutes  extends LazyLogging {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
//  implicit def system: ActorSystem


  // other dependencies that UserRoutes use
  val store: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  //#users-get-post
  //#users-get-delete   
  lazy val userRoutes: Route =
    pathPrefix("qsoHours") {
      concat(
        //#users-get-delete
        pathEnd {
          concat(
            get {val dummyFdHour = FdHour.allHours
              val users = (store ? dummyFdHour).mapTo[Seq[QsoHour]]
//              complete(users)
              onComplete(users) {
                case Success(value: Seq[QsoHour]) =>
                  val sJson =  Json.prettyPrint(Json.toJson(value))
                  complete(sJson)
                case Failure(ex)    =>
                  logger.error("route", ex)
                  complete((500, s"An error occurred: ${ex.getMessage}"))
//                case Failure(ex)    => complete((InternalServerError, s"An error occurred: ${ex.getMessage}"))


              }
//              te(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))

            },
//            post {
//              entity(as[User]) { user =>
//                val userCreated: Future[ActionPerformed] =
//                  (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
//                onSuccess(userCreated) { performed =>
//                  logger.info("Created user [{}]: {}", user.name, performed.description)
//                  complete((StatusCodes.Created, performed))
//                }
//              }
//            }
          )
        },
        //#users-get-post
        //#users-get-delete
/*
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-user-info
              val maybeUser: Future[Option[User]] =
                (userRegistryActor ? GetUser(name)).mapTo[Option[User]]
              rejectEmptyResponse {
                complete(maybeUser)
              }
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
              val userDeleted: Future[ActionPerformed] =
                (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
              onSuccess(userDeleted) { performed =>
                logger.info("Deleted user [{}]: {}", name, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            }
          )
        }
*/
      )
      //#users-get-delete
    }
  //#all-routes
}
