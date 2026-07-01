package com.alai.muhil.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import io.circe.*
import io.circe.generic.auto.*

import com.alai.muhil.*
import com.alai.muhil.common.Endpoint

  object SimulatorPage {
    enum Msg extends App.Msg {
      case UpdatePrompt(value: String)
      case Run
      case GotResult(res: SimulationResponse)
      case Error(msg: String)
      case ResetZoom
      case ZoomIn
      case ZoomOut
    }

  case class SimulationRequest(prompt: String)
  case class SimulationResponse(x: Vector[Double], y: Vector[Double])
}

final case class SimulatorPage(
  prompt: String = "",
  x: Vector[Double] = Vector.empty,
  y: Vector[Double] = Vector.empty,
  status: Option[String] = None
) extends Page {

  import SimulatorPage.*


  object SimEndpoint extends Endpoint[App.Msg] {
    // Use absolute URL since frontend dev server runs on a different port (1234)
    val location = "http://localhost:8080/simulate"
    val method = tyrian.http.Method.Post
    val onResponse = Endpoint.onResponse[SimulationResponse, App.Msg](
      r => Msg.GotResult(r),
      e => Msg.Error(e)
    )
    val onError = e => Msg.Error(e.toString)
  }

  override def initCmd: Cmd[IO, App.Msg] = Cmd.None

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
    case m: Msg.UpdatePrompt =>
      val Msg.UpdatePrompt(v) = m
      (copy(prompt = v), Cmd.None)

    case Msg.Run =>
      (
        copy(status = Some("Running...")),
        SimEndpoint.call(SimulationRequest(prompt))
      )

    case m: Msg.GotResult =>
      val Msg.GotResult(res) = m

      val cmd = Cmd.SideEffect[IO] {
        import scala.scalajs.js
        import scala.scalajs.js.JSConverters.*
        import com.alai.muhil.js.Plotly

        val trace = js.Dynamic.literal(
          x = res.x.toJSArray,
          y = res.y.toJSArray,
          `type` = "scatter",
          mode = "lines",
          line = js.Dynamic.literal(shape = "hv")
        )

        // Derive a sensible dtick based on visible range
        val xMin = if (res.x.nonEmpty) res.x.head else 0.0
        val xMax = if (res.x.nonEmpty) res.x.last else 1.0
        val span = xMax - xMin
        val dtick = if (span <= 0.01) 0.001
          else if (span <= 0.1) 0.01
          else if (span <= 1.0) 0.1
          else 1.0

        val layout = js.Dynamic.literal(
          title = "Waveform",
          xaxis = js.Dynamic.literal(
            title = js.Dynamic.literal(text = "Time (s)"),
            showgrid = true,
            tickmode = "linear",
            dtick = dtick,
            ticksuffix = " s",
            range = js.Array(xMin, xMax)
          ),
          yaxis = js.Dynamic.literal(
            title = js.Dynamic.literal(text = "Amplitude (0/1)"),
            range = js.Array(0, 1.2),
            tickvals = js.Array(0, 1),
            ticktext = js.Array("LOW", "HIGH")
          )
        )

        Plotly.newPlot("chart", js.Array(trace), layout)
      }

      (copy(x = res.x, y = res.y, status = Some("Done")), cmd)

    case Msg.ResetZoom =>
      val cmd = Cmd.SideEffect[IO] {
        import scala.scalajs.js
        import com.alai.muhil.js.Plotly
        Plotly.relayout("chart", js.Dynamic.literal(
          "xaxis.autorange" -> true
        ))
      }
      (this, cmd)

    case Msg.ZoomIn =>
      val cmd = Cmd.SideEffect[IO] {
        import scala.scalajs.js
        import com.alai.muhil.js.Plotly
        Plotly.relayout("chart", js.Dynamic.literal(
          "xaxis.range" -> js.Array(0, 1)
        ))
      }
      (this, cmd)

    case Msg.ZoomOut =>
      val cmd = Cmd.SideEffect[IO] {
        import scala.scalajs.js
        import com.alai.muhil.js.Plotly
        Plotly.relayout("chart", js.Dynamic.literal(
          "xaxis.range" -> js.Array(0, 2)
        ))
      }
      (this, cmd)

    case m: Msg.Error =>
      val Msg.Error(e) = m
      (copy(status = Some(e)), Cmd.None)

    case _ => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] =
    div(
      h2("Simulator"),
      textarea(
        placeholder := "Enter prompt...",
        value := prompt,
        onInput(s => Msg.UpdatePrompt(s))
      )(),
      button(onClick(Msg.Run))("Run"),
      div(
        button(onClick(Msg.ResetZoom))("Reset Zoom"),
        button(onClick(Msg.ZoomIn))("Zoom In"),
        button(onClick(Msg.ZoomOut))("Zoom Out")
      ),
      status.map(s => div(p(s))).getOrElse(div()),
      div(id := "chart")()
    )
}
