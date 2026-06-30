package com.alai.muhil.core

import com.alai.muhil.domain.WaveformConfig
import com.alai.muhil.domain.SignalState
import fs2.Stream
import scala.concurrent.duration.*

class WaveformGeneratorSimulator[F[_]](
    sampleIntervalMs: Long
) extends WaveformGenerator[F]:

  override def generate(config: WaveformConfig): Stream[F, SignalState] =
    val periodMs = (1000.0 / config.frequencyHz).toLong
    val activeDurationMs = (periodMs * config.dutyCycle).toLong
    
    Stream.iterate(0L)(t => (t + sampleIntervalMs) % periodMs)
      .map { elapsed =>
        val currentState = 
          if (activeDurationMs == 0) 0.0
          else if (elapsed < activeDurationMs) config.initialState.toDouble
          else (1 - config.initialState).toDouble
        SignalState(currentState)
      }

object WaveformGeneratorSimulator:
  def apply[F[_]](sampleIntervalMs: Long = 1): WaveformGeneratorSimulator[F] =
    new WaveformGeneratorSimulator(sampleIntervalMs)