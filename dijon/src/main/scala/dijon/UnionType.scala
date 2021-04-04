package dijon

/**
 * Union types using Curry-Howard isomorphism: http://stackoverflow.com/questions/3508077/
 */
object UnionType {
  sealed trait ¬[-A]

  sealed trait TSet {
    type Compound[A]
    type Map[F[_]] <: TSet
  }

  sealed trait ∅ extends TSet {
    type Compound[A] = A
    type Map[F[_]] = ∅
  }

  sealed trait ∨[T <: TSet, H] extends TSet {
    type Member[X] = T#Map[¬]#Compound[¬[H]] <:< ¬[X]

    type Compound[A] = T#Compound[H with A]

    type Map[F[_]] = T#Map[F] ∨ F[H]
  }
}
