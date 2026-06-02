package com.github.xepozz.ide.introspector.core.interaction

import java.awt.Component

object WidgetInteractorRegistry {
    private val interactors: List<WidgetInteractor> = listOf(
        TreeInteractor,
        ListInteractor,
        TableInteractor,
        TabbedPaneInteractor,
        ComboBoxInteractor,
    )

    fun forComponent(component: Component): WidgetInteractor? =
        interactors.firstOrNull { it.supports(component) }
}
