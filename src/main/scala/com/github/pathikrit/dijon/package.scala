package com.github.pathikrit

import scala.collection.mutable
import scala.util.parsing.json.{JSON, JSONObject}

package object dijon {

  type JsonTypes = (String v Int v Double v Boolean v Null v JsonArray v JsonObject v None.type)
  type ValidJsonType[A] = A => JsonTypes
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

  // TODO: do Union types using Curry-Howard: http://www.chuusai.com/2011/06/09/scala-union-types-curry-howard/
  type v[A,B] = Either[Option[A], Option[B]]

  private def L[A,B](a: A): v[A,B] = Left(Some(a))
  private def R[A,B](b: B): v[A,B] = Right(Some(b))

  // TODO: we only need 8-level disjoint type, for more use scala macro to generate this for up to 22 types?
  implicit def a2[A,B](a: A): v[A,B] = L(a)
  implicit def b2[A,B](b: B): v[A,B] = R(b)

  implicit def a3[A,B,C](a: A): v[v[A,B],C] = L(a2(a))
  implicit def b3[A,B,C](b: B): v[v[A,B],C] = L(b2(b))

  implicit def a4[A,B,C,D](a: A): v[v[v[A,B],C],D] = L(a3(a))
  implicit def b4[A,B,C,D](b: B): v[v[v[A,B],C],D] = L(b3(b))

  implicit def a5[A,B,C,D,E](a: A): v[v[v[v[A,B],C],D],E] = L(a4(a))
  implicit def b5[A,B,C,D,E](b: B): v[v[v[v[A,B],C],D],E] = L(b4(b))

  implicit def a6[A,B,C,D,E,F](a: A): v[v[v[v[v[A,B],C],D],E],F] = L(a5(a))
  implicit def b6[A,B,C,D,E,F](b: B): v[v[v[v[v[A,B],C],D],E],F] = L(b5(b))

  implicit def a7[A,B,C,D,E,F,G](a: A): v[v[v[v[v[v[A,B],C],D],E],F],G] = L(a6(a))
  implicit def b7[A,B,C,D,E,F,G](b: B): v[v[v[v[v[v[A,B],C],D],E],F],G] = L(b6(b))

  implicit def a8[A,B,C,D,E,F,G,H](a: A): v[v[v[v[v[v[v[A,B],C],D],E],F],G],H] = L(a7(a))
  implicit def b8[A,B,C,D,E,F,G,H](b: B): v[v[v[v[v[v[v[A,B],C],D],E],F],G],H] = L(b7(b))

  implicit def a9[A,B,C,D,E,F,G,H,I](a: A): v[v[v[v[v[v[v[v[A,B],C],D],E],F],G],H],I] = L(a8(a))
  implicit def b9[A,B,C,D,E,F,G,H,I](b: B): v[v[v[v[v[v[v[v[A,B],C],D],E],F],G],H],I] = L(b8(b))
}
