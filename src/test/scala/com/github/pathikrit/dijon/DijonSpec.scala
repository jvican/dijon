package com.github.pathikrit.dijon

import org.specs2.mutable.Specification
import scala.collection.mutable

class DijonSpec extends Specification {

  "dijon" should {

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

    "parse objects" in {
      rick.name mustEqual name
      rick.name mustNotEqual "Ryan"

      rick.age mustEqual age
      rick.age mustNotEqual 3

      rick.`class` aka "Scala keywords need to be escaped with ticks" mustEqual "human"
      rick.`class` mustNotEqual "dog"

      rick.`is online` aka "Keys with spaces need to be escaped with ticks" mustEqual true
      rick.`is online` mustNotEqual 1

      rick.weight mustEqual 175.1
      rick.weight mustNotEqual 175.0999
      rick.weight.keys mustEqual None

      rick.contact.emails(0) mustEqual email1
      rick.contact.emails(0) mustNotEqual email2
      rick.contact.emails(1) mustEqual email2
      rick.contact.emails(1) mustNotEqual email1

      rick.contact.emails.number mustEqual None
      rick.contact.emails(-1) mustEqual None
      rick.contact.emails(2) mustEqual None
      rick.wife mustEqual None
      rick.wife.name mustEqual None
      rick.wife.keys mustEqual None

      rick.hobbies(0) mustEqual "eating"
      rick.hobbies(0) mustNotEqual "cooking"
      rick.hobbies(0).keys mustEqual None

      rick.hobbies(1).games.chess mustEqual true
      rick.hobbies(1).games.chess mustNotEqual "yes"
      rick.hobbies(1).games.football mustEqual false
      rick.hobbies(1).games.football mustNotEqual 0

      rick.hobbies(1).games.football.as[Boolean] must beSome(false)
      //rick.hobbies(1).games.football.as[Int] must beNone
      //rick.hobbies(1).games.foosball.as[Boolean] must beNone

      rick.hobbies(2)(0) mustEqual "coding"
      rick.hobbies(2)(0) mustNotEqual "cooking"

      rick.hobbies(2)(1)(1) mustEqual "scala"
      rick.hobbies(2)(1)(1) mustNotEqual "java"
      rick.hobbies(2)(100) mustEqual None
      rick.hobbies(2)(100) mustNotEqual null

      rick.hobbies(3) mustEqual null
      rick.hobbies(3) mustNotEqual None
      rick.hobbies(4) mustNotEqual null
      rick.hobbies(4) mustEqual None

      rick.toMap.keySet mustEqual Set("name", "age", "class", "contact", "is online", "weight", "hobbies", "toMap")
      rick.selectDynamic("toMap")(1) mustEqual 345
      rick mustEqual parse(rick.toString) // round-trip test
      rick.toSeq must beEmpty
    }

    "parse arrays" in {
      val empty = parse("[]")
      empty(0) mustEqual None
      empty mustEqual `[]`
      empty mustEqual parse(empty.toString)
      empty.toSeq mustEqual Seq.empty
      empty.toMap mustEqual Map.empty

      val arr = json"""[1, true, null, "hi", {"key": "value"}]"""
      arr mustEqual parse(arr.toString)

      val Some(i: Double) = arr(0).as[Double]
      i mustEqual 1

      val Some(b: Boolean) = arr(1).as[Boolean]
      b must beTrue

      val n = arr(2)
      assert(n == null)

      val Some(s: String) = arr(3).as[String]
      s mustEqual "hi"

      val m = arr(4).toMap
      m("key") mustEqual "value"

      val u = arr(5)
      u mustEqual None

      arr.toSeq must have size 5
      arr.toMap mustEqual Map.empty
    }

    "handle multiline strings correct" in {
      val ml = `{}`
      ml.str = """my
                 |multiline
                 |string""".stripMargin
      ml.str.toString mustEqual raw""""my\nmultiline\nstring""""
    }

    "handle json upates" in {
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
      //assert(cat.age.as[Boolean] == None)

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

      (cat.vet.address -- "city") mustEqual json"""{ "name" : "Animal Hospital", "zip": 94306}"""
    }

    "handle merges" in {
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

      (scala ++ java).name mustNotEqual (java ++ scala).name
      (scala ++ java).bugs mustEqual (java ++ scala).bugs
    }

    "be type-safeish" in {
      val json = `{}`
      json.aString = "hi"                        // compiles
      json.aBoolean = true                       // compiles
      json.anInt = 23                            // compiles
      // test.somethingElse = Option("hi")       // does not compile
      val Some(i: Int) = json.anInt.as[Int]
      assert(i == 23)
      //val j: Int = json.aBoolean.as[Int]    // run-time exception
      ok
    }

    "grow arrays" in {
      val langs = json"""["scala", ["python2", "python3"]]"""
      langs(-2) = "java"
      langs(5) = "F#"
      langs(3) mustEqual null
      langs(5) mustEqual "F#"
      langs(1)(3) = "python4"
      langs(1)(3) mustEqual "python4"
      (langs(1)(100) -- "foo") mustEqual None
      (langs(1)(-1)(-20)(-39) -- "foo") mustEqual None
      langs(3) = `{}`
      langs(3).java = "sux"
      langs.toString mustEqual """["scala", ["python2", "python3", null, "python4"], null, {"java" : "sux"}, null, "F#"]"""
      langs mustEqual parse(langs.toString)
      langs(1).toSeq must have size 4
      langs.toMap must beEmpty
    }

    "not parse invalid jsons" in {
      val tests = Seq(
        "23",
        "hi",
        "\"hi\"",
        "3.4",
        "true",
        "",
        "null",
        "\"null\"",
        "{}}",
        "[[]",
        "{key: 98}",
        "{\"key\": 98\"}"
      )

      examplesBlock {
        for (str <- tests) {
          parse(str) must throwAn[IllegalArgumentException]
        }
      }
    }

    "parse empty object" in {
      val obj = json"{}"
      obj.toString mustEqual "{}"
      obj mustEqual `{}`
      (obj -- ("foo", "bar")) mustEqual parse("{}")
      obj.toMap mustEqual Map.empty
      obj.toSeq mustEqual Nil
    }

    "tolerate special symbols" in {
      val json = json"""{ "★": 23 }"""
      json.★ mustEqual 23
      json.★ mustNotEqual "23"
      json.★ = "23"
      json.★ mustEqual "23"
      json.updateDynamic("+")(true)               //sometimes we have to resort to this json.+ won't compile
      json.selectDynamic("+") mustEqual true
      json.selectDynamic("+") mustNotEqual "true"
    }

    "do merges for non-objects" in {
      val json = json"""{ "key": ["w"]}"""
      `{}` ++ json mustEqual json
      json ++ `{}` mustEqual json
      `[]` ++ json mustEqual json
      json ++ `[]` mustEqual `[]`
      json ++ json mustEqual json
      json ++ true mustEqual true
      json ++ 20 mustEqual 20
      20 ++ json mustEqual json
      json ++ "hi" mustEqual "hi"
      //"hi" ++ json mustEqual json
    }

    "ignore sets on primitives" in {
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

      test mustEqual parse(jsonStr)
      test.arr.toSeq mustEqual Seq(0, 2, true, "hi")
      test.num.toSeq must have size 0
      test.num.toMap must beEmpty
    }

    "hashcode works correctly" in {
      val map = mutable.Map.empty[SomeJson, Int]
      val j1 = json"""{ "key" : 0 }"""
      val j2 = json"""{ "key" : "0" }"""

      map(j1) =  0
      map(j2) = 1
      map(`{}`) = 2
      map(`[]`) = 3

      map(j1) mustEqual 0
      map(j2) mustEqual 1
      map(`{}`) mustEqual 2
      map(`[]`) mustEqual 3
    }
  }
}
