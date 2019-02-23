package com.neo.sk.break.core

import java.awt.event.KeyEvent

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.neo.sk.break.shared.Protocol._
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * User: gaohan
  * Date: 2019/2/11
  * Time: 14:34
  */
trait PlayGround {

  def joinGame(id: String):Flow[UserAction, GameMessage, Any]

  def syncData()

}

object PlayGround {

  val log = LoggerFactory.getLogger(this.getClass)

  def create(system: ActorSystem)(implicit executor: ExecutionContext):PlayGround ={

    val ground = system.actorOf(Props(new Actor {

      var subscribers = Map.empty[String, ActorRef]

      //(userId,(userName,roomId))
      var userMap = Map.empty[String,Long]

      //(roomId,(userNumber,grid))
      var roomMap = Map.empty[Long,(Int,GridOnServer)]

      var roomNum = -1
      val maxRoomNum = 2

      var tickCount = 0l

      override def receive: Receive = {
        case r@Join (id,subscriber) =>
          log.info(s"got $r")

          val roomId = if(!roomMap.exists(_._2._1 < maxRoomNum)){
            roomNum += 1
            roomNum
          } else {
            roomMap.filter(_._2._1 < maxRoomNum).head._1
          }

          if(userMap.filter(r => r._2 == roomId && r._1 == id).isEmpty){
            userMap += (id -> roomId)
            if(roomMap.contains(roomId)){
              roomMap += (roomId -> (roomMap.get(roomId).head._1 + 1, roomMap.get(roomId).head._2))
            } else{
              val grid = new GridOnServer()
              roomMap += (roomId -> (1,grid))
            }
            context.watch(subscriber)
            subscribers += (id -> subscriber)
            roomMap(roomId)._2.addPlayer(id,roomId)
            roomMap(roomId)._2.genPlayer() //生成玩家对应的小球和挡板
            roomMap(roomId)._2.genBrick(roomId) //生成一个房间内的砖块
           // println(roomMap(roomId)._2.getGridSyncData)
            dispatchTo(id, Id(id))
            dispatch(NewPlayerJoined(id,roomId), roomId)
            dispatch(roomMap(roomId)._2.getGridSyncData, roomId)

          }else{
            //subscriber ! NewPlayerNameExist(id,roomId)
          }

        case r@Left(id) =>
          log.info(s"got $r")
          subscribers.get(id).foreach(context.unwatch)
          subscribers -= id
          if(userMap.get(id).nonEmpty){
            val roomId = userMap(id)
            roomMap(roomId)._2.removePlayer(id)
            userMap -= id
            if(roomMap(roomId)._1 - 1 <= 0){
              roomMap -= roomId
            } else{
              roomMap += (roomId -> (roomMap(roomId)._1 - 1, roomMap(roomId)._2))
            }
            dispatch(PlayerLeft(id), roomId)
          }

        case userAction: UserAction => userAction match {
          case r@Key(id, keyCode, frame) =>
            val roomId = userMap(id)
            val grid = roomMap(roomId)._2
            if(keyCode == KeyEvent.VK_SPACE) {
             // grid.genPlayer()
              grid.initAction(id)
             // grid.addPlayer(id,roomId)

            } else if(keyCode == KeyEvent.VK_1)  {
              println("reStart!!!!!!!!!!!!!!!")
              grid.genPlayer()
            } else {
              if(frame >= grid.frameCount) {
                grid.addActionWithFrame(id, keyCode,frame)
                dispatch(PlayerAction(id,keyCode,frame), roomId)
              } else if(frame >= grid.frameCount - savingFrame + advanceFrame) {
                grid.addActionWithFrame(id, keyCode,grid.frameCount)
                dispatchDistinct(id, DistinctPlayerAction(keyCode,grid.frameCount, frame), PlayerAction(id, keyCode, grid.frameCount),roomId)
                log.info(s"key delaysss: server: ${grid.frameCount} client:$frame ")
              }else{
                log.info(s"key delay: server: ${grid.frameCount} client:$frame ")
              }
            }

          case _ =>
        }

        case Sync =>
          tickCount += 1
          roomMap.foreach{ room =>
            val grid = room._2._2
            val roomId = room._1
            grid.update(false)
            if(tickCount % 20 == 5) {
              val GridSyncData = grid.getGridSyncData
              dispatch(GridSyncData,roomId)
            }

          }

        case r@Terminated(actor) =>
          log.warn(s"got $r")
          subscribers.find(_._2.equals(actor)).foreach{ case (id, _) =>
            log.info(s"got Terminated id = $id")
            if(userMap.exists(_._1 == id)){
              val roomId = userMap(id)
              val grid = roomMap(roomId)._2
              subscribers -= id
              userMap -= id
              grid.removePlayer(id)
              dispatch(PlayerLeft(id), roomId)
              if(roomMap(roomId)._1 - 1 <= 0){
                roomMap -= roomId
              } else{
                roomMap += (roomId -> (roomMap(roomId)._1 - 1, roomMap(roomId)._2))
              }
            }
          }

        case x =>
          log.info(s"got unknown msg: $x")
      }

      def dispatch(gameOutPut: GameMessage, roomId: Long) = {
        val user = userMap.filter(_._2 == roomId).keys.toList
        subscribers.foreach{
          case(id,ref)
            if user.contains(id) => ref ! gameOutPut
          case _ =>
        }
      }

      def dispatchTo(id: String, gameOutPut: GameMessage):Unit = {
        subscribers.get(id).foreach{ ref => ref ! gameOutPut}
      }

      def dispatchDistinct(distinctId: String, distinctGameOutPut: GameMessage, gameOutPut: GameMessage, roomId: Long):Unit ={
        val user = userMap.filter(_._2 ==roomId).keys.toList
        subscribers.foreach{
          case(id, ref) if user.contains(id) =>
            if(id != distinctId){
              ref ! gameOutPut
            } else {
              ref ! distinctGameOutPut
            }
          case _  =>

        }
      }

    }
    ), "ground")

    import concurrent.duration._
    system.scheduler.schedule(3 seconds, frameRate millis, ground, Sync) // sync tick

    //在终端中的游戏
    //sink.actorRef:方法将数据流中的数据发给指定的actorRef，完成时给目的actor发一个left Message
    def playInSink (id: String): Sink[UserAction,NotUsed] = Sink.actorRef[UserAction](ground, Left(id))
     // source为数据的源头，数据发布方，sink:为数据的终端，数据适用方，flow：数据处理节点
    new PlayGround {
      //Flow[in, out mat]
      override def joinGame(id: String): Flow[UserAction, GameMessage, Any] = {
        val in =
          Flow[UserAction]
          .map { s =>
            s
          }.to(playInSink(id))
        // to方法将flow 连接到sink端
        val out =
          Source.actorRef[GameMessage](3, OverflowStrategy.dropHead)
          .mapMaterializedValue(outActor => ground ! Join(id,outActor))

        Flow.fromSinkAndSource(in, out)
      }

      override def syncData(): Unit = ground ! Sync
    }
  }

  private case class Join(id: String, subscriber: ActorRef)
  private case class Left(id: String)
  private case object Sync

}
