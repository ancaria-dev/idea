package dev.ancaria.idea

import com.intellij.openapi.util.IconLoader

/**
 * Three marks, one family.
 *
 * The ring and the gold are the game's; what sits inside it says which of the
 * three this is. A gutter icon is 12x12 and the wizard's is 16x16, because the
 * platform draws each at its natural size and a scaled-up ring is a blurry one.
 */
object SacredIcons {

    /** The plugin itself: the New Project entry, the run configuration, balloons. */
    @JvmField
    val Sacred = IconLoader.getIcon("/icons/sacred.svg", SacredIcons::class.java)

    /** Beside the class the mod starts in. */
    @JvmField
    val Entrypoint = IconLoader.getIcon("/icons/entrypoint.svg", SacredIcons::class.java)

    /** Beside a method that listens to something the game did. */
    @JvmField
    val Event = IconLoader.getIcon("/icons/event.svg", SacredIcons::class.java)
}
