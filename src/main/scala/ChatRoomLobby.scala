import ChatRoomLobby.{CreateChatRoom, GetChatRooms, ResponseChatRoom, ResponseChatRooms}
import akka.actor.{Actor, ActorPath, ActorRef, Props}
import akka.actor.Actor.Receive

class ChatRoomLobby extends Actor {

  val chatRoom = context.actorOf(ChatRoom.props(), ChatRoomLobby.defaultChatRoomPathName)

  override def receive: Receive = {
    case CreateChatRoom(name) =>
      context.child(name).getOrElse{
        println("create chatRoom:" + name)
        context.actorOf(Props[ChatRoom], name)
      }
      sender() ! ResponseChatRoom(chatRoom.path.name)
    case GetChatRooms() =>
      var chatRoomNames = Set[String]()
      context.children.foreach{ x =>
        chatRoomNames = chatRoomNames + x.path.name
      }
      sender() ! ResponseChatRooms(chatRoomNames)
  }
}

object ChatRoomLobby {
  case class CreateChatRoom(name:String)
  case class ResponseChatRoom(chatRoomName:String)
  case class GetChatRooms()
  case class ResponseChatRooms(chatRoomNames:Set[String])

  def props():Props = Props[ChatRoomLobby]
  val pathName = "chatRoomLobby"
  val defaultChatRoomPathName = "default"
}