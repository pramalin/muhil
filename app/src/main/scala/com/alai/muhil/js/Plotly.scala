package com.alai.muhil.js

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSImport("plotly.js-dist-min", JSImport.Default)
object Plotly extends js.Object {
  def newPlot(divId: String, data: js.Array[js.Object], layout: js.Object): Unit = js.native
  def relayout(divId: String, layout: js.Object): Unit = js.native
}
