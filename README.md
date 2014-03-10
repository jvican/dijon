[![Build Status](https://travis-ci.org/pathikrit/dijon.png)](http://travis-ci.org/pathikrit/dijon)

dijon - A Scala Dynamic Json Library
=====
* Uses [Scala Dynamic types](http://www.scala-lang.org/api/2.10.3/index.html#scala.Dynamic) that let's you write boiler-free json
* [No external dependencies](build.sbt)
* [Less than 100 lines of code](src/main/scala/com/github/pathikrit/dijon/Json.scala)
* Why yet another Scala json library? Well, code speaks more than thousand words:

```scala
import com.github.pathikrit.dijon.Json._

val (name, age) = ("Tigri", 7)
val Some(cat) = json"""{"name": "$name", "info": {"age": $age, "hobbies": ["eating", "purring"]}, "is cat": true}"""

assert(cat.name == name)
assert(cat.info.age == age)
assert(cat.info.hobbies(1) == "purring")
assert(cat.`is cat`)
assert(cat.email == None)

val vet = new JsonObject
vet.name = "Dr. Kitty Specialist"
vet.address = new JsonObject
vet.address.name = "Palo Alto Pet Clinic"
vet.address.city = "Palo Alto"
vet.address.zip = 94041

cat.vet = vet

println(cat)

// prints {"vet" : {"address" : {"city" : Palo Alto, "zip" : 94041, "name" : Palo Alto Pet Clinic}, "name" : Dr. Kitty Specialist}, "is cat" : true, "name" : Tigri, "info" : {"hobbies" : ["eating", "purring"], "age" : 7.0}}

```

See the [spec](src/test/scala/com/github/pathikrit/dijon) for more examples.
