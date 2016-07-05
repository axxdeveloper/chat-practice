import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

/**
  * @author <a href="https://github.com/shooeugenesea">isaac</a>
  */
object WebServer {

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future map/flatmap in the end
    implicit val executionContext = system.dispatcher
    val chatRoom = system.actorOf(Props[ChatRoom], ChatRoom.PATH)

    val route =
      path("") {
        getFromResource("index.html")
      } ~
      path("chat") {
        post {
          parameters("inputMessage") { (inputMessage) =>
            println(inputMessage)
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, inputMessage))
          }
        }
      }

    Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/")
  }
}