import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import java.security._
import java.security.MessageDigest
import collection.mutable.ListBuffer
import akka.cluster.Cluster


class Master extends Actor {
  
  var bitListBuffer = ListBuffer[Tuple2[String,String]]()
  var noOfResults : Int = 0
  val noOfWorkers = ((Runtime.getRuntime().availableProcessors())*1.5).toInt
  val noOfMessages = ((1.67)*noOfWorkers).toInt
  val inputSize = 1000000
  val length = (inputSize/noOfMessages).toInt
  var initial = 0
  var check = noOfMessages
  var round = 0
  val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(noOfWorkers)), name = "workerRouter")
  
  
  def receive = {
    
    case GenerateBitcoins(noOfZeros : Int, count : Int) => 
      { round+=1 
        check = noOfMessages*round
   
        
        
        initial = count*inputSize
        
        
        for(i <- 0 until noOfMessages) 
        { 
          
          workerRouter ! InitiateGeneration("sanchitsharma", initial+1 , initial + length , noOfZeros )  
          
          initial = initial + length
      
       
        }
        
      
   
      }
    case BitcoinsGenerated(keys : ListBuffer[Tuple2[String, String]]) =>
      bitListBuffer = bitListBuffer ++ keys	
      
      noOfResults +=1
      
      
      
      
      if ( noOfResults == check)
      {  println("sending data............")
         context.parent ! TotalBitcoins(bitListBuffer)
        
        
      }
    
  }
}
  