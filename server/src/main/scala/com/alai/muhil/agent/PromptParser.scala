package com.alai.muhil.agent

import com.alai.muhil.domain.WaveformConfig

object PromptParser:

  private val freqRegex = """(\d+\.?\d*)\s*(k?hz)""".r
  private val dutyRegex = """(\d+\.?\d*)\s*%""".r

  def parse(prompt: String): Either[String, WaveformConfig] =
    val lower = prompt.toLowerCase

    val freqHz = freqRegex.findFirstMatchIn(lower).map { m =>
      val value = m.group(1).toDouble
      val unit = m.group(2)
      if unit == "khz" then value * 1000 else value
    }

    val duty = dutyRegex.findFirstMatchIn(lower).map { m =>
      m.group(1).toDouble / 100.0
    }

    freqHz match
      case Some(f) =>
        val d = duty.getOrElse(0.5) // default 50% duty cycle
        Right(WaveformConfig(f, d))
      case None =>
        Left("Unable to parse frequency")
