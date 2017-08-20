package com.technologyconversations.api

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait AllRoutes extends BookRoutes with StaticRoute {
  //#all-routes
  lazy val routes: Route = respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
    bookRoutes ~ staticRoute
  }
  //#all-routes
}
