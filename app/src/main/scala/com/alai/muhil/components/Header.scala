package com.alai.muhil.components

import tyrian.*
import tyrian.Html.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.alai.muhil.*
import com.alai.muhil.components.*
import com.alai.muhil.core.*
import com.alai.muhil.pages.*

object Header {
    // public API
    def view() =
    div( `class` := "container-fluid p-0")(
        div( `class` := "jvm-nav")(
            div( `class` := "container")(
                nav( `class` := "navbar navbar-expand-lg navbar-light JVM-nav")(
                    div( `class` := "container")(
                        renderLogo(),
                        button(
                            `class` :="navbar-toggler",
                            `type` :="button",
                            attribute("data-bs-toggle","collapse"),
                            attribute("data-bs-target", "#navbarNav"),
                            attribute("aria-controls", "navbarNav"),
                            attribute("aria-expanded", "false"),
                            attribute("aria-label", "Toggle navigation")
                        )(
                            span(`class` :="navbar-toggler-icon")()
                        ),
                        div(`class`:= "collapse navbar-collapse", id := "navbarNav")(
                            ul(`class` := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3")(
                                renderNavLinks()
                            )
                        )
                    )
                )
            )
        )
    )

    // private API
    @js.native
    @JSImport("url:/static/img/logo.png", JSImport.Default)
    private val logoImage: String = js.native
    
    private def renderLogo() =
        a(
            href := "/",
            `class` :="navbar-brand",
            onEvent(
                "click",
                e => {
                    e.preventDefault()
                    Router.ChangeLocation("/")
                }
            )
        )(
            img(
                `class` := "home-logo",
                src := logoImage,
                alt := "Muhil Simulator"
            )

        )

    private def renderNavLinks(): List[Html[App.Msg]] = {
        val constantLinks = List(
            renderSimpleNavLink("Home", Page.Urls.HOME),
            renderSimpleNavLink("Configure", Page.Urls.SIMULATOR),
        )   

        val unauthedLinks = List(
            renderSimpleNavLink("Login", Page.Urls.LOGIN),
            renderSimpleNavLink("Sign up", Page.Urls.SIGNUP),
        )

        val authedLinks = List(
            renderSimpleNavLink("Profile", Page.Urls.PROFILE),
            renderNavLink("Log Out" , Page.Urls.HASH)(_ => Session.Logout)
        )

        constantLinks ++ (
            if (Session.isActive) authedLinks
            else unauthedLinks
        )
    }

    private def renderSimpleNavLink(text: String, location: String) =
        renderNavLink(text, location)(Router.ChangeLocation(_))

    private def renderNavLink(text: String, location: String)(location2Msg: String => App.Msg) =
        li(`class` := "nav-item")(
          Anchors.renderNavLink(text, location, "nav-link jvm-item Home active-item")(location2Msg)
        )
}
