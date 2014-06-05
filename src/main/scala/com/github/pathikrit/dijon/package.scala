package com.github.pathikrit

import org.json4s.native.JsonMethods
import JsonMethods._
import org.json4s._

package object dijon {

  case class Json(var underlying: JValue) extends Dynamic {

    def selectDynamic(key: String): Json = underlying match {
      case JObject(obj) => obj apply key
      case _ => JNothing
    }

    def updateDynamic(key: String)(value: JValue): Unit = underlying match {
      case JObject(obj) => underlying = JObject(obj + (key -> value))
      case _ =>
    }

    def applyDynamic(key: String)(index: Int): Json = underlying match {
      case JObject(obj) if obj contains key => obj.apply(key)(index)
      case JArray(arr) if key == "apply" && (arr isDefinedAt index) => arr(index)
      case _ => JNothing
    }

    def update(index: Int, value: JValue): Unit = underlying match {
      case JArray(arr) if index >= 0 => underlying = JArray(List.tabulate((index+1) max arr.length) {i =>
        if (i >= arr.length) JNull
        else if (i == index) value
        else arr(i)
      })
      case _ =>
    }

    def ++(that: Json): Json = Json(this.underlying merge that.underlying)

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
    override def toString = pretty(render(underlying))
  }

  def `{}`: Json = JObject()
  def `[]`: Json = JArray(List.empty)

  val Implicits = BigDecimalMode
  implicit val formats = DefaultFormats

  implicit def jsonToJValue(json: Json): JValue = json.underlying
  implicit def jValueToJson(v: JValue): Json = Json(v)

  implicit def jFieldsToMap(l: List[JField]): Map[String, JValue] = l.toMap withDefaultValue JNothing
  implicit def mapToJFields(m: Map[String, JValue]): List[JField] = m.toList

  implicit def parse(s: String): Json = JsonMethods.parse(s)

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): Json = parse(sc.s(args : _*))
  }
}