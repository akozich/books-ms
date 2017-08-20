package com.technologyconversations.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.technologyconversations.api.mongodb.{ EnvironmentMongoDBConfigComponent, MongoDBBookDAOComponent }

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn

//#main-class
object Service extends App
    with AllRoutes
    with EnvironmentMongoDBConfigComponent
    with MongoDBBookDAOComponent {

  val Address = "0.0.0.0"
  val Port = 8080

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  // Needed for the Future and its methods flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.dispatcher

  val bookRegistryActor = system.actorOf(BookRegistryActor.props(bookDao), "bookRegistryActor")

  //#http-server
  val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, Address, Port)
  println(s"Server online at http://$Address:$Port/\nPress RETURN to stop...")
  StdIn.readLine()
  serverBindingFuture
    .flatMap(_.unbind())
    .onComplete { done =>
      done.failed.map { ex => log.error(ex, "Failed unbinding") }
      system.terminate()
    }
  //#http-server
  //#main-class
}
//#main-class
