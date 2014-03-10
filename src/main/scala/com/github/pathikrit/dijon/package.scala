package com.github.pathikrit

import scala.collection.mutable
import scala.util.parsing.json.{JSON, JSONObject}
import com.github.pathikrit.dijon.UnionType.{∨, ∅}

package object dijon {

  type JsonTypes = (∅ ∨ String ∨ Int ∨ Double ∨ Boolean ∨ JsonArray ∨ JsonObject ∨ None.type)
  type ValidJsonType[A] = JsonTypes#Member[A]
  type SomeJson = Json[A] forSome {type A}

  type JsonObject = mutable.Map[String, SomeJson]
  def `{}`: SomeJson = mutable.Map.empty[String, SomeJson]

  type JsonArray = mutable.Buffer[SomeJson]
  def `[]`: SomeJson = mutable.Buffer.empty[SomeJson]

  implicit class Json[A: ValidJsonType](val underlying: A) extends Dynamic {

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
      case str: String => "\"" + str + "\""     //TODO: use string interpolation here
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
  implicit val `SomeJson -> String` = toScalaType[String] _
  implicit val `SomeJson -> Int` = toScalaType[Int] _
  implicit val `SomeJson -> Double` = toScalaType[Double] _
  implicit val `SomeJson -> Boolean` = toScalaType[Boolean] _

  def parse(s: String): SomeJson = (JSON.parseFull(s) map assemble).get

  def assemble(s: Any): SomeJson = s match {
    case null => null
    case x: Map[String, Any] => mutable.Map((x mapValues assemble).toSeq: _*)
    case x: Seq[Any] => mutable.Buffer[SomeJson](x map assemble: _*)
    case x: String => x
    case x: Int => x
    case x: Double => x
    case x: Boolean => x
  }

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): SomeJson = parse(sc.s(args: _*))
  }
}
