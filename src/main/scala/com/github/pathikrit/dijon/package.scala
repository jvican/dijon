package com.github.pathikrit

import scala.collection.mutable
import scala.util.parsing.json.{JSON, JSONObject}

import com.github.pathikrit.dijon.UnionType.{∨, ∅}

package object dijon {

  type JsonTypes = ∅ ∨ String ∨ Int ∨ Double ∨ Boolean ∨ JsonArray ∨ JsonObject ∨ None.type
  type JsonType[A] = JsonTypes#Member[A]
  type SomeJson = Json[A] forSome {type A}

  type JsonObject = mutable.Map[String, SomeJson]
  def `{}`: SomeJson = mutable.Map.empty[String, SomeJson]

  type JsonArray = mutable.Buffer[SomeJson]
  def `[]`: SomeJson = mutable.Buffer.empty[SomeJson]

  implicit class Json[A : JsonType](val underlying: A) extends Dynamic {

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
        while(arr.size <= index) { arr += null }
        arr(index) = value
      case _ =>
    }

    def keys: Option[Set[String]] = underlying match {
      case obj: JsonObject => Some(obj.keySet.toSet)
      case _ => None
    }

    def +(that: SomeJson): SomeJson = (this.underlying, that.underlying) match {
      case (a: JsonObject, b: JsonObject) =>
        val res = a.clone()
        b.keys foreach {
          case key if res contains key => res(key) = res(key) + b(key)
          case key => res(key) = b(key)
        }
        res
      case _ => that
    }

    def -(keys: String*): SomeJson = underlying match {
      case obj: JsonObject => obj -- keys
      case _ => this
    }

    override def toString = underlying match {
      case obj: JsonObject => new JSONObject(obj.toMap).toString
      case arr: JsonArray => arr mkString ("[", ", ", "]")
      case str: String => s""""$str""""
      case _ => underlying.toString
    }

    override def equals(obj: Any) = underlying == (obj match {
      case that: SomeJson => that.underlying
      case _ => obj
    })

    override def hashCode = underlying.hashCode
  }

  implicit def toUnderlying[A : JsonType] = (json: SomeJson) => json.underlying.asInstanceOf[A]
  // TODO: better way to write this?
  implicit val `SomeJson -> String` = toUnderlying[String]
  implicit val `SomeJson -> Int` = toUnderlying[Int]
  implicit val `SomeJson -> Double` = toUnderlying[Double]
  implicit val `SomeJson -> Boolean` = toUnderlying[Boolean]

  def parse(s: String): SomeJson = (JSON.parseFull(s) map assemble) getOrElse (throw new IllegalArgumentException("Could not convert to JSON"))

  def assemble(s: Any): SomeJson = s match {
    case null => null
    case x: Map[String, Any] => mutable.Map((x mapValues assemble).toSeq : _*)
    case x: Seq[Any] => mutable.Buffer[SomeJson](x map assemble : _*)
    case x: String => x
    case x: Int => x
    case x: Double => x
    case x: Boolean => x
  }

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): SomeJson = parse(sc.s(args: _*))
  }
}
