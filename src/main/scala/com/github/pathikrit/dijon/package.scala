package com.github.pathikrit

import java.nio.charset.StandardCharsets._
import java.util

import com.github.pathikrit.dijon.UnionType.{∅, ∨}
import com.github.plokhotnyuk.jsoniter_scala.core._

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.util.Try

package object dijon {
  type JsonTypes = ∅ ∨ String ∨ Int ∨ Double ∨ Boolean ∨ JsonArray ∨ JsonObject ∨ None.type
  type JsonType[A] = JsonTypes#Member[A]
  type SomeJson = Json[A] forSome { type A }
  type JsonObject = mutable.Map[String, SomeJson]
  type JsonArray = mutable.Buffer[SomeJson]

  def `[]`: SomeJson = new mutable.ArrayBuffer[SomeJson](initArrayCapacity)

  def `{}`: SomeJson = new util.LinkedHashMap[String, SomeJson](initMapCapacity).asScala

  def JsonObject(values: (String, SomeJson)*): SomeJson = {
    val len = values.length
    var i = 0
    val map = new util.LinkedHashMap[String, SomeJson](len)
    while (i < len) {
      val kv = values(i)
      map.put(kv._1, kv._2)
      i += 1
    }
    map.asScala
  }

  def JsonArray(values: SomeJson*): SomeJson = mutable.ArrayBuffer[SomeJson](values: _*)

  implicit class Json[A : JsonType](val underlying: A) extends Dynamic {
    def selectDynamic(key: String): SomeJson = apply(key)

    def updateDynamic(key: String)(value: SomeJson): Unit = update(key, value)

    def applyDynamic[B](key1: String)(indexOrKey2: B): SomeJson = indexOrKey2 match {
      case index: Int => underlying.apply(key1).apply(index)
      case key2: String => underlying.apply(key1).apply(key2)
    }

    def apply(key: String): SomeJson = underlying match {
      case obj: JsonObject => obj.get(key) match {
        case Some(value) => value
        case _ => None
      }
      case _ => None
    }

    def apply(index: Int): SomeJson = underlying match {
      case arr: JsonArray if arr.isDefinedAt(index) => arr(index)
      case _ => None
    }

    def update(key: String, value: SomeJson): Unit = underlying match {
      case obj: JsonObject => obj += ((key, value))
      case _ => ()
    }

    def update(index: Int, value: SomeJson): Unit = underlying match {
      case arr: JsonArray if index >= 0 =>
        while (arr.length <= index) {
          arr += None
        }
        arr(index) = value
      case _ => ()
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
      case arr: JsonArray => arr.foldLeft(new mutable.ArrayBuffer[SomeJson](arr.length))((res, x) => res += x.deepCopy)
      case obj: JsonObject => obj.foldLeft(new util.LinkedHashMap[String, SomeJson](obj.size)) { (res, kv) =>
        res.put(kv._1, kv._2.deepCopy)
        res
      }.asScala
      case _ => this
    }

    override def toString: String = compact(this)

    override def equals(obj: Any): Boolean = underlying == (obj match {
      case that: SomeJson => that.underlying
      case _ => obj
    })

    override def hashCode: Int = underlying.hashCode
  }

  def compact(json: SomeJson): String = new String(writeToArray[SomeJson](json), UTF_8)

  def pretty(json: SomeJson): String = new String(writeToArray[SomeJson](json, prettyConfig), UTF_8)

  def parse(s: String): SomeJson = readFromArray[SomeJson](s.getBytes(UTF_8))

  implicit class JsonStringContext(val sc: StringContext) extends AnyVal {
    def json(args: Any*): SomeJson = parse(sc.s(args: _*))
  }

  implicit val codec: JsonValueCodec[SomeJson] = new JsonValueCodec[SomeJson] {
    override def decodeValue(in: JsonReader, default: SomeJson): SomeJson = decode(in, maxParsingDepth)

    override def encodeValue(x: SomeJson, out: JsonWriter): Unit = encode(x, out, maxSerializationDepth)

    override val nullValue: SomeJson = None

    private[this] def decode(in: JsonReader, depth: Int): SomeJson = {
      val b = in.nextToken()
      if (b == 'n') in.readNullOrError(None, "expected `null` value")
      else if (b == '"') {
        in.rollbackToken()
        in.readString(null)
      } else if (b == 't' || b == 'f') {
        in.rollbackToken()
        in.readBoolean()
      } else if ((b >= '0' && b <= '9') || b == '-') {
        in.rollbackToken()
        val d = in.readDouble()
        val i = d.toInt
        if (i.toDouble == d) i
        else d
      } else if (b == '[') {
        if (depth <= 0) in.decodeError("depth limit exceeded")
        val arr = new mutable.ArrayBuffer[SomeJson](initArrayCapacity)
        if (!in.isNextToken(']')) {
          in.rollbackToken()
          val dp = depth - 1
          do arr += decode(in, dp)
          while (in.isNextToken(','))
          if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
        }
        arr
      } else if (b == '{') {
        if (depth <= 0) in.decodeError("depth limit exceeded")
        val obj = new util.LinkedHashMap[String, SomeJson](initMapCapacity)
        if (!in.isNextToken('}')) {
          in.rollbackToken()
          val dp = depth - 1
          do obj.put(in.readKeyAsString(), decode(in, dp))
          while (in.isNextToken(','))
          if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
        }
        obj.asScala
      } else in.decodeError("expected JSON value")
    }

    private[this] def encode(x: SomeJson, out: JsonWriter, depth: Int): Unit = x.underlying match {
      case None => out.writeNull()
      case str: String => out.writeVal(str)
      case b: Boolean => out.writeVal(b)
      case i: Int => out.writeVal(i)
      case d: Double => out.writeVal(d)
      case arr: JsonArray =>
        if (depth <= 0) out.encodeError("depth limit exceeded")
        out.writeArrayStart()
        val dp = depth - 1
        val l = arr.size
        var i = 0
        while (i < l) {
          encode(arr(i), out, dp)
          i += 1
        }
        out.writeArrayEnd()
      case obj: JsonObject =>
        if (depth <= 0) out.encodeError("depth limit exceeded")
        out.writeObjectStart()
        val dp = depth - 1
        val it = obj.iterator
        while (it.hasNext) {
          val (k, v) = it.next()
          out.writeKey(k)
          encode(v, out, dp)
        }
        out.writeObjectEnd()
    }
  }

  private[this] val prettyConfig = WriterConfig.withIndentionStep(2)
  private[this] val maxParsingDepth = Try(System.getProperty("dijon.maxParsingDepth", "128").toInt).getOrElse(128)
  private[this] val maxSerializationDepth = Try(System.getProperty("dijon.maxSerializationDepth", "128").toInt).getOrElse(128)
  private[this] val initArrayCapacity = Try(System.getProperty("dijon.initArrayCapacity", "8").toInt).getOrElse(8)
  private[this] val initMapCapacity = Try(System.getProperty("dijon.initMapCapacity", "8").toInt).getOrElse(8)
}
