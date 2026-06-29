package com.alai.muhil.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO

import com.alai.muhil.*

final case class HomePage() extends Page {
    override def initCmd: Cmd[IO, App.Msg] = Cmd.None
    override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = (this, Cmd.None)
    override def view(): Html[App.Msg] = Html.div(
        Html.h1("Home Page")
    )
}
