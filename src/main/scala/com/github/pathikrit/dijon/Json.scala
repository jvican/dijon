package com.github.pathikrit.dijon

import scala.collection.mutable
import scala.util.parsing.json.{JSON, JSONObject}
import DisjointType._

object Json extends App {

  type JsonTypes = (String v Int v Double v Boolean v Null v JsonArray v JsonObject v None.type)
  type ValidJsonType[A] = A => JsonTypes
  type SomeJson = JsonElement[A] forSome {type A}

  type JsonObject = mutable.Map[String, SomeJson]
  def JsonObject: SomeJson = mutable.Map.empty[String, SomeJson]

  type JsonArray = mutable.Buffer[SomeJson]
  def JsonArray: JsonElement[JsonArray] = mutable.Buffer.empty[SomeJson]

  implicit class JsonElement[A: ValidJsonType](val underlying: A) extends Dynamic {

    def selectDynamic(key: String): SomeJson = underlying match {
      case obj: JsonObject if obj contains key => obj(key)
      case _ => None
    }

    def updateDynamic(key: String)(value: SomeJson): Unit = underlying match {
      case obj: JsonObject => obj(key) = value
      case _ =>
    }

    def applyDynamic(key: String)(index: Int): SomeJson = underlying match {
      case obj: JsonObject if obj contains key => obj(key)(index)
      case arr: JsonArray if key == "apply" && (arr isDefinedAt index) => arr(index)
      case _ => None
    }

    def update(index: Int, value: SomeJson): Unit = underlying match {
      case arr: JsonArray if index >= 0 =>
        while(arr.size <= index) {
          arr += null
        }
        arr(index) = value
      case _ =>
    }

    override def toString = underlying match {
      case obj: JsonObject => new JSONObject(obj.toMap).toString
      case arr: JsonArray => arr mkString ("[", ", ", "]")
      case str: String => "\"" + str + "\""     //todo: use string interpolation here
      case _ => underlying.toString
    }

    override def equals(that: Any) = that match {
      case other: SomeJson => underlying == other.underlying
      case _ => underlying == that
    }

    override def hashCode = underlying.hashCode
  }

  implicit def toScalaType[A: ValidJsonType](json: SomeJson): A = json.underlying.asInstanceOf[A]
  // TODO: better way to write this?
  //implicit def toMap(json: SomeJson): JsonObject = toScalaType(json)
  //implicit def toArray(json: SomeJson): JsonArray = toScalaType(json)
  implicit def toString(json: SomeJson): String = json.underlying.asInstanceOf[String]
  implicit def toInt(json: SomeJson): Int = json.underlying.asInstanceOf[Int]
  implicit def toDouble(json: SomeJson): Double = json.underlying.asInstanceOf[Double]
  implicit def toBoolean(json: SomeJson): Boolean = json.underlying.asInstanceOf[Boolean]

  def parse(s: String): SomeJson = (JSON.parseFull(s) map assemble).get

  def assemble(s: Any): SomeJson = s match {
    case null => null
    case x: Map[String, Any] => mutable.Map((x mapValues assemble).toSeq: _*)
    case x: Seq[Any] => val buffer: mutable.Buffer[SomeJson] = mutable.Buffer((x map assemble): _*); buffer //TODO: wtf?
    case x: String => x
    case x: Int => x
    case x: Double => x
    case x: Boolean => x
  }

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): SomeJson = parse(sc.s(args: _*))
  }
}
