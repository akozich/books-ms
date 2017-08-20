package com.technologyconversations.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class StaticRouteSpec extends WordSpec
  with ScalatestRouteTest
  with StaticRoute
  with Matchers {

  val staticUri = "/test.html"

  s"GET $staticUri" should {

    "return OK" in {
      Get(staticUri) ~> staticRoute ~> check {
        response.status shouldBe OK
      }
    }

    "return file content" in {
      Get(staticUri) ~> staticRoute ~> check {
        val content = responseAs[String]
        content shouldBe "This is just a test"
      }
    }

  }
}
