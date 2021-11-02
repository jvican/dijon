![Continuous Integration](https://github.com/jvican/dijon/workflows/Continuous%20Integration/badge.svg?branch=master)

dijon - Dynamic JSON in Scala
=====
* Boiler-free JSON wrangling using Scala [Dynamic Types](https://www.scala-lang.org/api/2.13.2/scala/Dynamic.html)
* Support of [RFC8259](https://tools.ietf.org/html/rfc8259) using a codec based on [jsoniter-scala-core][2] that [passes all JSONTestSuite checks](https://github.com/nst/JSONTestSuite/pull/99)
* Why yet another Scala JSON library? Well, code speaks more than thousand words:

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
assert(cat.name == name)                         // dynamic type
assert(cat.age == age)
val Some(catAge: Int) = cat.age.asInt
assert(catAge == age)
assert(cat.age.asBoolean == None)

val catMap = cat.toMap                           // view as a hashmap
assert(catMap.toMap.keysIterator.toSeq == Seq("name", "age", "hobbies", "is cat"))

assert(cat.hobbies(1) == "purring") // array access
assert(cat.hobbies(100) == None)    // missing element
assert(cat.`is cat` == true)        // keys with spaces/symbols/scala-keywords need to be escaped with ticks
assert(cat.email == None)           // missing key

val vet = `{}`                      // create empty json object
vet.name = "Dr. Kitty Specialist"   // set attributes in json object
vet.phones = `[]`                   // create empty json array
val phone = "(650) 493-4233"
vet.phones(2) = phone               // set the 3rd item in array to this phone
assert(vet.phones == mutable.Seq(None, None, phone))  // first 2 entries None

vet.address = `{}`
vet.address.name = "Animal Hospital"
vet.address.city = "Palo Alto"
vet.address.zip = 94306
assert(vet.address == mutable.Map[String, SomeJson]("name" -> "Animal Hospital", "city" -> "Palo Alto", "zip" -> 94306))

cat.vet = vet                            // set the cat.vet to be the vet json object we created above
assert(cat.vet.phones(2) == phone)
assert(cat.vet.address.zip == 94306)     // json deep access

println(cat) // {"name":"Tigri","age":7,"hobbies":["eating","purring"],"is cat":true,"vet":{"name":"Dr. Kitty Specialist","phones":[null,null,"(650) 493-4233"],"address":{"name":"Animal Hospital","city":"Palo Alto","zip":94306}}}

assert(cat == parse(cat.toString))   // round-trip test

var basicCat = cat -- "vet"                                  // remove 1 key
basicCat = basicCat -- ("hobbies", "is cat", "paws")         // remove multiple keys ("paws" is not in cat)
assert(basicCat == json"""{ "name": "Tigri", "age": 7}""")   // after dropping some keys above
```

* Simple deep-merging:
```scala
val scala = json"""
{
  "name": "scala",
  "version": "2.13.2",
  "features": {
    "functional": true,
    "awesome": true
  }
}
"""

val java = json"""
{
  "name": "java",
  "features": {
    "functional": [0, 0],
    "terrible": true
  },
  "bugs": 213
}
"""

val scalaCopy = scala.deepCopy
val javaCopy = java.deepCopy

assert((scala ++ java) == json"""{"name":"java","version":"2.13.2","features":{"functional":[0,0],"terrible":true,"awesome":true},"bugs":213}""")
assert((java ++ scala) == json"""{"name":"scala","version":"2.13.2","features":{"functional": true,"terrible":true,"awesome":true},"bugs":213}""")

assert(scala == scalaCopy)       // original json objects stay untouched after merging
assert(java == javaCopy)
```

* [Union types](dijon/src/main/scala/dijon/UnionType.scala) for [type-safety](dijon/src/main/scala/dijon/package.scala#L11):
```scala
val json = `{}`
json.aString = "hi"                        // compiles
json.aBoolean = true                       // compiles
json.anInt = 23                            // compiles
//json.somethingElse = Option("hi")       // does not compile
val Some(i: Int) = json.anInt.asInt
assert(i == 23)
assert(json.aBoolean.asInt == None)
```

* `obj()` and `arr()` constructor functions for building up complex JSON values with less overhead:
```scala
val rick = obj(
  "name" -> name,
  "age" -> age,
  "class" -> "human",
  "weight" -> 175.1,
  "is online" -> true,
  "contact" -> obj(
    "emails" -> arr(email1, email2),
    "phone" -> obj(
      "home" -> "817-xxx-xxx",
      "work" -> "650-xxx-xxx"
    )
  ),
  "hobbies" -> arr(
    "eating",
    obj(
      "games" -> obj(
        "chess" -> true,
        "football" -> false
      )
    ),
    arr("coding", arr("python", "scala")),
    None
  ),
  "toMap" -> arr(23, 345, true),
  "apply" -> 42
)
```

See the [spec][1] for more examples.

Also, for the `dijon.codec` an additional functionality is available when using [jsoniter-scala-core][2], like:
* parsing/serialization from/to byte arrays, byte buffers, and input/output streams
* parsing of [streamed JSON values](https://en.wikipedia.org/wiki/JSON_streaming) (concatenated or delimited by 
  whitespace characters) and JSON arrays from input streams using callbacks without the need of holding a whole input in
  the memory
* use a custom configuration for parsing and serializing  
  
See [jsoniter-scala-core spec][3] for more details and code samples.

Usage
===
1. Add the following to your `build.sbt`:
```scala
libraryDependency += "me.vican.jorge" %% "dijon" % "0.6.0" // Use %%% instead of %% for Scala.js
```
2. Turn on support of dynamic types by adding import clause:
```scala
import scala.language.dynamics._
```
or by setting the scala compiler option:
```scala
scalacOptions += "-language:dynamics"
```
3. Add import of the package object of `dijon` for the main functionality:
```scala
import dijon._
```
4. Optionally, add import of package object of `jsoniter-scala-core` for extended json functionality:
```scala
import com.github.plokhotnyuk.jsoniter_scala.core._
```

TODO
====
* BigInt support
* Circular references checker
* YAML interpolator
* Macro for type inference to induce compile-time errors where possible
* JSON string interpolator fills in braces, quotes and commas etc

[1]: dijon/src/test/scala/dijon/DijonSpec.scala
[2]: https://github.com/plokhotnyuk/jsoniter-scala/blob/master/jsoniter-scala-core/src/main/scala/com/github/plokhotnyuk/jsoniter_scala/core/package.scala
[3]: https://github.com/plokhotnyuk/jsoniter-scala/blob/master/jsoniter-scala-core/src/test/scala/com/github/plokhotnyuk/jsoniter_scala/core/PackageSpec.scala
