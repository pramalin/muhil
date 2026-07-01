package com.alai.muhil.core

import cats.effect.Concurrent
import cats.syntax.all.*
import com.alai.muhil.domain.WaveformConfig
import fs2.Stream

object SimulationService:

  def sample[F[_]: Concurrent](
      simulator: WaveformGeneratorSimulator[F],
      config: WaveformConfig,
      durationMs: Double,
      sampleIntervalMs: Double
  ): F[(Vector[Double], Vector[Double])] =

    val totalSamples = (durationMs / sampleIntervalMs).toInt
    // Convert ms -> seconds to match simulator
    val sampleIntervalSec = sampleIntervalMs / 1000.0

    simulator
      .generate(config)
      .take(totalSamples.toLong)
      .zipWithIndex
      .compile
      .toVector
      .map { vec =>
        // x in seconds (consistent with simulator)
        val x = vec.map { case (_, idx) => idx.toDouble * sampleIntervalSec }
        val y = vec.map { case (state, _) => state.amplitude }
        (x, y)
      }
