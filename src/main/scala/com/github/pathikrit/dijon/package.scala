package com.github.pathikrit

import java.util

import com.github.pathikrit.dijon.UnionType.{∅, ∨}
import com.github.plokhotnyuk.jsoniter_scala.core._

import scala.annotation.switch
import scala.jdk.CollectionConverters._
import scala.collection.mutable

package object dijon {
  type JsonTypes = ∅ ∨ String ∨ Int ∨ Double ∨ Boolean ∨ JsonArray ∨ JsonObject ∨ None.type
  type JsonType[A] = JsonTypes#Member[A]
  type SomeJson = Json[A] forSome {type A}
  type JsonObject = mutable.Map[String, SomeJson]
  type JsonArray = mutable.Buffer[SomeJson]

  def `{}`: SomeJson = new util.LinkedHashMap[String, SomeJson].asScala

  def `[]`: SomeJson = new mutable.ArrayBuffer[SomeJson]

  implicit class Json[A : JsonType](val underlying: A) extends Dynamic {
    def selectDynamic(key: String): SomeJson = underlying match {
      case obj: JsonObject => obj.get(key) match {
        case Some(value) => value
        case _ => None
      }
      case _ => None
    }

    def updateDynamic(key: String)(value: SomeJson): Unit = underlying match {
      case obj: JsonObject => obj += ((key, value))
      case _ => ()
    }

    def applyDynamic(key: String)(index: Int): SomeJson = underlying match {
      case obj: JsonObject => obj.get(key) match {
        case Some(value) => value.underlying match {
          case arr: JsonArray if arr.isDefinedAt(index) => arr(index)
          case _ => None
        }
        case _ => None
      }
      case arr: JsonArray if key == "apply" && arr.isDefinedAt(index) => arr(index)
      case _ => None
    }

    def update(index: Int, value: SomeJson): Unit = underlying match {
      case arr: JsonArray if index >= 0 =>
        while (arr.length <= index) {
          arr += None
        }
        arr(index) = value
      case _ =>
    }

    def ++(that: SomeJson): SomeJson = (this.underlying, that.underlying) match {
      case (a: JsonObject, b: JsonObject) =>
        val res = new util.LinkedHashMap[String, SomeJson](a.size + b.size)
        a.foreach { case (k, v) =>
          res.put(k, if (b.contains(k)) v ++ b(k) else v.deepCopy)
        }
        b.foreach { case (k, v) =>
          if (!res.containsKey(k)) res.put(k, v.deepCopy)
        }
        res.asScala
      case _ => that.deepCopy
    }

    def --(keys: String*): SomeJson = underlying match {
      case obj: JsonObject =>
        val res = obj.clone()
        keys.foreach(res -= _)
        res.deepCopy
      case _ => deepCopy
    }

    def remove(keys: String*): Unit = underlying match {
      case obj: JsonObject => keys.foreach(obj -= _)
      case _ => ()
    }

    def asString: Option[String] = underlying match {
      case x: String => new Some(x)
      case _ => None
    }

    def asDouble: Option[Double] = underlying match {
      case x: Double => new Some(x)
      case _ => None
    }

    def asInt: Option[Int] = underlying match {
      case x: Int => new Some(x)
      case _ => None
    }

    def asBoolean: Option[Boolean] = underlying match {
      case x: Boolean => new Some(x)
      case _ => None
    }

    def toSeq: collection.Seq[SomeJson] = underlying match {
      case arr: JsonArray => arr
      case _ => Nil
    }

    def toMap: collection.Map[String, SomeJson] = underlying match {
      case obj: JsonObject => obj
      case _ => Map.empty
    }

    def deepCopy: SomeJson = underlying match {
      case arr: JsonArray =>
        val res = new mutable.ArrayBuffer[SomeJson](arr.length)
        arr.foreach(x => res += x.deepCopy)
        res
      case obj: JsonObject =>
        val res = new util.LinkedHashMap[String, SomeJson](obj.size)
        obj.foreach { case (k, v) =>
          res.put(k, v.deepCopy)
        }
        res.asScala
      case _ => this
    }

    override def toString: String = compact(this)

    override def equals(obj: Any): Boolean = underlying == (obj match {
      case that: SomeJson => that.underlying
      case _ => obj
    })

    override def hashCode: Int = underlying.hashCode
  }

  def compact(json: SomeJson): String = writeToString[SomeJson](json)

  def pretty(json: SomeJson): String = writeToString[SomeJson](json, WriterConfig.withIndentionStep(2))

  def parse(s: String): SomeJson = readFromString[SomeJson](s)

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): SomeJson = parse(sc.s(args: _*))
  }

  implicit val codec: JsonValueCodec[SomeJson] = new JsonValueCodec[SomeJson] {
    override def decodeValue(in: JsonReader, default: SomeJson): SomeJson = (in.nextToken(): @switch) match {
      case 'n' => in.readNullOrError(default, "expected `null` value")
      case '"' =>
        in.rollbackToken()
        in.readString(null)
      case 'f' | 't' =>
        in.rollbackToken()
        in.readBoolean()
      case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '-' =>
        in.rollbackToken()
        val d = in.readDouble()
        val i = d.toInt
        if (i.toDouble == d) i
        else d
      case '[' =>
        val arr = new mutable.ArrayBuffer[SomeJson]
        if (!in.isNextToken(']')) {
          in.rollbackToken()
          do arr += decodeValue(in, default)
          while (in.isNextToken(','))
          if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
        }
        arr
      case '{' =>
        val obj = new util.LinkedHashMap[String, SomeJson]
        if (!in.isNextToken('}')) {
          in.rollbackToken()
          do obj.put(in.readKeyAsString(), decodeValue(in, default))
          while (in.isNextToken(','))
          if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
        }
        obj.asScala
      case _ => in.decodeError("expected JSON value")
    }

    override def encodeValue(x: SomeJson, out: JsonWriter): Unit = x.underlying match {
      case None => out.writeNull()
      case str: String => out.writeVal(str)
      case b: Boolean => out.writeVal(b)
      case i: Int => out.writeVal(i)
      case d: Double => out.writeVal(d)
      case arr: JsonArray =>
        out.writeArrayStart()
        arr.foreach(v => encodeValue(v, out))
        out.writeArrayEnd()
      case obj: JsonObject =>
        out.writeObjectStart()
        obj.foreach { case (k, v) =>
          out.writeKey(k)
          encodeValue(v, out)
        }
        out.writeObjectEnd()
    }

    override val nullValue: SomeJson = None
  }
}
