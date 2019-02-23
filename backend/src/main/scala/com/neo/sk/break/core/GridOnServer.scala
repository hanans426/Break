package com.neo.sk.break.core

import java.awt.event.KeyEvent
import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.break.shared
import org.slf4j.LoggerFactory
import com.neo.sk.break.shared.Grid
import com.neo.sk.break.shared._
import com.neo.sk.break.shared.Protocol._

/**
  * User: gaohan
  * Date: 2019/2/12
  * Time: 16:04
  */
class GridOnServer extends Grid {

  private[this] val log = LoggerFactory.getLogger(this.getClass)

  private[this] var waitingJoin = Map.empty[String, Long]

  // private[this] var hitenBrick = Map.empty[String,BrickWithFrame]
  val idGenerator = new AtomicInteger(1000)

  def addPlayer(id:String, roomId: Long) = waitingJoin += (id -> roomId)

  def randomColor() = {
    val a = random.nextInt(10)
    val color = a match {
      case 0 => "#4682B4"
      case 1 => "#CD5C5C"
      case 2 => "#FA8072"
      case 3 => "#FFD700"
      case 4 => "#43CD80"
      case 5 => "#43CD80"
      case 6 => "#EE4000"
      case 7 => "#228B22"
      case 8 => "#EE8262"
      case 9 => "#48D1CC"
    }
    color
  }

  def randomVelocity() = {
    val b = random.nextInt(6)
    val velocityX = b match {
      case 0 => 5f
      case 1 => -5f
      case 2 => 4f
      case 3 => -4f
      case 4 => 4.5f
      case 5 => -4.5f
    }
    velocityX

  }
  def randomPoint() = {
    val c = random.nextInt(6)
    val locationX = c match {
      case 0 => Point(100, 605)
      case 1 => Point(300, 605)
      case 2 => Point(500, 605)
      case 3 => Point(700,605)
      case 4 => Point(900,605)
      case 5 => Point(1100,605)
    }
    locationX

  }

  //玩家进入时生成信息相应的板子和小球
  def genPlayer():Unit = {
    waitingJoin.foreach{ p =>
      val id = p._1
      val color = randomColor()
      val location = randomPoint()
      val newBall = BallsInfo(id, color, 0, 0, 5, 3, Point(location.x + BoardsSize.l/2 ,595), 0)
      val newBoard = BoardsInfo(id, location, Point(0,0), BoardsSize.l, color)
      balls += (id -> newBall)
      boards += (id -> newBoard)
    }
  }

  //创建房间是生成砖块，初始化field,第i行第j列
  def genBrick(roomId:Long)= {
    var genBrick = List.empty[BrickInfo]
    for(i <- 1 to 8) {
      for(j <- 1 to 12){
        val oneBrick = BrickInfo(i, j, 15 - i,Point((j-1) * BrickSize.w, (i -1) * BrickSize.h), true)
        genBrick ::= oneBrick
      }
    }
    field ::= genBrick
  }
  //初始化小球的速度分量
  def initAction(id: String): Unit = {
    val player = balls.get(id)
    if(player.nonEmpty){
      val newVelocityX = randomVelocity()
      val newVelocityY = -5f
      balls += id -> player.get.copy(velocityX = newVelocityX, velocityY = newVelocityY)
    }

  }

  override def update(isSynced: Boolean): Unit = {
    super.update(isSynced)
    waitingJoin = Map.empty[String, Long]
    isXChange = Map.empty[String, Boolean]
    isYChange = Map.empty[String, Boolean]
  }

  override def updatePlayers(): Unit = {
    var updatedBalls = List.empty[BallsInfo]
    var updatedBoards = List.empty[BoardsInfo]
    val acts = actionMap.getOrElse(frameCount,Map.empty[String,Int]) // 此处可以获取到actionMap，且有正确的keycode
    balls.values.map{ ball =>
      boards.values.map { boards =>
        updateAPlayer(boards,ball,acts)
      }
    }.foreach{ rs =>
      rs.foreach{ r =>
        updatedBalls ::= r._1
        updatedBoards ::= r._2
      }

    }
    balls = updatedBalls.map(s => (s.id, s)).toMap
    boards = updatedBoards.map(s => (s.id, s)).toMap
  }

  override def updateAPlayer(board: BoardsInfo, ball: BallsInfo, actMap: Map[String, Int]): (BallsInfo, BoardsInfo) = {
    val keyCode = actMap.get(board.id)
    val newDirection = {
      keyCode match{
        case Some(KeyEvent.VK_LEFT) => Point(-1, 0)
        case Some(KeyEvent.VK_RIGHT) => Point(1, 0)
        case _ => Point(0,0)
      }
    }

    hitBoards(board.id, ball.id)

    val score = hitBrick(ball.id).getOrElse(0)
    //println("score=" +score)
    val life = hitBoundary(ball.id).getOrElse(3)
   // println("life =" +life)
    var newVelocityX = ball.velocityX
    var newVelocityY = ball.velocityY
    isXChange.get(ball.id) match {
      case Some(x) =>
        if(x){
         // println("xxxxxxxxx")
          newVelocityX = - newVelocityX
          isXChange += (ball.id -> false)
        }
      case _ =>
    }
    isYChange.get(ball.id) match {
      case Some(x) =>
        if(x){
         // println("yyyyyyyyy")
          newVelocityY = - newVelocityY
          isYChange += (ball.id -> false)
        }
      case _ =>
    }
    val oldCenter = ball.center
    val newCenter = Point((oldCenter.x + newVelocityX).toInt, (oldCenter.y + newVelocityY).toInt)
    val oldLocation = board.location
    if(oldLocation.x >= 0 && oldLocation.x + BoardsSize.l <= MyBoundary.w) {
      newLocation = Point(oldLocation.x + newDirection.x * boardsSpeed, oldLocation.y + newDirection.y * boardsSpeed)
    } else if(oldLocation.x < 0){
      newLocation = Point(0, 605)
    } else if(oldLocation.x + BoardsSize.l > MyBoundary.w){
      newLocation = Point(MyBoundary.w - BoardsSize.l, 605)
    }

    (ball.copy(velocityX = newVelocityX, velocityY = newVelocityY, life = life, center = newCenter, score = score),
      board.copy(location = newLocation, direction = newDirection ))
  }

  //匹配Id一样的板子和小球，小球碰到板子后改变方向
  override def hitBoards(ballId: String, boardId: String) = {
    if(ballId == boardId){
      //println("match right")
      boards.get(boardId) match {
        case Some(x:BoardsInfo) =>
          balls.get(ballId) match {
            case Some(a:BallsInfo) =>
              if(a.center.y + a.r <= x.location.y && a.center.y >= 600 && a.center.x <= x.location.x + x.length && a.center.x >= x.location.x) {
                yChange = true
                isYChange += (a.id -> yChange)
                //println("hitball="+ a.id + x.id)
              }else{
               // println("miss the ball")
              }
            case _ =>
              println("not hit board")
          }
        case _ =>
      }
    }else{
//      println("board="+boardId)
//      println("ball="+ ballId)

    }
  }
 //判断是否与四周边界相撞，其余三边改变方向，另一边生命值减一
  override def hitBoundary(ballId: String):Option[Int] = {
    balls.get(ballId) match {
      case Some(x: BallsInfo) =>
        if(x.center.x - x.r <= 0 || x.center.x + x.r >= MyBoundary.w){
         // println("hitBoundarySide")
          xChange = true
          isXChange += (x.id -> xChange )
        } else if (x.center.y - x.r == 0){
         // println("hitUp")
          yChange = true
          isYChange += (x.id -> yChange)
        } else if (x.center.y == MyBoundary.h){
          x.life = x.life -1
          //println("life = "+x.life)
          ballLife = x.life

        }
    }
    //println("ballllife = " + ballLife)
    Some(ballLife)
  }


  // 小球碰撞砖块后，将砖块从grid中移除，并且得到小球的分数和速度的方向改变值
  override def hitBrick(ballId: String):Option[Int] = {
    field.foreach{ bricks =>
      bricks.foreach{ brick =>
        val i = brick.indexI
        val j = brick.indexJ
        if(brick.life) {
          balls.get(ballId) match {
            case Some(x: BallsInfo) =>
              if (x.center.x <= brick.location.x + BrickSize.w + x.r && x.center.x >= brick.location.x - x.r && x.center.y == brick.location.y - x.r) {
                //println("hitBrickUp")
                yChange = true
                isYChange += (x.id -> yChange)
                totalScore += brick.score
                brick.life = false
              } else if (x.center.x <= brick.location.x + BrickSize.w + x.r && x.center.x >= brick.location.x - x.r && x.center.y == brick.location.y + BrickSize.h + x.r) {
                //println("hitBrickDown")
                yChange = true
                isYChange += (x.id -> yChange)
                totalScore += brick.score
                brick.life = false
              } else if (x.center.y <= brick.location.y + BrickSize.h + x.r && x.center.y >= brick.location.y - x.r && x.center.x == brick.location.x - x.r) {
                // println("hitBrickLeft")
                xChange = true
                isXChange += (x.id -> xChange)
                totalScore += brick.score
                brick.life = false
              } else if (x.center.y <= brick.location.y + BrickSize.w + x.r && x.center.y >= brick.location.y - x.r && x.center.x == brick.location.x + BrickSize.w + x.r) {
                xChange = true
                isXChange += (x.id -> xChange)
                // println("hitBrickRight")
                totalScore += brick.score
                brick.life = false
              } else {
              }
            case _ =>
          }

        }
        if(!brick.life){
          deadBricks ::= (i, j)
        }


      }
    }
    Some(totalScore)
  }


}
