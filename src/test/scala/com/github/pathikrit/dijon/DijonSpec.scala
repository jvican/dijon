package com.github.pathikrit.dijon

import org.specs2.mutable.Specification

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
          ]
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

      rick.contact.emails(0) mustEqual email1
      rick.contact.emails(0) mustNotEqual email2
      rick.contact.emails(1) mustEqual email2
      rick.contact.emails(1) mustNotEqual email1

      rick.contact.emails.number mustEqual None
      rick.contact.emails(-1) mustEqual None
      rick.contact.emails(2) mustEqual None
      rick.wife mustEqual None
      rick.wife.name mustEqual None

      rick.hobbies(0) mustEqual "eating"
      rick.hobbies(0) mustNotEqual "cooking"

      rick.hobbies(1).games.chess mustEqual true
      rick.hobbies(1).games.chess mustNotEqual "yes"
      rick.hobbies(1).games.football mustEqual false
      rick.hobbies(1).games.football mustNotEqual 0

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
    }

    "parse arrays" in {
      val empty = parse("[]")
      empty(0) mustEqual None

      val arr = json"""[1, true, null, "hi", {"key": "value"}]"""
      val i: Double = arr(0)
      i mustEqual 1

      val b: Boolean = arr(1)
      b must beTrue

      val n = arr(2)
      assert(n == null)

      val s: String = arr(3)
      s mustEqual "hi"

      //val m: Map[String, SomeJson] = arr(4)
      //m("key") mustEqual "value2"

      //val u: None.type = arr(5)
      //u must beNone
    }

    "not parse primitives" in {
      parse("23") must throwAn[Exception]
      parse("hi") must throwAn[Exception]
      parse("\"hi\"") must throwAn[Exception]
      parse("3.4") must throwAn[Exception]
      parse("true") must throwAn[Exception]
    }

    "parse empty object" in {
      val obj = json"{}"
      obj.toString mustEqual "{}"
    }

    "work for example 1" in {
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
      val catAge: Double = cat.age
      cat.age = catAge + 1
      assert(cat.age == 8)

      assert(cat.hobbies(1) == "purring")
      assert(cat.`is cat` == true)
      assert(cat.email == None)

      val vet = `{}`
      vet.name = "Dr. Kitty Specialist"
      vet.address = `{}`
      vet.address.name = "Silicon Valley Animal Hospital"
      vet.address.city = "Palo Alto"
      vet.address.zip = 94306

      cat.vet = vet
      assert(cat.vet.address.zip == 94306)

      println(cat)
      //{"name" : "Tigri", "hobbies" : ["eating", "purring"], "vet" : {"address" : {"city" : "Palo Alto", "zip" : 94306, "name" : "Silicon Valley Animal Hospital"}, "name" : "Dr. Kitty Specialist"}, "is cat" : true, "age" : 7.0}

      ok
    }

    "work for example 2" in {
      val json = `{}`
      json.aString = "hi"                        // compiles
      json.aBoolean = true                       // compiles
      json.anInt = 23                            // compiles
      // test.somethingElse = Option("hi")       // does not compile
      val i: Int = json.anInt
      assert(i == 23)
      //val j: Int = json.aBoolean    // run-time exception
      ok
    }
  }
}
