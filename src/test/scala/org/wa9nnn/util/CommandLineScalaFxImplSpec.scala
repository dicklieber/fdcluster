package org.wa9nnn.util

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import _root_.scalafx.application.JFXApp.Parameters

//class CommandLineScalaFxImplSpec extends Specification with Mockito {
//  val parameters: Parameters = mock[Parameters]
//  parameters.unnamed returns (Seq("skipSomethingFlag"))
//  parameters.named returns (
//    Seq("paramPresent" -> "paramPresentValue",
//      "paramInt" -> "42"
//    ).toMap)
//  parameters.raw returns (Seq.empty)
//
//  val commandLine = new CommandLineScalaFxImpl(parameters)
//  "CommandLineScalaFxImpl" >> {
//    "getString" in {
//      commandLine.getString("paramPresent") must beSome("paramPresentValue")
//    }
//    "getString Missing" >> {
//      commandLine.getString("paramMissing") must beNone
//    }
//
//    "getInt" in {
//      commandLine.getInt("paramInt") must beSome(42)
//    }
//
//    "is missing" in {
//      commandLine.is("paramMissing") must beFalse
//
//    }
//    "is " in {
//      commandLine.is("skipSomethingFlag") must beTrue
//
//    }
//  }
//}
