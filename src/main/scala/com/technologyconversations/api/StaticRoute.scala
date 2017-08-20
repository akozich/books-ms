package com.technologyconversations.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait StaticRoute {
  //#static-routes
  lazy val staticRoute: Route = pathPrefix("") {
    getFromDirectory("client/")
  }
  //#static-routes
}
