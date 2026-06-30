package com.alai.muhil.domain


case class WaveformConfig(
    frequencyHz: Double,
    dutyCycle: Double, // 0 to 1.0
    initialState: Int, // 0 or 1
  )
