import java.nio.charset.StandardCharsets

import ChatRoom.{ChatMessage, GetChatMessages}
import ChatRoomLobby.{CreateChatRoom, GetChatRooms, ResponseChatRoom, ResponseChatRooms}
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
    val chatRoomLobby = system.actorOf(ChatRoomLobby.props(), ChatRoomLobby.pathName)

    var messageSeqId = 0;
    def getChatRooms: ResponseChatRooms = {
      implicit val timeout = Timeout(5 seconds)
      val future = chatRoomLobby ? new GetChatRooms()
      val response = Await.result(future, timeout.duration).asInstanceOf[ResponseChatRooms]
      response
    }
    def getChatMessages(chatRoom:String, lastMsgId: String = "0"): String = {
      implicit val timeout = Timeout(5 seconds)
      val future = system.actorSelection(chatRoomLobby.path + "/" + chatRoom) ? GetChatMessages(toLong(lastMsgId).getOrElse(0))
      val response = Await.result(future, timeout.duration).asInstanceOf[String]
      response
    }
    def sendChatMessage(chatRoom:String, inputMessage: String): Unit = {
      messageSeqId = messageSeqId+1
      system.actorSelection(chatRoomLobby.path + "/" + chatRoom) ! ChatMessage(messageSeqId, inputMessage)
    }
    def createChatRoom(chatRoom: String): ResponseChatRoom = {
      implicit val timeout = Timeout(5 seconds)
      val future = chatRoomLobby ? new CreateChatRoom(chatRoom)
      Await.result(future, timeout.duration).asInstanceOf[ResponseChatRoom]
    }
    val route =
      path("") {
        getFromResource("index.html")
      } ~
      path("chatRooms") {
        get {
          val response: ResponseChatRooms = getChatRooms
          var responseText = ""
          response.chatRoomNames.foreach(responseText += _ + ",")
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, responseText))
        }
      } ~
      path("chatRooms" / Segment) { chatRoom =>
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(chatRoom)) {
          throw new UnsupportedOperationException("don't support chinese words")
        }
        get {
          parameter("lastMsgId") {(lastMsgId) =>
            val response: String = getChatMessages(chatRoom, lastMsgId)
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, response))
          }
        } ~
        post {
          parameters("inputMessage","lastMsgId") { (inputMessage, lastMsgId) =>
            sendChatMessage(chatRoom, inputMessage)
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, getChatMessages(chatRoom, lastMsgId)))
          }
        } ~
        put {
          createChatRoom(chatRoom)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, getChatMessages(chatRoom)))
        }
      }

    Http().bindAndHandle(route, "0.0.0.0", 8080)
    println(s"Server online at http://0.0.0.0:8080/")
  }

  private def toLong(s:String): Option[Long] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }

}