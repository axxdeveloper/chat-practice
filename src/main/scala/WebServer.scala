import ChatRoom.{ChatMessage, GetChatMessages}
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await

object WebServer {

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    val chatRoom = system.actorOf(Props[ChatRoom], ChatRoom.PATH)
    var seqId = 0;
    val route =
      path("") {
        getFromResource("index.html")
      } ~
      path("chat") {
        post {
          parameters("inputMessage","sessionId") { (inputMessage, sessionId) =>
            parameter("lastMsgId") {(lastMsgId) =>
              seqId = seqId+1
              chatRoom ! ChatMessage(seqId, inputMessage)
              implicit val timeout = Timeout(5 seconds)
              val future = chatRoom ? GetChatMessages(lastMsgId.toLong)
              val response = Await.result(future, timeout.duration).asInstanceOf[String]
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, response))
            }
          }
        }
      } ~
      path("getMessage") {
        get {
          parameter("lastMsgId") {(lastMsgId) =>
            implicit val timeout = Timeout(5 seconds)
            val future = chatRoom ? GetChatMessages(lastMsgId.toLong)
            val response = Await.result(future, timeout.duration).asInstanceOf[String]
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, response))
          }
        }
      }

    Http().bindAndHandle(route, "0.0.0.0", 8080)
    println(s"Server online at http://0.0.0.0:8080/")
  }

}