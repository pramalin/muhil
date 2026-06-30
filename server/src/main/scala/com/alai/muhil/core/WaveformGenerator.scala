package com.alai.muhil.core

import com.alai.muhil.domain.WaveformConfig
import cats.effect.IO
import com.alai.muhil.domain.SignalState

trait WaveformGenerator[F[_]]:
    def generate(config: WaveformConfig): fs2.Stream[F, SignalState]