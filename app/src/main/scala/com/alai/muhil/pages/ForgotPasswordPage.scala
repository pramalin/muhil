package com.alai.muhil.pages

import tyrian.*
import tyrian.http.*
import tyrian.Html.*
import cats.effect.IO
import io.circe.generic.auto.*

import com.alai.muhil.common.*
import com.alai.muhil.components.*
import com.alai.muhil.domain.auth.*
import com.alai.muhil.*

final case class ForgotPasswordPage(
    email: String = "",
    status: Option[Page.Status] = None
) extends FormPage("Forgot Password", status) {
    import ForgotPasswordPage.*

    override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
        case UpdateEmail(e) => (this.copy(email = e), Cmd.None)
        case AttemptResetPassword => 
            if (!email.matches(Constants.emailRegex.regex))
                (setErrorStatus("Please insert a valid email"), Cmd.None)
            else
                 (this, Commands.resetPassword(email))
        case ResetSuccess() =>
             (setSuccessStatus("Check your email!"), Cmd.None)
        case ResetFailure(error) =>
             (setErrorStatus(error), Cmd.None)
        case _ => (this, Cmd.None)
    }

    override protected def renderFormContent(): List[Html[App.Msg]]  = List(
       renderInput("Email", "email", "text", true, UpdateEmail(_)),
       button(`type` := "button", onClick(AttemptResetPassword))("Send Email"),
       Anchors.renderSimpleNavLink( "Have a token?", Page.Urls.RESET_PASSWORD, "auth-link")
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


object ForgotPasswordPage {
    trait Msg extends App.Msg
    case class UpdateEmail(email: String) extends Msg
    // actions
    case object AttemptResetPassword extends Msg
    case class ResetFailure(error: String) extends Msg
    case class ResetSuccess() extends Msg
    
    object Endpoints {
        val resetPassword = new Endpoint[Msg] {
            override val location: String = Constants.endpoints.forgotPassword
            override val method: Method = Method.Post
            override val onError: HttpError => Msg = e => ResetFailure(e.toString)
            override val onResponse: Response => Msg = _ => ResetSuccess()
        }
    }

    object Commands {
        def resetPassword(email: String): Cmd[IO, Msg] =
            Endpoints.resetPassword.call(ForgotPasswordInfo(email))
    }
}
