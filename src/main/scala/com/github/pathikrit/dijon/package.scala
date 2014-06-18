package com.github.pathikrit

import org.json4s.native.JsonMethods
import JsonMethods._
import org.json4s._

package object dijon {

  case class Json(var underlying: JValue) extends Dynamic {

    def apply(index: Int): Json =  underlying match {
      case JArray(arr) if arr isDefinedAt index => arr(index)
      case _ => JNothing
    }

    def selectDynamic(key: String): Json = underlying match {
      case JObject(obj) => obj apply key
      case _ => JNothing
    }

    def updateDynamic(key: String)(value: JValue): Unit = underlying match {
      case JObject(obj) => underlying = JObject(obj + (key -> value))
      case _ =>
    }

    def applyDynamic(key: String)(index: Int): Json = underlying match {
      case JObject(obj) if jFieldsToMap(obj) contains key => Json(obj.apply(key)).apply(index)
      case JArray(arr) if key == "apply" && (arr isDefinedAt index) => throw new IllegalStateException("should not be called")
      case _ => JNothing
    }

    def update(index: Int, value: JValue): Unit = underlying match {
      case JArray(arr) if index >= 0 => underlying = JArray(List.tabulate((index+1) max arr.length) {i =>
        if (i == index) value
        else if (i >= arr.length) JNull
        else arr(i)
      })
      case _ =>
    }

    def +++(that: Json) = Json(underlying merge that.underlying)

    def --(keys: String*): Json = underlying match {
      case JObject(obj) => JObject(obj -- keys)
      case _ => this
    }

    def toMap = underlying match {
      case obj: JObject => obj.values
      case _ => Map.empty[String, Any]
    }

    def toSeq = underlying match {
      case arr: JArray => arr.values
      case _ => List.empty[Any]
    }

    def as[A: Manifest] = underlying.extract[A]
    def asOpt[A: Manifest] = underlying.extractOpt[A]

    def toJsonString = compact(render(underlying))
    override def toString = underlying.toSome map render map pretty getOrElse "nothing"

    override def equals(that: Any) = super.equals(that) || underlying == that || underlying.values == that || (that.isInstanceOf[Json] && underlying.values == that.asInstanceOf[Json].underlying.values)
  }

  def `{}`: Json = JObject()
  def `[]`: Json = JArray(List.empty)

  val Implicits = BigDecimalMode
  import Implicits._
  implicit val formats = DefaultFormats

  implicit def jValueToJson(v: JValue): Json = Json(v)
  implicit def jsonToJValue(json: Json): JValue = json.underlying

  implicit def jFieldsToMap(l: List[JField]): Map[String, JValue] = l.toMap withDefaultValue JNothing
  implicit def mapToJFields(m: Map[String, JValue]): List[JField] = m.toList

  implicit class StringImplicits(s: String) {
    def asJson: Json = parse(s)
  }

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): Json = sc.s(args: _*).asJson
  }
}
