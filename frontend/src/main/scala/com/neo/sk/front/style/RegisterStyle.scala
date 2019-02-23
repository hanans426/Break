package com.neo.sk.front.style

import scala.language.postfixOps
import scalacss.DevDefaults._
/**
  * User: gaohan
  * Date: 2019/2/9
  * Time: 21:54
  */
object RegisterStyle extends StyleSheet.Inline{
  import dsl._

  val head = style(
    fontSize(35 px),
    margin(20 px),
    textAlign.center
  )
  val container = style(
    fontSize(17 px),
    margin(15 px, 0 px),
    height(30 px),
    textAlign.center
  )
  val input = style(
    width(160 px),
    height(30 px),
    borderRadius(5 px),
    fontSize(17 px)
  )

  val button = style(
    width(100 px),
    height(38 px),
    borderRadius(5 px),
    fontSize(17 px),
    marginLeft(48.%%),
    backgroundColor.rgb(255, 228, 225)
  )

}
