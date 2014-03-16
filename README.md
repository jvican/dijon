[![Build Status](https://travis-ci.org/pathikrit/dijon.png?branch=master)](http://travis-ci.org/pathikrit/dijon) [![Coverage Status](https://coveralls.io/repos/pathikrit/dijon/badge.png)](https://coveralls.io/r/pathikrit/dijon)

dijon - Dynamic Json in Scala
=====
* Boiler-free json wrangling using Scala [Dynamic Types](http://www.scala-lang.org/api/2.10.3/index.html#scala.Dynamic)
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
assert(cat.name == name)                         // dynamic type
assert(cat.age == age)
val Some(catAge: Double) = cat.age.as[Double]    // type inference
assert(catAge == age)
assert(cat.age.as[Boolean] == None)

val catMap = cat.toMap                           // view as a hashmap
assert(catMap.keySet == Set("name", "age", "hobbies", "is cat"))

assert(cat.hobbies(1) == "purring") // array access
assert(cat.hobbies(100) == None)    // missing element
assert(cat.`is cat` == true)        // keys with spaces/symbols/scala-keywords need to be escaped with ticks
assert(cat.email == None)           // missing key

val vet = `{}`                      // create empty json object
vet.name = "Dr. Kitty Specialist"   // set attributes in json object
vet.phones = `[]`                   // create empty json array
val phone = "(650) 493-4233"
vet.phones(2) = phone               // set the 3rd item in array to this phone
assert(vet.phones == mutable.Seq(null, null, phone))  // first 2 entries null

vet.address = `{}`
vet.address.name = "Animal Hospital"
vet.address.city = "Palo Alto"
vet.address.zip = 94306
assert(vet.address == mutable.Map("name" -> "Animal Hospital", "city" -> "Palo Alto", "zip" -> 94306))

cat.vet = vet                            // set the cat.vet to be the vet json object we created above
assert(cat.vet.phones(2) == phone)
assert(cat.vet.address.zip == 94306)     // json deep access

println(cat) // {"name" : "Tigri", "hobbies" : ["eating", "purring"], "vet" : {"address" : {"city" : "Palo Alto", "zip" : 94306, "name" : "Animal Hospital"}, "name" : "Dr. Kitty Specialist", "phones" : [null, null, "(650) 493-4233"]}, "is cat" : true, "age" : 7.0}
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
  "version": "2.10.3",
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

assert((scala ++ java) == json"""{"name": "java", "version": "2.10.3", "features": { "functional": [0, 0], "terrible": true, "awesome": true}, "bugs": 213}""")
assert((java ++ scala) == json"""{"name": "scala", "version": "2.10.3", "features": { "functional": true, "terrible": true, "awesome": true}, "bugs": 213}""")
```

* [Union types](src/main/scala/com/github/pathikrit/dijon/UnionType.scala) for [type-safety](src/main/scala/com/github/pathikrit/dijon/package.scala#L11):
```scala
val json = `{}`
json.aString = "hi"                        // compiles
json.aBoolean = true                       // compiles
json.anInt = 23                            // compiles
// test.somethingElse = Option("hi")       // does not compile
val Some(i: Int) = json.anInt.as[Int]
assert(i == 23)
val j: Int = json.aBoolean.as[Int]    // run-time exception
```

See the [spec][1] for more examples.

Usage
===
Add the following to your `build.sbt` to use `dijon`:
```scala
resolvers += "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/"

libraryDependency += "com.github.pathikrit" %% "dijon" % "0.1.1"
```

TODO
====
* BigInt support
* Pretty printer
* Circular references checker
* YAML interpolator
* Macro for type inference to induce compile-time errors where possible
* JSON string interpolator fills in braces, quotes and commas etc
* Scala 2.11 compatibility without warnings

[1]: src/test/scala/com/github/pathikrit/dijon/DijonSpec.scala
