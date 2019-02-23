package com.neo.sk.front.scalajs

import com.neo.sk.break.shared.Protocol._
import com.neo.sk.front.pages.{FirstPage, LoginPage, RegisterPage}
import com.neo.sk.front.style.{LoginStyle, RegisterStyle}
import com.neo.sk.front.utils.PageSwitcher
import mhtml.{Cancelable, mount}

import scala.scalajs.js
import scala.scalajs.js.typedarray.ArrayBuffer
import org.scalajs.dom
import org.scalajs.dom.ext.{Color, KeyCode}
import org.scalajs.dom.html.{Document => _, _}
import org.scalajs.dom.raw._
import com.neo.sk.break.shared._
import org.seekloud.byteobject._
import org.seekloud.byteobject.ByteObject._
import com.neo.sk.front.scalajs.DrawGame._
import java.util.concurrent.atomic.AtomicInteger

/**
  * User: gaohan
  * Date: 2019/2/12
  * Time: 16:05
  */
object NetGameHolder extends js.JSApp with PageSwitcher{

  var wsSetup = false
  var justSynced = false
  var myId = ""
  var myRoomId = 0
  var updateCounter = 0l

  var savedGrid = Map[Long,GridDataSync]()
  var syncData: scala.Option[GridDataSync] = None
  val grid = new GridOnClient
  var basicTime = 0l

  var gameLoopControl = 1.0 // 保存game Loop 的setInterval的ID
  var nextAnimation = 0.0 //保存requestAnimationFrame 的ID

  val idGenerator = new AtomicInteger(1000)

  val watchKeys = Set(
    KeyCode.Space,
    KeyCode.Left,
    KeyCode.Right
  )
  var currentPage = currentHashVar.map { ls =>
    println(s"currentPage change to ${ls.mkString(",")}")
    ls match {
      case "index" :: Nil => FirstPage.app
      case "login":: Nil => LoginPage.app
      case "register"::  Nil => RegisterPage.app
      case "game":: Nil => LoginPage.app
      case _ => FirstPage.app

    }
  }
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {

    import scalacss.ProdDefaults._
    LoginStyle.addToDocument()
    RegisterStyle.addToDocument()
    show()


    DrawGame.canvas.focus()
    dom.window.requestAnimationFrame(drawLoop())

  }
  def update(isSynced:Boolean):Unit = {
    grid.update(isSynced: Boolean)
  }

  // 逻辑帧
  def gameLoop(): Unit = {
    basicTime = System.currentTimeMillis()
    if(wsSetup) {
      if(!justSynced) {
        update(false)
      } else {
        sync(syncData)
        syncData = None
        update(true)
        justSynced = false
      }
    }
    savedGrid += (grid.frameCount -> grid.getGridSyncData)
    savedGrid -= (grid.frameCount - savingFrame - advanceFrame)
  }

  //开始逻辑帧的计算
  def startLoop(): Unit = {
    gameLoop()
    gameLoopControl = dom.window.setInterval(() => gameLoop(),frameRate)
  }

  //动画帧
  def drawLoop(): Double => Unit = { _ =>
    draw()
    nextAnimation = dom.window.requestAnimationFrame(drawLoop())
  }

  // 画一帧
  def draw(): Unit = {
    if(wsSetup) {
      val data = grid.getGridSyncData
      DrawGame.drawGrid(myId,data)
    }  else {
      DrawGame.drawGameOff()
    }

  }

  def show(): Cancelable = {
    switchPageByHash()
    val page =
      <div>
        {currentPage}
      </div>
    mount(dom.document.body, page)
  }

  val sendBuffer = new MiddleBufferInJs(4096000)

  def joinGame(id: String): Unit ={

//    println(idGenerator.getAndIncrement().toLong)
//    println(idGenerator.getAndIncrement().toLong)

//    if(isSecret){
//      Protocol.isSecret = true
//    }
   // val playground = dom.document.getElementById("playground")
    println(id)
    val gameStream = new WebSocket(getWebSocketUri(dom.document,id))
   // println(s"player${idGenerator.getAndIncrement().toLong}")
    gameStream.onopen = { (event0:Event) =>
//      dom.window.setInterval(() =>{
//        //pingMsg
//      },Protocol.netInfoRate )
      println("onOpen")
      DrawGame.drawGameOn()
      wsSetup = true
      DrawGame.canvas.focus()
      canvas.onkeydown = {
        (e: dom.KeyboardEvent) => {
          if(watchKeys.contains(e.keyCode)) {
           // e.preventDefault()
            grid.addActionWithFrame(myId, e.keyCode, grid.frameCount + operateDelay)
            val msg: UserAction =
              Key(myId, e.keyCode, grid.frameCount + advanceFrame + operateDelay)
//            if(e.keyCode == KeyCode.Space){
//              grid.initAction(myId)
//            }
            msg.fillMiddleBuffer(sendBuffer)
            val ab:ArrayBuffer = sendBuffer.result()
            gameStream.send(ab)
          }
        }
      }
      event0
    }

    gameStream.onerror = { (event1: Event) =>
      DrawGame.drawGameOff()
      println("onerror")
      wsSetup = false
    }

    gameStream.onmessage = { (event: MessageEvent) =>
      println("onmessage")
      event.data match {
        case blobMsg: Blob =>
          val fr = new FileReader()
          fr.readAsArrayBuffer(blobMsg)
          fr.onloadend = { _:Event =>
            val buf = fr.result.asInstanceOf[ArrayBuffer] // read data from ws

            val middleDataInJs = new MiddleBufferInJs(buf) //put data into MiddleBuffer
            val encodeData: Either[decoder.DecoderFailure, Protocol.GameMessage] =
              bytesDecode[Protocol.GameMessage](middleDataInJs) // get encoded data
            encodeData match {
              case Right(data) =>
                data match{
                 case Protocol.Id(id) => myId = id
                 case Protocol.TextMsg(msg) =>
                   println(msg)
                 case Protocol.NewPlayerJoined(id,roomId) =>
                   myRoomId = roomId.toInt + 1
                 case Protocol.PlayerLeft(id) =>
                 case Protocol.PlayerAction(id, keyCode, frame) =>
                   if(id == myId) {
                    grid.addActionWithFrame(id, keyCode, frame)
                   }
                 case Protocol.DistinctPlayerAction(keyCode,frame,frontFrame) =>
                   println("DisPlayAction =" + keyCode)
                   val savedAction = grid.actionMap.get(frontFrame - advanceFrame)
                   if(savedAction.nonEmpty) {
                     val delAction = savedAction.get - myId
                     val addAction = grid.actionMap.getOrElse(frame - advanceFrame, Map[String,Int]()) + (myId -> keyCode)
                     grid.actionMap += (frontFrame - advanceFrame -> delAction)
                     grid.actionMap += (frame - advanceFrame -> addAction)
                     updateCounter = grid.frameCount - (frontFrame - advanceFrame)
                     sync(savedGrid.get(frontFrame - advanceFrame))
                     for(_ <- 1 to updateCounter.toInt) {
                       update(false)
                     }

                   }
                 case data:GridDataSync =>
                    if(!grid.init) {
                     // println(s"data" + data)
                      grid.init = true
                      val timeOut = 100 - (System.currentTimeMillis() - data.timeStamp) % 100
                      dom.window.setTimeout(() => startLoop(), timeOut)
                    }
                    syncData = Some(data)
                    justSynced = true

                }
              case Left(e) =>
                println(s"got error: ${e.message}")
            }
          }
      }

    }

    gameStream.onclose = {(event: Event) =>
      DrawGame.drawGameOff()
      wsSetup = false
    }

  }

  def getWebSocketUri(document:Document, nameOfChatParticipant: String) : String = {
    val wsProtocol = if(dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}/break/gaming?id=$nameOfChatParticipant"
  }

  def p(msg:String) = {
    val paragraph = dom.document.createElement("p")
    paragraph.innerHTML = msg
    paragraph
  }

  def sync (dataOpt: scala.Option[GridDataSync]) = {
    if(dataOpt.nonEmpty){
      val data = dataOpt.get
      val presentFrame  = grid.frameCount
      grid.frameCount = data.frameCount
      grid.balls = data.balls.map(s => s.id -> s).toMap
      grid.boards = data.boards.map(s => s.id -> s).toMap
      grid.field = data.field
      if(data.frameCount <= presentFrame) {
        for(_ <- presentFrame to data.frameCount) {
          grid.update(false)
        }

        val myBallOpt = grid.balls.find(_._1 == myId)
        val myBoardOpt = grid.boards.find(_._1 == myId)
        if(myBallOpt.nonEmpty) {
          var myBall = myBallOpt.get._2
          var myBoard = myBoardOpt.get._2
          for(i <- advanceFrame to 1 by -1) {
            val info = grid.updateAPlayer(myBoard,myBall, grid.actionMap.getOrElse(data.frameCount - i, Map.empty))
            myBall = info._1
            myBoard = info._2
            }
             grid.balls += ((myBall.id, myBall))
             grid.boards += ((myBoard.id, myBoard))
          }
          grid.field = data.field

        }
      }
    }

}
