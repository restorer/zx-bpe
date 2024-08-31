import com.eightsines.bpe.engine.BpeEngine
import com.eightsines.bpe.engine.GraphicsEngine
import com.eightsines.bpe.engine.Renderer
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.presentation.UiRenderer
import com.eightsines.bpe.presentation.UiView
import com.eightsines.bpe.util.UidFactoryImpl
import kotlinx.browser.window

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
    val uiView = UiView(window.document, UiRenderer())

    uiView.render(uiEngine.state)

    uiView.onAction = {
        uiEngine.execute(it)
        uiView.render(uiEngine.state)
    }
}

fun main() {
    window.onload = { onBpeLoaded() }
}
