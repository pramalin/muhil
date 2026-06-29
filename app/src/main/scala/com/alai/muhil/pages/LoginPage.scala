package com.alai.muhil.pages

import tyrian.*
import tyrian.http.*
import tyrian.Html.*
import cats.effect.IO
import tyrian.cmds.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

import com.alai.muhil.core.*
import com.alai.muhil.common.*
import com.alai.muhil.domain.auth.*
import com.alai.muhil.components.*

import com.alai.muhil.*

final case class LoginPage(
    email: String = "",
    password: String = "",
    status: Option[Page.Status] = None
) extends FormPage("Login", status) {
    import LoginPage.*

    override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
        case UpdateEmail(e) => (this.copy(email = e), Cmd.None)
        case UpdatePassword(p) => (this.copy(password = p), Cmd.None)
        case AttemptLogin => 
            if (!email.matches(Constants.emailRegex.regex))
                (setErrorStatus("Invalid email"), Cmd.None)
            else if (password.isEmpty())
                (setErrorStatus("Please enter a password"), Cmd.None)
            else
                 (this, Commands.login(
                    LoginInfo(email, password)))
        case LoginError(message) =>
             (setErrorStatus(message), Cmd.None)
        case LoginSuccess(token) =>
             (setSuccessStatus("Success"), Cmd.Emit(Session.SetToken(email, token, isNewUser = true)))
        case _ => (this, Cmd.None)
    }

    override protected def renderFormContent(): List[Html[App.Msg]]  = List(
       renderInput("Email", "email", "text", true, UpdateEmail(_)),
       renderInput("Password", "password", "password", true, UpdatePassword(_)),
       button(`type` := "button", onClick(AttemptLogin))("Log In"),
       Anchors.renderSimpleNavLink("Forgot password?", Page.Urls.FORGOT_PASSWORD, "auth-link")
    )
    

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // private
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // util
    def setErrorStatus(message: String): Page =
        this.copy(status = Some(Page.Status(message, Page.StatusKind.ERROR)))
    def setSuccessStatus(message: String): Page =
        this.copy(status = Some(Page.Status(message, Page.StatusKind.SUCCESS)))
}


object LoginPage {
    trait Msg extends App.Msg
    case class UpdateEmail(email: String) extends Msg
    case class UpdatePassword(password: String) extends Msg
    // actions
    case object AttemptLogin extends Msg
    case object NoOp extends Msg
    // statuses
    case class LoginError(error: String) extends Msg
    case class LoginSuccess(token: String) extends Msg

    object Endpoints {
        val login = new Endpoint[Msg] {
            override val location: String = Constants.endpoints.login
            override val method: Method = Method.Post
            override val onError: HttpError => Msg =
                e => LoginError(e.toString)
            override val onResponse: Response => Msg = response => {
                val maybeToken = response.headers.get("authorization")
                maybeToken match {
                    case Some(token) => LoginSuccess(token)
                    case None => LoginError("Invalid username or password")
                }
            }
        }
    }

    object Commands {
        def login(loginInfo: LoginInfo): Cmd[IO, Msg] =
            Endpoints.login.call(loginInfo)
    }
}
