package com.github.pathikrit.dijon

import com.github.pathikrit.dijon.Implicits._

object Test extends App {
  val langs = json"""["scala", ["python2", "python3"]]"""
  langs(1)(3) = "python4"
  println("====> " + langs)
}
