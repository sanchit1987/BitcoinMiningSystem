import akka.actor.Props
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import akka.cluster.Cluster
import akka.cluster.ClusterEvent
import akka.actor.RootActorPath
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.ActorLogging
import akka.actor.Terminated
import akka.actor._
import scala.util.Random
import akka.remote.transport


class RemoteNode(ipAddress : String) extends Actor with ActorLogging {
  var port = 50000+new Random().nextInt(1000)
  val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[ClusterEvent.MemberUp])
  cluster.subscribe(self, classOf[ClusterEvent.MemberRemoved])
  
 val main = new Address("akka.tcp","project1", ipAddress ,2552)
  
   context.actorSelection(RootActorPath(main) / "user" / "BitCoinsServer") ! Identify("120")  
 
  def receive = {
    case ClusterEvent.MemberUp(member) => 
        
    case ActorIdentity("120", None) => 
      println("Server not Found")
      context.stop(self)
    case ActorIdentity("120", Some(ref)) =>
      
      cluster.join(main)
      context.watch(ref)
    case Terminated(_) => context.stop(self)
    context.system.shutdown
    case ClusterEvent.MemberRemoved(m, _) =>
      if (m.address == main) context.stop(self)
  }
}