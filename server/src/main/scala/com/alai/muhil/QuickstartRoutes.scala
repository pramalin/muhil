package com.alai.muhil

import cats.effect.Sync
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import io.circe.*
import cats.effect.Sync
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.Method.POST
import org.http4s.circe.jsonEncoderOf
import org.http4s.circe.jsonOf
import com.alai.muhil.agent.PromptParser
import com.alai.muhil.core.{WaveformGeneratorSimulator, SimulationService}

object QuickstartRoutes:

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }

  case class SimulationRequest(prompt: String)
  case class SimulationResponse(x: Vector[Double], y: Vector[Double])

  given Decoder[SimulationRequest] = new Decoder[SimulationRequest] {
    final def apply(c: HCursor): Decoder.Result[SimulationRequest] =
      c.downField("prompt").as[String].map(SimulationRequest(_))
  }

  given Encoder[SimulationResponse] = new Encoder[SimulationResponse] {
    final def apply(a: SimulationResponse): Json = Json.obj(
      ("x", Json.fromValues(a.x.map(Json.fromDoubleOrNull))),
      ("y", Json.fromValues(a.y.map(Json.fromDoubleOrNull)))
    )
  }

  def simulationRoutes[F[_]: cats.effect.Concurrent]: HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl.*

    given EntityDecoder[F, SimulationRequest] = jsonOf
    given EntityEncoder[F, SimulationResponse] = jsonEncoderOf

    // Use the same sampling interval (in seconds) as the service (ms -> s)
    // placeholder, actual sampling decided per request based on frequency
    val defaultSampleIntervalMs = 1.0
    val simulator = WaveformGeneratorSimulator[F](defaultSampleIntervalMs / 1000.0)

    HttpRoutes.of[F] {
      case req @ POST -> Root / "simulate" =>
        for {
          body <- req.as[SimulationRequest]
          result <- PromptParser.parse(body.prompt) match
            case Left(err) => BadRequest(err)
            case Right(config) =>
              // TEMP FIX: force 50% duty to validate simulator behavior
              val fixedConfig = config.copy(dutyCycle = 0.5)
              println(s"CONFIG DEBUG => $fixedConfig")

              // Adaptive sampling: target ~20 samples per period
              val samplesPerPeriod = 20.0
              val periodSec = 1.0 / fixedConfig.frequencyHz
              val sampleIntervalSec = periodSec / samplesPerPeriod
              val sampleIntervalMs = sampleIntervalSec * 1000.0

              val adaptiveSimulator = WaveformGeneratorSimulator[F](sampleIntervalSec)

              // Adaptive duration: show ~5 periods for clarity
              val durationSec = periodSec * 5.0
              val durationMs = durationSec * 1000.0

              SimulationService
                .sample(adaptiveSimulator, fixedConfig, durationMs = durationMs, sampleIntervalMs = sampleIntervalMs)
                .flatMap { case (x, y) => Ok(SimulationResponse(x, y)) }
        } yield result
    }
