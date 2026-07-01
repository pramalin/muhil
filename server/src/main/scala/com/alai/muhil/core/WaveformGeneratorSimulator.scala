package com.alai.muhil.core

import com.alai.muhil.domain.WaveformConfig
import com.alai.muhil.domain.SignalState
import fs2.Stream
import scala.concurrent.duration.*

class WaveformGeneratorSimulator[F[_]](
    sampleIntervalSec: Double
) extends WaveformGenerator[F]:

  override def generate(config: WaveformConfig): Stream[F, SignalState] =
    val periodSec = 1.0 / config.frequencyHz

    // Integer-based phase to avoid floating precision issues
    // Ensure at least 2 samples so we can represent both high and low
    val samplesPerPeriod = math.max(2L, math.round(periodSec / sampleIntervalSec))
    // Use floor, but guarantee at least 1 high sample when dutyCycle > 0
    val rawActive = math.floor(samplesPerPeriod * config.dutyCycle).toLong
    val activeSamples = if (config.dutyCycle > 0 && rawActive == 0) 1L else rawActive

    Stream.iterate(0L)(_ + 1).map { i =>
      // Time-based phase computation avoids aliasing from integer sample counts
      val t = i.toDouble * sampleIntervalSec
      val phase = (t % periodSec) / periodSec
      val isHigh = phase < config.dutyCycle

      val value = if (isHigh) 1.0 else 0.0

      SignalState(value)
    }

object WaveformGeneratorSimulator:
  // Increase default sampling rate to 1 ms
  def apply[F[_]](sampleIntervalSec: Double = 0.001): WaveformGeneratorSimulator[F] =
    new WaveformGeneratorSimulator(sampleIntervalSec)
