package com.github.pathikrit

import org.json4s.native.JsonMethods._
import org.json4s._
import scala.collection.mutable

package object dijon {

  abstract class DynamicJson(val j: JValue) extends Dynamic {
    type U
    val underlying: U
    def apply(index: Int): DynamicJson = JNothing
    def update(index: Int, value: JValue): Unit = {}
    def selectDynamic(key: String): DynamicJson = JNothing
    def updateDynamic(key: String)(value: JValue): Unit = {}
    def applyDynamic(key: String)(index: Int): DynamicJson = JNothing
    def as[A: Manifest]: A = j.extract[A]
    def asOpt[A: Manifest]: Option[A] = j.extractOpt[A]
    def +++(that: DynamicJson): DynamicJson = j merge that.j
    def ---(keys: String*): DynamicJson = this
    def toMap: Map[String, Any] = Map.empty[String, Any]
    def toSeq: Seq[Any] = Seq.empty[Any]
    override def toString: String = compact(render(j))
    //override def equals(that: Any) = super.equals(that) || underlying == that || toJValue == that || (that.isInstanceOf[Json] && that.asInstanceOf[Json].underlying == underlying)
  }

  private[this] class DynamicJsonPrimitive(override val j: JValue) extends DynamicJson(j) {
    override type U = j.Values
    override val underlying = j.values
  }

  private[this] class DynamicJsonObject(override val j: JObject) extends DynamicJson(j) {
    override type U = mutable.Map[String, DynamicJson]
    override val underlying: U = mutable.Map(j.obj map { case (k, v) => k -> jValueToJson(v)}: _*)
    override def selectDynamic(key: String) = {
      println("obj.selectDynamic", key)
      underlying(key)
    }
    override def updateDynamic(key: String)(value: JValue) = underlying(key) = value
    override def applyDynamic(key: String)(index: Int) = underlying(key)(index)
  }

  private[this] class DynamicJsonArray(override val j: JArray) extends DynamicJson(j) {
    override type U = mutable.Buffer[DynamicJson]
    override val underlying: U = mutable.Buffer[DynamicJson](j.arr map jValueToJson: _*)
    override def apply(index: Int) = {
      println("arr.apply", index, underlying)
      if (underlying isDefinedAt index) underlying(index) else super.apply(index)
    }
    override def update(index: Int, value: JValue): Unit = {
      println("arr.updateDynamic", index, value, underlying)
      if (index >= 0) {
        while(underlying.size <= index) { underlying += JNothing }
        underlying(index) = value
      }
    }
    override def applyDynamic(key: String)(index: Int): DynamicJson = throw new UnsupportedOperationException(s"key: $key, index = $index")
  }

  def `{}`: DynamicJson = JObject()
  def `[]`: DynamicJson = JArray(List.empty)

  val Implicits = BigDecimalMode
  import Implicits._
  implicit val formats = DefaultFormats

  implicit def jValueToJson(jValue: JValue): DynamicJson = jValue match {
    case obj: JObject => new DynamicJsonObject(obj)
    case arr: JArray => new DynamicJsonArray(arr)
    case _ => new DynamicJsonPrimitive(jValue)
  }
  implicit def jsonToJValue(json: DynamicJson): JValue = json.j

  implicit class StringImplicits(s: String) {
    def asJson: DynamicJson = parse(s)
  }

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): DynamicJson = sc.s(args: _*).asJson
  }
}
