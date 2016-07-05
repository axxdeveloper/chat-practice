import ChatRooms.{CreateChatRoom, GetChatRooms, ResponseChatRoom, ResponseChatRooms}
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive

class ChatRooms extends Actor {

  val chatRoom = context.actorOf(Props(classOf[ChatRoom],ChatRoom.PATH), ChatRoom.PATH)

  override def receive: Receive = {
    case CreateChatRoom(name) =>
      val chatRoom = context.child(name).getOrElse( context.actorOf(Props[ChatRoom], name) )
      sender() ! ResponseChatRoom(chatRoom.path.name)
    case GetChatRooms() =>
      val chatRoomNames = List()
      context.children.foreach(chatRoomNames + _.path.name)
      println(chatRoomNames)
      sender() ! ResponseChatRooms(List())
  }
}

object ChatRooms {
  case class CreateChatRoom(name:String)
  case class ResponseChatRoom(chatRoomName:String)
  case class GetChatRooms()
  case class ResponseChatRooms(chatRoomNames:List[String])
}