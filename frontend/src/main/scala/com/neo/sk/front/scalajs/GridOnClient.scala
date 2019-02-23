package com.neo.sk.front.scalajs
import java.awt.event.KeyEvent

import com.neo.sk.break.shared._
import com.neo.sk.break.shared.Grid



/**
  * User: gaohan
  * Date: 2019/2/12
  * Time: 16:05
  */
class GridOnClient extends Grid{


  override def update(isSynced: Boolean): Unit = {
    super.update(isSynced: Boolean)
  }

  override def updatePlayers(): Unit = {
    var updatedBalls = List.empty[BallsInfo]
    var updatedBoards = List.empty[BoardsInfo]
    val acts = actionMap.getOrElse(frameCount,Map.empty[String,Int])
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
    var life = hitBoundary(ball.id).getOrElse(3)
   // println(life)
    var newVelocityX = ball.velocityX
    var newVelocityY = ball.velocityY
//    if(xChange){
//      newVelocityX = - newVelocityX
//      xChange = false
//    }
//    if(yChange){
//      newVelocityY = - newVelocityY
//      yChange = false
//    }
    isXChange.get(ball.id) match {
      case Some(x) =>
        if(x){
          newVelocityX = - newVelocityX
          isXChange += (ball.id -> false)
        }
      case _ =>
    }
    isYChange.get(ball.id) match {
      case Some(x) =>
        if(x){
          newVelocityY = - newVelocityY
          isYChange += (ball.id -> false)
        }
      case _ =>
    }
    val oldCenter = ball.center
    val newCenter = Point((oldCenter.x + newVelocityX).toInt, (oldCenter.y + newVelocityY).toInt)
    val oldLocation = board.location
    //var newLocation = Point(oldLocation.x, oldLocation.y)
    if(oldLocation.x >=0 && oldLocation.x + BoardsSize.l <= MyBoundary.w) {
      newLocation = Point(oldLocation.x + newDirection.x * boardsSpeed, oldLocation.y + newDirection.y * boardsSpeed)
    } else if(oldLocation.x < 0){
      newLocation = Point(0, 605)
    } else if(oldLocation.x + BoardsSize.l > MyBoundary.w){
      newLocation = Point(MyBoundary.w - BoardsSize.l, 605)
    }
    (ball.copy(velocityX = newVelocityX, velocityY = newVelocityY, life = life, center = newCenter, score = score),
      board.copy(location = newLocation  , direction = newDirection ))
  }

  //匹配Id一样的板子和小球，小球碰到板子后改变方向
  //override def hitBoards(ballId: String, boardId: String) = None
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
                // println("hitBoards")
              }else{
                // println("miss the ball")
              }
            case _ =>
              println("not hit board")
          }
        case _ =>
      }
    }else{

    }
  }
  //判断是否与四周边界相撞，其余三边改变方向，另一边生命值减一
 // override def hitBoundary(ballId: String):Option[Int] = None
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

     Some(ballLife)
   }

  // 小球碰撞砖块后，将砖块从grid中移除，并且得到小球的分数和速度的方向改变值
 // override def hitBrick(ballId: String):Option[Int] = None

  var init: Boolean = false

  override def hitBrick(ballId: String):Option[Int] = {
    //var bricks = List.empty[BrickInfo]
    // println("aaaa="+ field)
    field.foreach{ bricks =>
      bricks.foreach{ brick =>
        val i = brick.indexI
        val j = brick.indexJ
        //var life = brick.life
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
