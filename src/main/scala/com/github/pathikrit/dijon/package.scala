package com.github.pathikrit

import java.util

import com.github.pathikrit.dijon.UnionType.{∅, ∨}
import com.github.plokhotnyuk.jsoniter_scala.core._
import scala.annotation.switch
import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.reflect.runtime.universe.{TypeTag, typeOf}

package object dijon {
  type JsonTypes = ∅ ∨ String ∨ Int ∨ Double ∨ Boolean ∨ JsonArray ∨ JsonObject ∨ None.type
  type JsonType[A] = JsonTypes#Member[A]
  type SomeJson = Json[A] forSome {type A}
  type JsonObject = mutable.Map[String, SomeJson]
  type JsonArray = mutable.Buffer[SomeJson]

  def `{}`: SomeJson = new util.LinkedHashMap[String, SomeJson].asScala

  def `[]`: SomeJson = new mutable.ArrayBuffer[SomeJson]

  implicit class Json[A : JsonType: TypeTag](val underlying: A) extends Dynamic {
    def selectDynamic(key: String): SomeJson = underlying match {
      case obj: JsonObject if obj contains key => obj(key)
      case _ => None
    }

    def updateDynamic(key: String)(value: SomeJson): Unit = underlying match {
      case obj: JsonObject => obj(key) = value
      case _ => ()
    }

    def applyDynamic(key: String)(index: Int): SomeJson = underlying match {
      case obj: JsonObject if obj contains key => obj(key)(index)
      case arr: JsonArray if key == "apply" && (arr isDefinedAt index) => arr(index)
      case _ => None
    }

    def update(index: Int, value: SomeJson): Unit = underlying match {
      case arr: JsonArray if index >= 0 =>
        while (arr.size <= index) {
          arr += None
        }
        arr(index) = value
      case _ =>
    }

    def ++(that: SomeJson): SomeJson = (this.underlying, that.underlying) match {
      case (a: JsonObject, b: JsonObject) =>
        val res = a.clone()
        b.keys foreach {
          case key if res contains key => res(key) = res(key) ++ b(key)
          case key => res(key) = b(key)
        }
        res
      case _ => that
    }

    def --(keys: String*): SomeJson = underlying match {
      case obj: JsonObject => obj -- keys
      case _ => this
    }

    def remove(keys: String*): Unit = underlying match {
      case obj: JsonObject => obj --= keys
      case _ => ()
    }

    def as[T : JsonType : TypeTag]: Option[T] = underlying match {
      case x: T if typeOf[A] <:< typeOf[T] => Some(x)
      case _ => None
    }

    def toMap: collection.Map[String, SomeJson] = as[JsonObject] match {
      case Some(x) => x
      case None => Map.empty
    }

    def toSeq: collection.Seq[SomeJson] = as[JsonArray] match {
      case Some(x) => x
      case None => Nil
    }

    override def toString: String = compact(this)

    override def equals(obj: Any): Boolean = underlying == (obj match {
      case that: SomeJson => that.underlying
      case _ => obj
    })

    override def hashCode: Int = underlying.hashCode
  }

  def compact(json: SomeJson): String = writeToString[SomeJson](json)

  def pretty(json: SomeJson): String = writeToString[SomeJson](json, WriterConfig(indentionStep = 2))

  def parse(s: String): SomeJson = readFromString[SomeJson](s)

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): SomeJson = parse(sc.s(args: _*))
  }

  implicit val codec: JsonValueCodec[SomeJson] = new JsonValueCodec[SomeJson] {
    override def decodeValue(in: JsonReader, default: SomeJson): SomeJson = (in.nextToken(): @switch) match {
      case 'n' =>
        in.readNullOrError(default, "expected `null` value")
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
      case _ =>
        in.decodeError("expected JSON value")
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
