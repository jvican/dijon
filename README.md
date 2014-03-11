[![Build Status](https://travis-ci.org/pathikrit/dijon.png)](http://travis-ci.org/pathikrit/dijon)

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
assert(cat.name == name)            // dynamic type

val catAge: Double = cat.age        // type inference
cat.age = catAge + 1
assert(cat.age == age + 1)

assert(cat.hobbies(1) == "purring")
assert(cat.hobbies(100) == None)    // missing element
assert(cat.`is cat` == true)        // keys with spaces/symbols/scala-keywords need to be escaped with ticks
assert(cat.email == None)           // missing key

val vet = `{}`                      // create empty json object
vet.name = "Dr. Kitty Specialist"
vet.phones = `[]`                   // create empty json array
val phone = "(650) 493-4233"
vet.phones(2) = phone               // set the 3rd item in array to this phone
assert(vet.phones == mutable.Seq(null, null, phone))  // first 2 entries null

vet.address = `{}`
vet.address.name = "Animal Hospital"
vet.address.city = "Palo Alto"
vet.address.zip = 94306
assert(vet.address == mutable.Map("name" -> "Animal Hospital", "city" -> "Palo Alto", "zip" -> 94306))

cat.vet = vet                        // json setter
assert(cat.keys == Some(Set("name", "age", "hobbies", "is cat", "vet")))    // get all keys
assert(cat.vet.phones(2) == phone)
assert(cat.vet.address.zip == 94306)

println(cat) // {"name" : "Tigri", "hobbies" : ["eating", "purring"], "vet" : {"address" : {"city" : "Palo Alto", "zip" : 94306, "name" : "Animal Hospital"}, "name" : "Dr. Kitty Specialist", "phones" : [null, null, "(650) 493-4233"]}, "is cat" : true, "age" : 8.0}
assert(cat == parse(cat.toString))   // round-trip test

cat -= "vet"                            // remove 1 key
cat -= ("hobbies", "is cat", "paws")    // remove multiple keys - note paws is not actually in cat
assert(cat == parse("""{ "name": "Tigri", "age": 8}"""))   // after dropping some keys above
```

* [Union types](src/main/scala/com/github/pathikrit/dijon/UnionType.scala) for [type-safety](src/main/scala/com/github/pathikrit/dijon/package.scala#L10):
```scala
val json = `{}`
json.aString = "hi"                        // compiles
json.aBoolean = true                       // compiles
json.anInt = 23                            // compiles
// test.somethingElse = Some("hi")         // does not compile
val i: Int = json.anInt
assert(i == 23)
val j: Int = json.aBoolean                 // run-time exception
```

See the [spec][1] for more examples.


TODO
====
* Circular references checker
* Pretty printer
* Better parse errors + more test cases for invalid JSONs
* Pluggable custom types e.g. BigDecimal and Date


[1]: src/test/scala/com/github/pathikrit/dijon/DijonSpec.scala
