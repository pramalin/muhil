package com.alai.muhil.agent

import munit.FunSuite
import com.alai.muhil.domain.WaveformConfig

class PromptParserSpec extends FunSuite {

  test("parses frequency only with default 50% duty") {
    val res = PromptParser.parse("create 1000hz square wave")
    assert(res.isRight)
    val cfg = res.toOption.get
    assertEqualsDouble(cfg.frequencyHz, 1000.0, 1e-6)
    assertEqualsDouble(cfg.dutyCycle, 0.5, 1e-6)
  }

  test("parses kHz and percentage") {
    val res = PromptParser.parse("1khz 25% duty")
    assert(res.isRight)
    val cfg = res.toOption.get
    assertEqualsDouble(cfg.frequencyHz, 1000.0, 1e-6)
    assertEqualsDouble(cfg.dutyCycle, 0.25, 1e-6)
  }
}
