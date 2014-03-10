package com.github.pathikrit.dijon

import com.github.pathikrit.dijon.Json._

import org.specs2.mutable.Specification

class JsonSpec extends Specification {

  "yasjl" should {

    val (email1, email2) = ("pathikritbhowmick@msn.com", "pathikrit.bhowmick@gmail.com")
    val (name, age) = ("Rick", 27)

    val json = json"""
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
      val Some(rick) = json
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
//      val Some(empty) = parse("[]")
//      empty(0) mustEqual None
//
//      val Some(arr) = json"""[1, true, null, "hi"]"""
//      val i: Double = arr(0)
//      i mustEqual 1
//
//      val b: Boolean = arr(1)
//      b must beTrue
//
//      val n = arr(2)
//      n mustEqual null
//
//      val s: String = arr(3)
//      s mustEqual "hi"
//
//      val u = arr(4)
//      u mustEqual None
      todo
    }

    "parse primitivies" in {
//      val Some(int: Int) = parse("23")
//      int mustEqual 23
//
//      val Some(string: String) = parse("hi")
//      string mustEqual "hi"
//
//      val Some(double: Double) = parse("3.4")
//      double mustEqual "3.4"
//
//      val Some(boolean: Boolean) = parse("true")
//      boolean must beTrue
      todo
    }

    "work for examples" in {
      import com.github.pathikrit.dijon.Json._

      val (name, age) = ("Tigri", 7)
      val Some(cat) = json"""
        {
          "name": "$name",
          "age": $age,
          "hobbies": ["eating", "purring"],
          "is cat": true
        }
      """
      assert(cat.name == name)
      assert(cat.age == age)
      assert(cat.hobbies(1) == "purring")
      assert(cat.`is cat` == true)
      assert(cat.email == None)

//      val vet = JsonObject
//      vet.name = "Dr. Kitty Specialist"
//      vet.address.name = "Silicon Valley Animal Hospital"
//      vet.address.city = "Palo Alto"
//      vet.address.zip = 94306
//
//      cat.vet = vet

      println(cat)

      //val vetZip: Int = cat.vet.address.zip
      //vetZip mustEqual vet.address.zip
      //vetZip mustNotEqual 94041
      ok
    }
  }
}
