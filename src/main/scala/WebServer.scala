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
    var chatRoom = system.actorOf(Props[ChatServer], ChatServer.PATH)

    val route =
      path("chatRoom") {
        get {
          println("get chatroom")
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, scala.io.Source.fromInputStream(getClass.getResourceAsStream("index.html")).mkString))
        }
      }

//    val requestHandler: HttpRequest => HttpResponse = {
//      case req @ HttpRequest(GET, Uri.Path("/"), headers, entity, protocol) =>
//        println(headers)
//        println(entity)
//        println(protocol)
//        println(req.uri.toString())
//        println(req.uri)
//        HttpResponse(entity = HttpEntity(
//          ContentTypes.`text/html(UTF-8)`,scala.io.Source.fromInputStream(getClass.getResourceAsStream("index.html")).mkString))
//
//      case HttpRequest(GET, Uri.Path("/chatRoom"), headers, entity, protocol) =>
//        println(headers)
//        println(entity)
//        println(protocol)
//        HttpResponse(entity = HttpEntity(
//          ContentTypes.`text/html(UTF-8)`,"<html><body>test</body></html>"))
//
//      case _: HttpRequest =>
//        HttpResponse(404, entity = "Unknown resource!")
//    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
//    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
//    StdIn.readLine() // let it run until user presses return
//    bindingFuture
//      .flatMap(_.unbind()) // trigger unbinding from the port
//      .onComplete(_ => system.terminate()) // and shutdown when done

  }
}