package com.cine.launcher.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.cine.launcher.data.Shelf
import com.cine.launcher.data.Title

/** Builds a titled horizontal shelf of [TitleCard]s. Returns the section view and its first focusable card. */
class ShelfView(
    context: Context,
    shelf: Shelf,
    showSample: Boolean,
    onFocus: (Title) -> Unit,
    onOpen: (Title) -> Unit,
) : Box(context) {
    val cards = ArrayList<View>()

    init {
        orientation = VERTICAL
        val head = Box(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.px(56), 0, Ui.px(56), 0)
        }
        head.addView(context.label(shelf.title, 22, C.onSurface, 600, spacing = 0.01f))
        head.addView(context.spacer(14))
        head.addView(context.label(shelf.subtitle, 12, C.secondary, 600))
        addView(head)
        val row = Row(context)
        if (shelf.items.isEmpty()) {
            row.strip.addView(context.label("Nothing here yet.", 15, C.onVariant, 500).apply {
                pad(0, 40)
            })
        }
        shelf.items.forEach { t ->
            val card = TitleCard(context, t, shelf.portrait, showSample)
            val target = card.focusTarget
            target.addFocusListener { has -> if (has) onFocus(t) }
            target.setOnClickListener { onOpen(t) }
            cards += target
            row.strip.addView(card)
        }
        addView(row, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
}
