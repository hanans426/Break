package com.neo.sk.break.shared
import scala.util.Random
import java.util.concurrent.atomic.AtomicInteger

/**
  * User: gaohan
  * Date: 2019/2/12
  * Time: 16:06
  */
trait Grid {


  val random = new Random(System.nanoTime())
  //(frameCount,(id,keyCode))
  var actionMap = Map.empty[Long, Map[String, Int]]

  var frameCount = 0l
  var field = List.empty[List[BrickInfo]]
  var deadBricks = List.empty[(Int, Int)]
  var balls = Map.empty[String, BallsInfo]
  var boards = Map.empty[String, BoardsInfo]
  var deadList = List.empty[DeadPlayerInfo]
  var totalScore = 0
  var ballLife = 3
  var newLocation = Point(0,0)
 // var isChange = Map.empty[String, isChange]
  var isXChange = Map.empty[String, Boolean]
  var isYChange = Map.empty[String, Boolean]
//  var a = isChange.get("id").getOrElse()
  var xChange: Boolean = false
  var yChange:Boolean = false

  def removePlayer(id: String) = {
    val r = boards.get(id)
    if(r.isDefined){
      boards -= id
      balls -= id
    }
  }

  def addActionWithFrame(id: String, keyCode: Int, frame: Long) = {
    val map = actionMap.getOrElse(frame, Map.empty)
    val tmp = map + (id -> keyCode)
    actionMap += (frame -> tmp)
  }

  def update(isSynced: Boolean) = {
    if(!isSynced) {
      updatePlayers()
    } else{
      frameCount -= 1
    }
    actionMap -= (frameCount - Protocol.savingFrame)
    frameCount += 1
  }


  def updatePlayers(): Unit

  def updateAPlayer(board: BoardsInfo, ball: BallsInfo, actMap: Map[String,Int]):(BallsInfo,BoardsInfo)

  def hitBrick(ballId: String):Option[Int]

  def hitBoundary(ballId: String):Option[Int]

  def hitBoards(ballId: String, BoardId: String):Unit

  def getGridSyncData = {

    Protocol.GridDataSync(
      frameCount,
      boards.values.toList,
      balls.values.toList,
      field,
      deadBricks,
      System.currentTimeMillis()
    )


  }
}


