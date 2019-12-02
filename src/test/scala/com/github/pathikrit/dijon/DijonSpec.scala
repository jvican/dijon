package com.github.pathikrit.dijon

import com.github.plokhotnyuk.jsoniter_scala.core.JsonReaderException
import scala.collection.mutable
import org.scalatest.funsuite.AnyFunSuite

class DijonSpec extends AnyFunSuite {
  val (email1, email2) = ("pathikritbhowmick@msn.com", "pathikrit.bhowmick@gmail.com")
  val (name, age) = ("Rick", 27)

  val rick = json"""
    {
      "name": "$name",
      "age": $age,
      "class": "human",
      "weight": 175.1,
      "is online": true,
      "contact": {
        "emails": ["$email1", "$email2"],
        "phone": {
          "home": "817-xxx-xxx",
          "work": "650-xxx-xxx"
         }
       },
       "hobbies": [
         "eating",
         {
           "games": {"chess": true, "football": false}
         },
         ["coding", ["python", "scala"]],
         null
        ],
        "toMap": [23, 345, true]
     }
   """

  test("parse and serialize JSON objects") {
    assert(rick.name == name)

    assert(rick.age == age)

    assert(rick.`class` == "human")

    assert(rick.`is online` == true)

    assert(rick.weight == 175.1)
    assert(rick.weight.keys == None)

    assert(rick.contact.emails(0) == email1)
    assert(rick.contact.emails(1) == email2)

    assert(rick.contact.emails.number == None)
    assert(rick.contact.emails(-1) == None)
    assert(rick.contact.emails(2) == None)
    assert(rick.wife == None)
    assert(rick.wife.name == None)
    assert(rick.wife.keys == None)

    assert(rick.hobbies(0) == "eating")
    assert(rick.hobbies(0).keys == None)

    assert(rick.hobbies(1).games.chess == true)
    assert(rick.hobbies(1).games.football == false)

    assert(rick.hobbies(1).games.football.asBoolean == Some(false))
    assert(rick.hobbies(1).games.football.asInt == None)
    assert(rick.hobbies(1).games.foosball.asBoolean == None)

    assert(rick.hobbies(2)(0) == "coding")

    assert(rick.hobbies(2)(1)(1) == "scala")
    assert(rick.hobbies(2)(100) == None)

    assert(rick.hobbies(3) == None)
    assert(rick.hobbies(4) == None)
    assert(rick.undefined(0) == None)

    assert(rick.toMap.keysIterator.toSeq == List("name", "age", "class", "weight", "is online", "contact", "hobbies", "toMap"))
    assert(rick.selectDynamic("toMap")(1) == 345)
    assert(rick == parse(rick.toString)) // round-trip test
    assert(rick.toSeq.isEmpty == true)

    assert(pretty(rick) ==
      """{
        |  "name": "Rick",
        |  "age": 27,
        |  "class": "human",
        |  "weight": 175.1,
        |  "is online": true,
        |  "contact": {
        |    "emails": [
        |      "pathikritbhowmick@msn.com",
        |      "pathikrit.bhowmick@gmail.com"
        |    ],
        |    "phone": {
        |      "home": "817-xxx-xxx",
        |      "work": "650-xxx-xxx"
        |    }
        |  },
        |  "hobbies": [
        |    "eating",
        |    {
        |      "games": {
        |        "chess": true,
        |        "football": false
        |      }
        |    },
        |    [
        |      "coding",
        |      [
        |        "python",
        |        "scala"
        |      ]
        |    ],
        |    null
        |  ],
        |  "toMap": [
        |    23,
        |    345,
        |    true
        |  ]
        |}""".stripMargin)
  }

  test("parse and serialize JSON arrays") {
    val empty = parse("[]")
    assert(empty(0) == None)
    assert(empty == `[]`)
    assert(empty == parse(empty.toString))
    assert(empty.toSeq == Seq.empty)
    assert(empty.toMap == Map.empty)

    val arr = json"""[1, true, null, "hi", {"key": "value"}]"""
    assert(arr == parse(arr.toString))

    val Some(i: Int) = arr(0).asInt
    assert(i == 1)

    val Some(b: Boolean) = arr(1).asBoolean
    assert(b == true)

    val n = arr(2)
    assert(n == None)

    val Some(s: String) = arr(3).asString
    assert(s == "hi")

    val m = arr(4).toMap
    assert(m("key") == "value")

    val u = arr(5)
    assert(u == None)

    assert(arr.toSeq.size == 5)
    assert(arr.toMap == Map.empty)
  }

  test("handle JSON updates") {
    val (name, age, temperature) = ("Tigri", 7, 38.5)
    val cat = json"""
      {
        "name": "$name",
        "age": $age,
        "temperature": $temperature,
        "hobbies": ["eating", "purring"],
        "is cat": true
      }
    """
    assert(cat.name == name)                         // dynamic type
    val Some(catName: String) = cat.name.asString
    assert(catName == name)
    assert(cat.name.asBoolean.isEmpty)
    assert(cat.name.asDouble.isEmpty)

    assert(cat.age == age)
    val Some(catAge: Int) = cat.age.asInt
    assert(catAge == age)
    assert(cat.age.asBoolean.isEmpty)
    assert(cat.age.asString.isEmpty)

    assert(cat.temperature == temperature)
    val Some(catTemp: Double) = cat.temperature.asDouble
    assert(catTemp == temperature)
    assert(cat.age.asBoolean.isEmpty)

    val catMap = cat.toMap                           // view as a hashmap
    assert(catMap.toMap.keysIterator.toSeq == Seq("name", "hobbies", "is cat", "temperature", "age"))

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

    assert(cat == parse(cat.toString))   // round-trip test

    cat.name.remove("something")         // do nothing
    cat.age.remove("something")          // do nothing
    cat.`is cat`.remove("something")     // do nothing
    vet.phones.remove("something")       // do nothing

    val catCopy = cat.deepCopy

    var basicCat = cat -- "vet"                                  // remove 1 key
    basicCat = basicCat -- ("hobbies", "is cat", "paws")         // remove multiple keys ("paws" is not in cat)
    assert(basicCat == json"""{"name":"Tigri","temperature":38.5,"age": 7}""")   // after dropping some keys above
    basicCat.remove("age")                                       // remove 1 key by mutating object
    assert(basicCat == json"""{"name": "Tigri","temperature":38.5}""")

    assert((cat.vet.address -- "city") == json"""{"name":"Animal Hospital","zip": 94306}""")

    assert(cat == catCopy)               // original json objects stay untouched after removing keys by `--`

    val jsonToMutate = json"[1,2,3]"
    val removeResult = jsonToMutate -- "city"
    assert(removeResult == jsonToMutate)
    jsonToMutate(7) = 7
    assert(removeResult != jsonToMutate)
  }

  test("handle nulls") {
    val t = json"""{"a":null,"b":{"c":null}}"""
    assert(t.a == None)
    assert(t.b.c == None)

    val v = parse("""{"a": null}""")
    assert(v.a == None)

    assert(t == parse(t.toString)) //round-trip test

    t.b.c = v
    assert(t.b.c.a == None)
  }

  test("handle deep merges") {
    val scala = json"""
      {
        "name": "scala",
        "version": "2.13.0",
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

    assert((scala ++ java) == json"""{"name":"java","version":"2.13.0","features":{"functional":[0,0],"terrible":true,"awesome":true},"bugs":213}""")
    assert((java ++ scala) == json"""{"name":"scala","version":"2.13.0","features":{"functional": true,"terrible":true,"awesome":true},"bugs":213}""")

    assert((scala ++ java).bugs == (java ++ scala).bugs)

    assert(scala == scalaCopy)       // original json objects stay untouched after merging
    assert(java == javaCopy)

    val ab = json"""{"a":{"b":[0,1]}}"""
    val ac = json"""{"a":{"c":[1,2]}}"""
    val `ab++ac` = ab ++ ac          // merge result should be not affected by subsequent mutation of arguments
    ab.a.b(0) = json"""5"""
    ac.a.c(0) = json"""3"""
    assert(ab == json"""{"a":{"b":[5,1]}}""")
    assert(ac == json"""{"a":{"c":[3,2]}}""")
    assert(`ab++ac` == json"""{"a":{"b":[0,1],"c":[1,2]}}""")
  }

  test("be type-safeish") {
    var j = json"""{"name" : "chen"}"""
    assert(j.name == "chen")
    assert(j.name.asString == Some("chen"))
    assert(j.name.asInt == None)

    j = `{}`
    j.aString = "hi"                        // compiles
    j.aBoolean = true                       // compiles
    j.anInt = 23                            // compiles
    //j.somethingElse = Option("hi")        // does not compile
    val Some(i: Int) = j.anInt.asInt
    assert(i == 23)
    assert(j.aBoolean.asInt == None)
  }

  test("grow arrays") {
    val langs = json"""["scala", ["python2", "python3"]]"""
    langs(-2) = "java"
    langs(5) = "F#"
    assert(langs(3) == None)
    assert(langs(5) == "F#")
    langs(1)(3) = "python4"
    assert(langs(1)(3) == "python4")
    assert((langs(1)(100) -- "foo") == None)
    assert((langs(1)(-1)(-20)(-39) -- "foo") == None)
    langs(3) = `{}`
    langs(3).java = "sux"
    assert(langs.toString == """["scala",["python2","python3",null,"python4"],null,{"java":"sux"},null,"F#"]""")
    assert(langs == parse(langs.toString))
    assert(langs(1).toSeq.size == 4)
    assert(langs.toMap.isEmpty == true)
  }

  test("not parse invalid JSON") {
    val tests = Seq(
      "hi",
      "-",
      "00",
      "0-0",
      "",
      "{}}",
      "[[]",
      "{key: 98}",
      "{'key': 98}",
      "{\"key\": 98\"}",
      "{\"key\": [98, 0}",
      """ { "key": "hi""} """,
      "{\"foo\": 98 \"bar\": 0}"
    )

    for (str <- tests) {
      intercept[JsonReaderException](parse(str))
    }
  }

  test("parse empty object") {
    val obj = json"{}"
    assert(obj.toString == "{}")
    assert(obj == `{}`)
    assert((obj -- ("foo", "bar")) == parse("{}"))
    assert(obj.toMap == Map.empty)
    assert(obj.toSeq == Nil)
  }

  test("tolerate special symbols") {
    val json = json"""{ "★": 23 }"""
    assert(json.★ == 23)
    json.★ = "23"
    assert(json.★ == "23")
    json.updateDynamic("+")(true)               //sometimes we have to resort to this json.+ won't compile
    assert(json.selectDynamic("+") == true)
  }

  test("do merges for non-objects") {
    val json = json"""{ "key": ["w"]}"""
    assert(`{}` ++ json == json)
    assert(json ++ `{}` == json)
    assert(`[]` ++ json == json)
    assert(json ++ `[]` == `[]`)
    assert(json ++ json == json)
    assert(json ++ true == true)
    assert(json ++ 20 == 20)
    assert(20 ++ json == json)
    assert(json ++ Math.PI == Math.PI)
    assert(Math.PI ++ json == json)
    assert(json ++ "hi" == "hi")
    assert(Json("hi") ++ json == json)         //sometimes we have to resort to this "hi".++(json) won't compile

    val jsonToMutate = json"""[1,2,3]"""
    val mergeResult = json ++ jsonToMutate
    jsonToMutate(0) = 7
    assert(mergeResult != json ++ jsonToMutate)
    assert(mergeResult == json"""[1,2,3]""")
  }

  test("ignore sets on primitives") {
    val jsonStr = """
      {
        "num": 0,
        "arr": [0, 2, true, "hi"],
        "bol": true
      }
    """
    val test = parse(jsonStr)
    test.num.key = 0
    test.arr.key = true
    test.bol.key = "true"

    assert(test == parse(jsonStr))
    assert(test.arr.toSeq == Seq(0, 2, true, "hi"))
    assert(test.num.toSeq.isEmpty == true)
    assert(test.num.toMap.isEmpty == true)
  }

  test("hashcode works correctly") {
    val map = mutable.Map.empty[SomeJson, Int]
    val j1 = json"""{ "key" : 0 }"""
    val j2 = json"""{ "key" : "0" }"""

    map(j1) =  0
    map(j2) = 1
    map(`{}`) = 2
    map(`[]`) = 3

    assert(map(j1) == 0)
    assert(map(j2) == 1)
    assert(map(`{}`) == 2)
    assert(map(`[]`) == 3)
  }

  test("handle multi-line strings correct") {
    val obj = `{}`
    obj.str = """my
                |multiline
                |string""".stripMargin
    assert(obj.str.toString == raw""""my\nmultiline\nstring"""")
  }

  test("handle quotes in string keys") {
    val obj = `{}`
    obj.greet = "hi\""
    assert(parse(obj.toString) == obj)
    assert(json""" { "greet": "hi\\"" } """ == obj)

    obj.nested = `{}`
    obj.nested.inner = "ho\""
    assert(parse(obj.toString) == obj)
    assert(json""" { "greet": "hi\\"",
              "nested": { "inner": "ho\\"" } } """ == obj)
  }

  test("handle numbers represented as integers") {
    val jsonStr = """{"anInt" : 1}"""
    val obj = parse(jsonStr)
    assert(obj.anInt == 1)
  }

  test("do deep copy") {
    val json = json"""{"anObj":{"aString":"hi","anInt":1},"anArray":[2.0,{"aBoolean": true},null]}"""
    assert(json.deepCopy ne json)
    assert(json.deepCopy == json)
    assert(json.deepCopy.anObj ne json.anObj)
    assert(json.deepCopy.anObj == json.anObj)
    assert(json.deepCopy.anArray ne json.anArray)
    assert(json.deepCopy.anArray == json.anArray)
  }
}
