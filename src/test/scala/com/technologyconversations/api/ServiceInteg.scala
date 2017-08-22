package com.technologyconversations.api

import org.scalatest.{ Matchers, WordSpec }

import scalaj.http._
import scala.util.Properties._

class ServiceInteg extends WordSpec with Matchers {

  val domain = envOrElse("DOMAIN", "http://localhost:8080")
  val uri = s"$domain/api/v1/books"

  s"GET $uri" should {

    "return OK" in {
      val response: HttpResponse[String] = Http(uri).asString
      response.code shouldEqual 200
    }

  }

}
