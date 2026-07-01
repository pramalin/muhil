package com.alai.muhil.pages

import tyrian.*
import cats.effect.IO

import com.alai.muhil.*
import com.alai.muhil.components.*

object Page {
    trait Msg

    enum StatusKind {
        case SUCCESS, ERROR, LOADING
    }

    final case class Status(message: String, kind: StatusKind)

    object Status {
        val LOADING = Status("Loading", StatusKind.LOADING) 
    }

    object Urls {
        val LOGIN            = "/login"
        val SIGNUP           = "/signup"
        val FORGOT_PASSWORD  = "/forgotpassword"
        val RESET_PASSWORD = "/resetpassword"
        val PROFILE        = "/profile"
        val POST_JOB       = "/postJob"
        val JOBS             = "/jobs"
        val EMPTY            = ""
        val HOME             = "/"
        val SIMULATOR        = "/simulator"
        val HASH             = "#"
        def JOB(id: String) = s"/jobs/$id"
    }

    def get(location: String): Page =
        location match {
            case Urls.LOGIN            => LoginPage()
            case Urls.SIGNUP           => SignUpPage()
            case Urls.FORGOT_PASSWORD  => ForgotPasswordPage()
            case Urls.RESET_PASSWORD => ResetPasswordPage()
            case Urls.PROFILE        => ProfilePage()
//            case Urls.POST_JOB       => PostJobPage()
            case Urls.EMPTY | Urls.HOME | Urls.JOBS => HomePage()
            case Urls.SIMULATOR => SimulatorPage()
//            case s"/jobs/$id"           => JobPage(id)
            case _                      => NotFoundPage()
        }
}

abstract class Page extends Component[App.Msg, Page]
