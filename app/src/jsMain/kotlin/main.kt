import com.eightsines.bpe.engine.BpeEngine
import com.eightsines.bpe.engine.GraphicsEngine
import com.eightsines.bpe.engine.Renderer
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.presentation.UiView
import com.eightsines.bpe.util.UidFactoryImpl
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.asList

fun onBpeLoaded() {
    val graphicsEngine = GraphicsEngine(
        painter = Painter(),
        renderer = Renderer(),
    )

    val bpeEngine = BpeEngine(
        uidFactory = UidFactoryImpl(),
        graphicsEngine = graphicsEngine,
    )

    val uiEngine = UiEngine(bpeEngine)
    val uiView = UiView(window.document)

    window.document.querySelectorAll(".js-canvas").asList()
        .filterIsInstance<HTMLCanvasElement>()
        .forEach {
            val ctx = it.getContext("2d") as CanvasRenderingContext2D

            ctx.fillStyle = "#0000c0"
            ctx.fillRect(0.0, 0.0, 320.0, 256.0)

            ctx.fillStyle = "#0000ff"
            ctx.fillRect(32.0, 32.0, 256.0, 192.0)
        }

    uiView.render(uiEngine.state)

    uiView.onAction = {
        uiEngine.execute(it)
        uiView.render(uiEngine.state)
    }
}

fun main() {
    window.onload = { onBpeLoaded() }
}
