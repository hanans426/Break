package com.neo.sk.front.scalajs

import com.neo.sk.break.shared.Protocol._
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import com.neo.sk.break.shared._
import com.neo.sk.front.scalajs.NetGameHolder._
import com.neo.sk.break.shared._

/**
  * User: gaohan
  * Date: 2019/2/16
  * Time: 12:38
  */
object DrawGame {

//  val windowWidth = Boundary.w
//  val windowHeight = Boundary.h


  val canvas = dom.document.getElementById("GameView").asInstanceOf[Canvas]
//  canvas.setAttribute("style",s"position:absolute;z-index:0;left: 0px; top:0px;background: rgba(240, 255, 240, 1);height:${Boundary.h}px;width:${Boundary.w}px")
  val infoCanvas = dom.document.getElementById("GameInfo").asInstanceOf[Canvas]
  val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  val infoCtx = infoCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
 // infoCanvas.setAttribute("style",s"position:absolute;z-index:1;left: 0px; top: 0px;background: rgba(0, 0, 0, 0.8);height:${Boundary.h}px;width:${Boundary.w}px")
  var myScore = 0
  val canvasSize = Point(dom.document.documentElement.clientWidth, dom.document.documentElement.clientHeight)
  def drawGameOff(): Unit = {
   // canvas.setAttribute("style",s"position:absolute;z-index:0;left: 0px; top:0px;background: rgba(240, 255, 240, 1);height:${Boundary.h}px;width:${Boundary.w}px")
    ctx.fillStyle = ColorSetting.bgGround1
    ctx.fillRect(0, 0, canvasSize.x, canvasSize.y)
    ctx.fillStyle = ColorSetting.font
    ctx.font = "36px Helvetica"
    ctx.fillText("Ops, connection lost.", canvasSize.x / 2, canvasSize.y / 2)
  }

  def drawGameOn():Unit = {
   // canvas.setAttribute("style",s"position:absolute;z-index:0;left: 0px; top:0px;background: rgba(240, 255, 240, 1);height:${Boundary.h}px;width:${Boundary.w}px")
    ctx.fillStyle = ColorSetting.bgGround1
   // ctx.fillRect(0, 0, windowWidth, windowHeight)

  }

  def drawGrid(uid: String, data: GridDataSync): Unit = {

    canvas.setAttribute("style",s"position:absolute;z-index:0;left: 0px; top:0px;background: rgba(240, 255, 240, 1);height:${canvasSize.y}px;width:${canvasSize.x}px")
    canvas.width = canvasSize.x
    canvas.height = canvasSize.y

    val period = (System.currentTimeMillis() - basicTime).toInt
    //画边界
    ctx.translate(100,100)
    ctx.fillStyle = ColorSetting.boundary
    ctx.shadowBlur = 5
    ctx.shadowColor = "#FFFFFF"
    ctx.fillRect(-10, -50, MyBoundary.w + 20, boundaryWidth)
    ctx.fillRect(-10, -50, boundaryWidth,MyBoundary.h + 50)
    ctx.fillRect(MyBoundary.w + 10, -50, boundaryWidth, MyBoundary.h + 50)
    ctx.fillRect(-10, MyBoundary.h, MyBoundary.w + 20, boundaryWidth)

    val balls = data.balls
    val boards = data.boards
    val bricks = data.field
   // println(bricks)
    val playerNum = boards.size
    val deadBrick = data.deadBrick

    //println(deadBrick)

     bricks.foreach{ oneField =>
       oneField.foreach{ brick =>
         val i = brick.indexI
         val j = brick.indexJ
         if(!deadBrick.contains((i,j))){
        // if(brick.life){
           ctx.fillStyle = brick.score match  {
             case 14 => ColorSetting.brick1
             case 13 => ColorSetting.brick2
             case 12 => ColorSetting.brick3
             case 11 => ColorSetting.brick4
             case 10 => ColorSetting.brick5
             case 9 => ColorSetting.brick6
             case 8 => ColorSetting.brick7
             case 7 => ColorSetting.brick8
             case _ => ColorSetting.errBrick
           }
//           println("brickX =" + brick.location.x)
//           printlnntln("brickY = " + brick.location.y)
           ctx.fillRect(brick.location.x, brick.location.y, BrickSize.w,BrickSize.h)
         } else {
           ctx.clearRect(brick.location.x, brick.location.y, BrickSize.w,BrickSize.h)
         }

       }
    }

    balls.foreach{ ball =>
      val id = ball.id
      val r = ball.r
      val score = ball.score
      val color = ball.color
      val life = ball.life
      val ballX = ball.center.x + ball.velocityX * period / frameRate
      val ballY = ball.center.y + ball.velocityY * period / frameRate
//      println("ballX=" + ballX)
//      println("ballY=" + ballY)
      ctx.beginPath()
      ctx.strokeStyle = color
      ctx.arc(ballX, ballY, r, 0, Math.PI * 2 )
      ctx.fillStyle = color
      ctx.fill()
      ctx.stroke()
      ctx.closePath()
      if(id == uid){
        ctx.save()
        ctx.font =  "24px Helvetica"
        ctx.fillText(s"score:${score}",-100, 100)
        ctx.fillText(s"life:${life}", -100, 200)
        ctx.restore()
        if(ball.center.y >= 605){
          ctx.clearRect(0,0,1200, 700)
          ctx.save()
          ctx.fillStyle = ColorSetting.warnFont
          ctx.font = "36px Helvetica"
          ctx.fillText("you are dead, please try again!", 400, 300)
          ctx.restore()
        }
      }


    }

    boards.foreach { board =>
      val id = board.id
      if (board.location.x >= 0 && board.location.x + BoardsSize.l <= MyBoundary.w) {
        ctx.clearRect(board.location.x, 605, BoardsSize.l, BoardsSize.h)
        val boardX = board.location.x + board.direction.x * boardsSpeed
        // val boardY = board.location.y + board.direction.y * period / frameRate
        ctx.fillStyle = board.color
        ctx.beginPath()
        ctx.strokeStyle = board.color
        ctx.fillRect(boardX, 605, BoardsSize.l, BoardsSize.h)
        ctx.fillText(s"Id:${id}", boardX, 625)
        ctx.stroke()
        ctx.closePath()
      } else if(board.location.x <0){
        ctx.fillRect(0, 605, BoardsSize.l,BoardsSize.h)
        ctx.fillText(id, 0, 625)
      } else if(board.location.x + BoardsSize.l > MyBoundary.w){
        ctx.fillRect(MyBoundary.w - BoardsSize.l, 605, BoardsSize.l, BoardsSize.h)
        ctx.fillText(id,MyBoundary.w - BoardsSize.l, 605)

      }

    }
    ctx.fillStyle = ColorSetting.warnFont
    ctx.font = "12px Helvetica"
    ctx.fillText("一些说明:", 1200, 10)
    ctx.fillText("1.按下空格，启动游戏", 1200, 30)
    ctx.fillText("2.板子只能接到相同颜色的小球", 1200, 50)
    ctx.fillText("3.暂时只能通过刷新重新加入", 1200, 70)
    ctx.fillText("4.进行一段时间后会很卡.....", 1200, 90)

  }

}
