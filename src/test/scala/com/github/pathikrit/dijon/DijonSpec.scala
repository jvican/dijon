package com.github.pathikrit.dijon

import com.github.plokhotnyuk.jsoniter_scala.core.JsonReaderException
import org.scalatest.{Matchers, WordSpec}
import scala.collection.mutable

class DijonSpec extends WordSpec with Matchers {
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
      rick.name shouldBe name

      rick.age shouldBe age

      rick.`class` shouldBe "human"

      rick.`is online` shouldBe true

      rick.weight shouldBe 175.1
      rick.weight.keys shouldBe None

      rick.contact.emails(0) shouldBe email1
      rick.contact.emails(1) shouldBe email2

      rick.contact.emails.number shouldBe None
      rick.contact.emails(-1) shouldBe None
      rick.contact.emails(2) shouldBe None
      rick.wife shouldBe None
      rick.wife.name shouldBe None
      rick.wife.keys shouldBe None

      rick.hobbies(0) shouldBe "eating"
      rick.hobbies(0).keys shouldBe None

      rick.hobbies(1).games.chess shouldBe true
      rick.hobbies(1).games.football shouldBe false

      rick.hobbies(1).games.football.as[Boolean] shouldBe Some(false)
      rick.hobbies(1).games.football.as[Int] shouldBe None
      rick.hobbies(1).games.foosball.as[Boolean] shouldBe None

      rick.hobbies(2)(0) shouldBe "coding"

      rick.hobbies(2)(1)(1) shouldBe "scala"
      rick.hobbies(2)(100) shouldBe None

      rick.hobbies(3) shouldBe None
      rick.hobbies(4) shouldBe None

      rick.toMap.keySet shouldBe Set("name", "age", "class", "contact", "is online", "weight", "hobbies", "toMap")
      rick.selectDynamic("toMap")(1) shouldBe 345
      rick shouldBe parse(rick.toString) // round-trip test
      rick.toSeq shouldBe empty

      pretty(rick) shouldBe
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
          |}""".stripMargin
    }

    "parse arrays" in {
      val empty = parse("[]")
      empty(0) shouldBe None
      empty shouldBe `[]`
      empty shouldBe parse(empty.toString)
      empty.toSeq shouldBe Seq.empty
      empty.toMap shouldBe Map.empty

      val arr = json"""[1, true, null, "hi", {"key": "value"}]"""
      arr shouldBe parse(arr.toString)

      val Some(i: Int) = arr(0).as[Int]
      i shouldBe 1

      val Some(b: Boolean) = arr(1).as[Boolean]
      b shouldBe true

      val n = arr(2)
      assert(n == None)

      val Some(s: String) = arr(3).as[String]
      s shouldBe "hi"

      val m = arr(4).toMap
      m("key") shouldBe "value"

      val u = arr(5)
      u shouldBe None

      arr.toSeq should have size 5
      arr.toMap shouldBe Map.empty
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
      val Some(catAge: Int) = cat.age.as[Int]    // type inference
      assert(catAge == age)
      assert(cat.age.as[Boolean].isEmpty)

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
      assert(vet.phones == mutable.Seq(None, None, phone))  // first 2 entries None

      vet.address = `{}`
      vet.address.name = "Animal Hospital"
      vet.address.city = "Palo Alto"
      vet.address.zip = 94306
      //assert(vet.address == mutable.Map("zip" -> 94306, "name" -> "Animal Hospital", "city" -> "Palo Alto")) FIXME: fix assertion

      cat.vet = vet                            // set the cat.vet to be the vet json object we created above
      assert(cat.vet.phones(2) == phone)
      assert(cat.vet.address.zip == 94306)     // json deep access

      assert(cat == parse(cat.toString))   // round-trip test

      var basicCat = cat -- "vet"                                  // remove 1 key
      basicCat = basicCat -- ("hobbies", "is cat", "paws")         // remove multiple keys ("paws" is not in cat)
      assert(basicCat == json"""{ "name": "Tigri", "age": 7}""")   // after dropping some keys above
      basicCat.remove("age")                                       // remove 1 key by mutating object
      assert(basicCat == json"""{ "name": "Tigri" }""")

      (cat.vet.address -- "city") shouldBe json"""{ "name" : "Animal Hospital", "zip": 94306}"""
    }

    "handle nulls" in {
      val t = json"""{"a": null, "b": {"c": null}}"""
      t.a shouldBe None
      t.b.c shouldBe None

      val v = parse("""{"a": null}""")
      v.a shouldBe None

      assert(t == parse(t.toString)) //round-trip test

      t.b.c = v
      t.b.c.a shouldBe None
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

      (scala ++ java).bugs shouldBe ((java ++ scala).bugs)
    }

    "be type-safeish" in {
      var j = json"""{"name" : "chen"}"""
      j.name shouldBe "chen"
      j.name.as[String] shouldBe Some("chen")
      j.name.as[Int] shouldBe None

      j = `{}`
      j.aString = "hi"                        // compiles
      j.aBoolean = true                       // compiles
      j.anInt = 23                            // compiles
      //j.somethingElse = Option("hi")        // does not compile
      val Some(i: Int) = j.anInt.as[Int]
      i shouldBe 23
      j.aBoolean.as[Int] shouldBe None
    }

    "grow arrays" in {
      val langs = json"""["scala", ["python2", "python3"]]"""
      langs(-2) = "java"
      langs(5) = "F#"
      langs(3) shouldBe None
      langs(5) shouldBe "F#"
      langs(1)(3) = "python4"
      langs(1)(3) shouldBe "python4"
      (langs(1)(100) -- "foo") shouldBe None
      (langs(1)(-1)(-20)(-39) -- "foo") shouldBe None
      langs(3) = `{}`
      langs(3).java = "sux"
      langs.toString shouldBe """["scala",["python2","python3",null,"python4"],null,{"java":"sux"},null,"F#"]"""
      langs shouldBe parse(langs.toString)
      langs(1).toSeq should have size 4
      langs.toMap shouldBe empty
    }

    "not parse invalid jsons" in {
      val tests = Seq(
        "hi",
        "-",
        "00",
        "",
        //"{}}", FIXME: should be fixed in jsoniter-scala-core
        "[[]",
        "{key: 98}",
        "{'key': 98}",
        "{\"key\": 98\"}",
        """ { "key": "hi""} """           //http://stackoverflow.com/questions/15637429/
      )

      for (str <- tests) {
        intercept[JsonReaderException](parse(str))
      }
    }

    "parse empty object" in {
      val obj = json"{}"
      obj.toString shouldBe "{}"
      obj shouldBe `{}`
      (obj -- ("foo", "bar")) shouldBe parse("{}")
      obj.toMap shouldBe Map.empty
      obj.toSeq shouldBe Nil
    }

    "tolerate special symbols" in {
      val json = json"""{ "★": 23 }"""
      json.★ shouldBe 23
      json.★ = "23"
      json.★ shouldBe "23"
      json.updateDynamic("+")(true)               //sometimes we have to resort to this json.+ won't compile
      json.selectDynamic("+") shouldBe true
    }

    "do merges for non-objects" in {
      val json = json"""{ "key": ["w"]}"""
      `{}` ++ json shouldBe json
      json ++ `{}` shouldBe json
      `[]` ++ json shouldBe json
      json ++ `[]` shouldBe `[]`
      json ++ json shouldBe json
      json ++ true shouldBe true
      json ++ 20 shouldBe 20
      20 ++ json shouldBe json
      json ++ "hi" shouldBe "hi"
      //"hi" ++ json shouldBe json
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

      test shouldBe parse(jsonStr)
      test.arr.toSeq shouldBe Seq(0, 2, true, "hi")
      test.num.toSeq should have size 0
      test.num.toMap shouldBe empty
    }

    "hashcode works correctly" in {
      val map = mutable.Map.empty[SomeJson, Int]
      val j1 = json"""{ "key" : 0 }"""
      val j2 = json"""{ "key" : "0" }"""

      map(j1) =  0
      map(j2) = 1
      map(`{}`) = 2
      map(`[]`) = 3

      map(j1) shouldBe 0
      map(j2) shouldBe 1
      map(`{}`) shouldBe 2
      map(`[]`) shouldBe 3
    }

    "handle multiline strings correct" in {
      val obj = `{}`
      obj.str = """my
                  |multiline
                  |string""".stripMargin
      obj.str.toString shouldBe raw""""my\nmultiline\nstring"""" //"
    }

    "handle quotes in string keys" in {
      val obj = `{}`
      obj.greet = "hi\""
      parse(obj.toString) shouldBe obj
      json""" { "greet": "hi\\"" } """ shouldBe obj

      obj.nested = `{}`
      obj.nested.inner = "ho\""
      parse(obj.toString) shouldBe obj
      json""" { "greet": "hi\\"",
                "nested": { "inner": "ho\\"" } } """ shouldBe obj
    }

    "handle numbers represented as integers" in {
      val jsonStr = """{"anInt" : 1}"""
      val obj = parse(jsonStr)
      obj.anInt shouldBe 1
    }
  }
}
