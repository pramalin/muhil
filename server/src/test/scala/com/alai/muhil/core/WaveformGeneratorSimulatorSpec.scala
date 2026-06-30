package com.alai.muhil.core

import cats.effect.IO
import com.alai.muhil.domain.WaveformConfig
import com.alai.muhil.domain.SignalState
import fs2.Stream
import munit.CatsEffectSuite

class WaveformGeneratorSimulatorSpec extends CatsEffectSuite:

  val generator = WaveformGeneratorSimulator[IO](sampleIntervalMs = 10)

  test("generates 1 when initialState is 1 and dutyCycle is 1.0") {
    val config = WaveformConfig(frequencyHz = 1.0, dutyCycle = 1.0, initialState = 1)
    
    generator.generate(config)
      .take(5)
      .compile
      .toList
      .map { states =>
        assertEquals(states, List.fill(5)(SignalState(1.0)))
      }
  }

  test("generates 0 when initialState is 0 and dutyCycle is 0.0") {
    val config = WaveformConfig(frequencyHz = 1.0, dutyCycle = 0.0, initialState = 0)
    
    generator.generate(config)
      .take(5)
      .compile
      .toList
      .map { states =>
        assertEquals(states, List.fill(5)(SignalState(0.0)))
      }
  }

  test("alternates between 0 and 1 with 50% duty cycle") {
    val config = WaveformConfig(frequencyHz = 10.0, dutyCycle = 0.5, initialState = 1)
    
    generator.generate(config)
      .take(20)
      .compile
      .toList
      .map { states =>
        val values = states.map(_.amplitude)
        val ones = values.count(_ == 1.0)
        val zeros = values.count(_ == 0.0)
        assert(ones > 0, "should have some 1s")
        assert(zeros > 0, "should have some 0s")
      }
  }

  test("generates correct initial state") {
    val configHigh = WaveformConfig(frequencyHz = 100.0, dutyCycle = 0.5, initialState = 1)
    val configLow = WaveformConfig(frequencyHz = 100.0, dutyCycle = 0.5, initialState = 0)
    
    for
      highStates <- generator.generate(configHigh).take(1).compile.toList
      lowStates <- generator.generate(configLow).take(1).compile.toList
    yield
      assertEquals(highStates.head, SignalState(1.0))
      assertEquals(lowStates.head, SignalState(0.0))
  }