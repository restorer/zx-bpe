import kotlinx.browser.window
import kotlinx.dom.removeClass
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun onBpeLoaded() {
    window.document.querySelectorAll(".js-drawing-canvas").asList()
        .filterIsInstance<HTMLCanvasElement>()
        .forEach {
            val ctx = it.getContext("2d") as CanvasRenderingContext2D

            ctx.fillStyle = "#0000c0"
            ctx.fillRect(0.0, 0.0, 320.0, 256.0)

            ctx.fillStyle = "#0000ff"
            ctx.fillRect(32.0, 32.0, 256.0, 192.0)
        }

    window.document.querySelectorAll(".js-container").asList()
        .filterIsInstance<HTMLElement>()
        .forEach { it.removeClass("hidden") }
}

fun main() {
    window.onload = { onBpeLoaded() }
}
