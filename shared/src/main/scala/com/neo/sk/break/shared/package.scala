package com.neo.sk.break


/**
  * User: gaohan
  * Date: 2019/2/12
  * Time: 16:07
  */
package object shared {

  case class BoardsInfo(
    id: String,
    location: Point,
    direction: Point,
    length: Int = 100,
    color: String,
  )

  case class BallsInfo(
    id: String,
    color: String,
    velocityX: Float,
    velocityY: Float,
    r: Int = 10,
    var life: Int = 3,
    center: Point,
    var score: Int
  )

  case class isChange(var xChange: Boolean, var yChange: Boolean)

  case class Brick(index: Int, score: Int, life: Boolean)

  case class BrickInfo(
    indexI: Int,
    indexJ: Int,
    score: Int,
    location: Point,
    var life: Boolean
  )

  case class DeadPlayerInfo(
    id: String,
    score: Int,
  )

  case class BrickWithFrame(
    frameCount: Long,
    brick: BrickInfo
  )

  case class Rectangle(
    leftUp: Point,
    leftDown: Point,
    rightUp: Point,
    rightDown: Point
  )


  case class Point(x: Int,y: Int){
    def +(other: Point) = Point(x + other.x, y + other.y)

    def -(other: Point) = Point(x - other.x, y - other.y)

    def *(n: Int) = Point(x * n, y * n)

    def /(n: Int) = Point(x / n, y / n)

    def %(other: Point) = Point(x % other.x, y % other.y)

    def to (other: Point) = {
      val (x0, x1) = if(x > other.x)(other.x, x) else (x, other.x)
      val (y0, y1) = if(y > other.y)(other.y, y) else (y, other.y)
      val list = (for{
        xs <- x0 to x1
        ys <- y0 to y1
      }yield {
        Point(xs,ys)
      }).toList
      if(other.y == y) {
        if(other.x > x) list.sortBy(_.x)else list.sortBy(_.x).reverse
      } else {
        if(other.y > y) list.sortBy(_.y)else list.sortBy(_.y).reverse
      }
    }

//    def zone(range: Int) = (for {
//      xs <- x - range to x + range
//      ys <- y - range to y + range
//    } yield {
//      Point(xs, ys)
//    }).toList
    def zone(rangeX: Int, rangeY: Int) = (for {
      xs <- x to x + rangeX
      ys <- y to y + rangeY
    } yield {
      Point(xs, ys)
    }).toList

  }

  object Boundary {
    val w = 1500
    val h = 900
  }

  object MyBoundary{
    val w = 1200
    val h = 700
  }

  object BrickSize{
    val w = 100
    val h = 50
  }

  object BoardsSize{
    val l = 100
    val h = 10
  }

  val boundaryWidth = 3

  val boardsSpeed = 20
//
//
//  val secretId = s"player${idGenerator.getAndIncrement().toLong}"

  object ColorSetting {
    val bgGround1 = "#F5FFFA"
    val errGround = "#2F4F4F"
    val font = "#F5F5F5"
    val brick1 = "#8B475D"
    val brick2 = "#CD6889"
    val brick3 = "#EE799F"
    val brick4 = "#FF82AB"
    val brick5 = "#F08080"
    val brick6 = "#FFB6C1"
    val brick7 = "#EED5D2"
    val brick8 = "#E6E6FA"
    val errBrick = "#FFFAFA"
    val boundary = "#DCDCDC"


  }



}
