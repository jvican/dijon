package com.github.pathikrit.dijon

// TODO: do Union types using Curry-Howard: http://www.chuusai.com/2011/06/09/scala-union-types-curry-howard/
object UnionType {
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
