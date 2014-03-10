[![Build Status](https://travis-ci.org/pathikrit/dijon.png)](http://travis-ci.org/pathikrit/dijon)

dijon - Dynamic Json in Scala
=====
* Boiler-free json handling using Scala [Dynamic Types](http://www.scala-lang.org/api/2.10.3/index.html#scala.Dynamic)
* Type-safe through [union types](src/main/scala/com/github/pathikrit/dijon/DisjointType.scala)
```scala
val test = JsonObject
test.aString = "hi"     // compiles
test.aBoolean = true    // compiles
test.anInt = 23         // compiles
test.`null` = null      // compiles
// test.somethingElse = Set.empty[String]  // does not compile
```
* No external [dependencies](build.sbt)
* Less than [100 lines](src/main/scala/com/github/pathikrit/dijon/package.scala) of code
* Well [tested][1]
* Why yet another Scala json library? Well, code speaks more than thousand words:

```scala
val (name, age) = ("Tigri", 7)
val cat = json"""
{
  "name": "$name",
  "age": $age,
  "hobbies": ["eating", "purring"],
  "is cat": true
}
"""
assert(cat.name == name)

assert(cat.age == age)
//assert(cat.age != age + 1)
//cat.age = cat.age + 1
//assert(cat.age == age + 1)

assert(cat.hobbies(1) == "purring")
assert(cat.`is cat` == true)
assert(cat.email == None)

val vet = JsonObject
vet.name = "Dr. Kitty Specialist"
vet.address = JsonObject
vet.address.name = "Silicon Valley Animal Hospital"
vet.address.city = "Palo Alto"
vet.address.zip = 94306

cat.vet = vet
assert(cat.vet.address.zip == 94306)

println(cat)
//{"name" : "Tigri", "hobbies" : ["eating", "purring"], "vet" : {"address" : {"city" : "Palo Alto", "zip" : 94306, "name" : "Silicon Valley Animal Hospital"}, "name" : "Dr. Kitty Specialist"}, "is cat" : true, "age" : 7.0}
```

See the [spec][1] for more examples.


[1]: src/test/scala/com/github/pathikrit/dijon/DijonSpec.scala