package com.alai.muhil

import cats.effect.{IO, IOApp}

object Application extends IOApp.Simple:
  val run = QuickstartServer.run[IO]
