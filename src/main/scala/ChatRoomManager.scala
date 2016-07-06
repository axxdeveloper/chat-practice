import ChatRoomManager.{CreateChatRoom, GetChatRooms, ResponseChatRoom, ResponseChatRooms}
import akka.actor.{Actor, ActorPath, ActorRef, Props}
import akka.actor.Actor.Receive

class ChatRoomManager extends Actor {

  val chatRoom = context.actorOf(Props(classOf[ChatRoom]), "default")

  override def receive: Receive = {
    case CreateChatRoom(name) =>
      val chatRoom = context.child(name).getOrElse( context.actorOf(Props[ChatRoom], name) )
      sender() ! ResponseChatRoom(chatRoom.path.name)
    case GetChatRooms() =>
      var chatRoomNames = Set[String]()
      context.children.foreach{ x =>
        chatRoomNames = chatRoomNames + x.path.name
      }
      sender() ! ResponseChatRooms(chatRoomNames)
  }
}

object ChatRoomManager {
  case class CreateChatRoom(name:String)
  case class ResponseChatRoom(chatRoomName:String)
  case class GetChatRooms()
  case class ResponseChatRooms(chatRoomNames:Set[String])
}