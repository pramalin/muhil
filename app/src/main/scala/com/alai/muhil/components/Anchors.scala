package com.alai.muhil.components

import tyrian.*
import tyrian.Html.*

import com.alai.muhil.*
import com.alai.muhil.core.*

object Anchors {
  
    def renderSimpleNavLink(text: String, location: String, cssClass: String = "") =
        renderNavLink(text, location, cssClass)(Router.ChangeLocation(_))

    def renderNavLink(text: String, location: String, cssClass: String = "")(location2Msg: String => App.Msg) =
        li(`class` := "nav-item")(
            a(
                href := location,
                `class` := cssClass,
                onEvent(
                    "click",
                    e => {
                        e.preventDefault() // native JS - prevent reloading the page
                        location2Msg(location)
                    }
                )
            )(text)
        )
  
}
