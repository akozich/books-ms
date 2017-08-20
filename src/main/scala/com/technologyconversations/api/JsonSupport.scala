package com.technologyconversations.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._ // import the default encoders for primitive types (Int, String, Lists etc)

  implicit val bookReducedFormat: RootJsonFormat[BookReduced] = jsonFormat3(BookReduced)
  implicit val bookReducedSeqFormat: RootJsonFormat[Seq[BookReduced]] = seqFormat[BookReduced]
  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat4(Book)
}
