package com.technologyconversations.api.util

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives

import scala.concurrent.Future

object RouteUtils {
  def completeWithLocationHeader[T](resourceId: Future[Option[T]],
                                    ifDefinedStatus: StatusCode,
                                    ifEmptyStatus: StatusCode): Route =
    onSuccess(resourceId) {
      case Some(t) => completeWithLocationHeader(ifDefinedStatus, t)
      case None => RouteDirectives.complete(ifEmptyStatus)
    }

  def completeWithLocationHeader[T](status: StatusCode, resourceId: T): Route =
    extractRequestContext { requestContext =>
      val request = requestContext.request
      val location = request.uri.copy(path = request.uri.path / resourceId.toString)
      respondWithHeader(Location(location)) {
        RouteDirectives.complete(status)
      }
    }

  def complete(resource: Future[Unit]): Route = onSuccess(resource) {
    RouteDirectives.complete(StatusCodes.NoContent)
  }

}
