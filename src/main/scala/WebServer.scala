import ChatRoom.{ChatMessage, GetChatMessages}
import ChatRoomManager.{GetChatRooms, ResponseChatRooms}
import akka.actor.{ActorRef, ActorSystem, Identify, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, _}
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
    val chatRooms = system.actorOf(Props[ChatRoomManager], "chatRooms")

    var seqId = 0;
    val route =
      path("") {
        getFromResource("index.html")
      } ~
      path("chatRooms") {
        get {
          implicit val timeout = Timeout(5 seconds)
          val future = chatRooms ? new GetChatRooms()
          val response = Await.result(future, timeout.duration).asInstanceOf[ResponseChatRooms]
          println(response.chatRoomNames)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, response.toString))
        }
      } ~
      path("chat") {
        post {
          parameters("inputMessage","sessionId") { (inputMessage, sessionId) =>
            parameter("lastMsgId") {(lastMsgId) =>
              seqId = seqId+1
              system.actorSelection("chatRooms/chatRoom") ! ChatMessage(seqId, inputMessage)
              implicit val timeout = Timeout(5 seconds)
              val future = system.actorSelection("chatRooms/default") ? GetChatMessages(lastMsgId.toLong)
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
            val future = system.actorSelection("chatRooms/default") ? GetChatMessages(lastMsgId.toLong)
            val response = Await.result(future, timeout.duration).asInstanceOf[String]
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, response))
          }
        }
      }

    Http().bindAndHandle(route, "0.0.0.0", 8080)
    println(s"Server online at http://0.0.0.0:8080/")
  }

}