package com.phauer.recruitingapp.applicationView

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.component.page.Viewport
import com.vaadin.flow.router.RouterLayout

@Push
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
class MainLayout : Div(), RouterLayout
