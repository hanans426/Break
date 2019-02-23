package com.neo.sk.front.style

import scala.language.postfixOps
import scalacss.DevDefaults._
import scalacss.internal.CssEntry.FontFace

/**
  * User: gaohan
  * Date: 2019/2/9
  * Time: 21:29
  */
object LoginStyle extends StyleSheet.Inline{
  import dsl._

  val head = style(
    fontSize(35 px),
    margin(20 px),
    textAlign.center,
    fontStyle.italic
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
    fontSize(17 px),

  )

  val firstButton1 = style(
    width(200 px),
    height(50 px),
    borderRadius(5 px),
    fontSize(17 px),
    marginLeft(43.%%),
    backgroundColor.rgb(255,240,245)
  )

  val firstButton2 = style(
    width(200 px),
    height(50 px),
    borderRadius(5 px),
    fontSize(17 px),
    marginLeft(43.%%),
    backgroundColor.rgb(230, 230,250)
  )

  val firstButton3 = style(
    width(200 px),
    height(50 px),
    borderRadius(5 px),
    fontSize(17 px),
    marginLeft(43.%%),
    backgroundColor.rgb(255,192,203)
  )

  val button = style(
    width(100 px),
    height(38 px),
    borderRadius(5 px),
    fontSize(17 px),
    marginLeft(48.%%),
    backgroundColor.rgb(255,218,185)
  )
}
