import akka.actor._
import com.typesafe.config.ConfigFactory



object Main {

  def main(args : Array[String]) = {
    
    
    if (args(0).length<2) 
    { 
      val noOfZeros = Integer.parseInt(args(0))
      val system = ActorSystem("project1")		  	
      val server = system.actorOf(Props(new Server(noOfZeros)), "BitCoinsServer")
    }
      else 
      { System.setProperty("java.net.preferIPv4Stack", "true")
        val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").withFallback(
        				ConfigFactory.load())
        val system = ActorSystem("project1",config)
        val client = system.actorOf(Props(new RemoteNode(args(0))), "BitCoinsClient")
      }
    
  }
 
}