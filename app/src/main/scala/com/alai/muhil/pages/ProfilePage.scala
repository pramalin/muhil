package com.alai.muhil.pages

import tyrian.*
import tyrian.Html.*
import tyrian.http.*
import io.circe.generic.auto.*

import com.alai.muhil.*
import com.alai.muhil.core.*
import com.alai.muhil.common.*
import com.alai.muhil.domain.auth.*

final case class ProfilePage(
    oldPassword: String = "",
    newPassword: String = "",
    status: Option[Page.Status] = None
) extends FormPage("Profile", status) {
    import ProfilePage.*

    override def update(msg: App.Msg): (Page, Cmd[cats.effect.IO, App.Msg]) = msg match {
        case UpdateOldPassword(p) => (this.copy(oldPassword = p), Cmd.None)
        case UpdateNewPassword(p) => (this.copy(newPassword = p), Cmd.None)
        // action
        case AttemptChangePassword => 
            if (oldPassword.isEmpty())
                (setErrorStatus("Please provide current password"), Cmd.None)
            else if (oldPassword.isEmpty())
                (setErrorStatus("Please provide new password"), Cmd.None)
            else
                 (this, Commands.changePassword(oldPassword, newPassword))
        case ChangePasswordFailure(error) =>
            (setErrorStatus(error), Cmd.None)
        case ChangePasswordSuccess =>
            (setSuccessStatus("Password reset successfully"), Cmd.None)

        case _ =>
             (this, Cmd.None)
    }

    override def view(): Html[App.Msg] =
        if (Session.isActive) super.view()
        else renderInvalidPage

    override protected def renderFormContent(): List[Html[App.Msg]]  = List(
       renderInput("Old Password", "oldPassword", "password", true, UpdateOldPassword(_)),
       renderInput("New Password", "newPassword", "password", true, UpdateNewPassword(_)),
       button(`type` := "button", onClick(AttemptChangePassword))("Change Password"),
    )

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // private
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    // UI
    def renderInvalidPage = 
        div(
            h1("Profile"),
            div("Ouch! It seems you're not logged in yet.")
        )
        
    // util
    def setErrorStatus(message: String): Page =
        this.copy(status = Some(Page.Status(message, Page.StatusKind.ERROR)))
    def setSuccessStatus(message: String): Page =
        this.copy(status = Some(Page.Status(message, Page.StatusKind.SUCCESS)))
}

object ProfilePage {
    trait Msg extends App.Msg
    case class UpdateOldPassword(password: String) extends Msg
    case class UpdateNewPassword(password: String) extends Msg
    // actions
    case object AttemptChangePassword extends Msg
    case class  ChangePasswordFailure(error: String) extends Msg
    case object ChangePasswordSuccess extends Msg
    
    object Endpoints {
        val changePassword = new Endpoint[Msg] {
            override val location: String = Constants.endpoints.changePassword
            override val method: Method = Method.Put
            override val onError: HttpError => Msg = e => ChangePasswordFailure(e.toString)
            override val onResponse: Response => Msg = response => response.status match {
                case Status(200, _) => ChangePasswordSuccess
                case Status(404, _) => ChangePasswordFailure("Server says the user doesn't exists.")
                case Status(s, _) if s >= 400 && s < 500 => ChangePasswordFailure("Invalid credentials.")
                case _ => ChangePasswordFailure(s"Unexpected response: ${response.status}")
            }
        }
    }

    object Commands {
        def changePassword(oldPassword: String, newPassword: String) =
            Endpoints.changePassword.callAuthorized(NewPasswordInfo(oldPassword, newPassword))
    }
}