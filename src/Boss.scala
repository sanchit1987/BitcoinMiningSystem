
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
import akka.actor.Deploy
import akka.actor.Address
import akka.remote.RemoteScope
import collection.mutable.ListBuffer
import java.util.concurrent.TimeUnit

class Boss extends Actor {
  import context.dispatcher
  var zeros = 0
  val time = 60
  var noOfAssignedWork = 0
  var bitListBuffer = ListBuffer[Tuple2[String, String]]()
  import ClusterEvent.{ MemberUp, MemberRemoved }

  val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[ClusterEvent.MemberUp])
  cluster.subscribe(self, classOf[ClusterEvent.MemberRemoved])

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  val randomGen = new Random
  def pick[A](coll: IndexedSeq[A]): A = coll(randomGen.nextInt(coll.size))
  
   context.system.scheduler.scheduleOnce(time.seconds) {
        
        context.parent ! TotalBitcoins(bitListBuffer)
     }


  def receive = awaitingMembers

  val awaitingMembers: Receive = {

    case ServerGeneratedBitcoins(noOfZeros) =>
      {
        zeros = noOfZeros	
        noOfAssignedWork += 1
        val master = context.actorOf(Props[Master])
        master ! GenerateBitcoins(noOfZeros, noOfAssignedWork)
     }

    case TotalBitcoins(totalCoins: ListBuffer[Tuple2[String, String]]) =>
      {
       noOfAssignedWork += 1
        println("****Bitcoins received : " + (noOfAssignedWork - 1))
        bitListBuffer = bitListBuffer ++ totalCoins
        sender ! GenerateBitcoins(zeros, noOfAssignedWork)
       
      }

     
    case current: ClusterEvent.CurrentClusterState =>
      val notMe = current.members.toVector map (_.address) filter (_ != cluster.selfAddress)
      if (notMe.nonEmpty) context.become(active(notMe))
    case ClusterEvent.MemberUp(member) if member.address != cluster.selfAddress =>
      startClientMaster(member.address)
      context.become(active(Vector(member.address)))

  }

  def startClientMaster(node: Address) {
    noOfAssignedWork += 1
   
    val server = sender
    

    val props = Props[Master].withDeploy(Deploy(scope = RemoteScope(node)))

   

    val master = context.actorOf(props)
    context.watch(master)

   
    master ! GenerateBitcoins(zeros, noOfAssignedWork)
  }

  def active(addresses: Vector[Address]): Receive = {

    case MemberUp(member) if member.address != cluster.selfAddress =>
      startClientMaster(member.address)
      context.become(active(addresses :+ member.address))
    case MemberRemoved(member, _) =>
      val next = addresses filterNot (_ == member.address)
      if (next.isEmpty) context.become(awaitingMembers)
      else context.become(active(next))

    case TotalBitcoins(totalCoins: ListBuffer[Tuple2[String, String]]) =>
      {

        noOfAssignedWork += 1
        //println("****Bitcoins received from Master: " + (noOfAssignedWork - 1) + " Nos***************")
        println("data received from :" + sender.path.name)
        bitListBuffer = bitListBuffer ++ totalCoins
        sender ! GenerateBitcoins(zeros, noOfAssignedWork)
        
      }

     /* context.system.scheduler.scheduleOnce(time.seconds) {
       
        context.parent ! TotalBitcoins(bitListBuffer)
        context.stop(self)
     

      }*/

  }
}
 
  