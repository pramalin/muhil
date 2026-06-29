package com.alai.muhil.components

import tyrian.*
import tyrian.Html.*
import scala.scalajs.js.Date

import com.alai.muhil.*

object Footer {
  def view(): Html[App.Msg] =
    div(`class` := "footer")(
      p(
        text("Written in "),
        a(href := "https://scala-lang.org", target := "blank")("Scala"),
        text("with ❤️ at "),
        a(href := "https://localhost.com", target := "blank")("Muhil Simulator")
      ),
      p(s"© Alai Engineering ${new Date().getUTCFullYear()}")
    )
}
