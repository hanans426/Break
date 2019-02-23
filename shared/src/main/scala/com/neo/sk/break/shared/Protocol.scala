package com.neo.sk.break.shared

import java.util.concurrent.atomic.AtomicInteger

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException


/**
  * User: gaohan
  * Date: 2019/2/11
  * Time: 13:51
  */
object Protocol {

  sealed trait GameMessage

  case class GridDataSync(
    frameCount: Long,
    boards: List[BoardsInfo],
    balls: List[BallsInfo],
   // liveField: List[List[BrickInfo]],
    field: List[List[BrickInfo]],
    deadBrick: List[(Int,Int)],
    timeStamp: Long
  ) extends GameMessage

  case class TextMsg(msg: String) extends GameMessage

  case class Id(id: String) extends GameMessage

//  case class NewPlayerNameExist(id: String, roomId: Long) extends GameMessage

  case class NewPlayerJoined(id: String, roomId: Long) extends  GameMessage

  case class PlayerLeft(id: String) extends GameMessage

  case class PlayerAction(id: String, keyCode: Int, frame: Long) extends GameMessage

  case class DistinctPlayerAction(keyCode: Int, frame: Long, frontFrame: Long) extends GameMessage

//  case class NetDelayTest(createTime: Long) extends GameMessage

  sealed trait UserAction

  case class Key(id: String, keyCode:Int, frame: Long) extends UserAction

  case class NetTest(id: String,createTime: Long)

  case class TextInfo(id: String, info: String) extends UserAction

  val frameRate = 100

  val netInfoRate = 1000

  val savingFrame = 5//保存的帧数

  val advanceFrame = 1// 客户端提前的帧数

  val radius = 5 // 圆球的半径

  val operateDelay = 5//操作延迟的帧数

}
