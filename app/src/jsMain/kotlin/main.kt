import com.eightsines.bpe.engine.BpeEngine
import com.eightsines.bpe.engine.GraphicsEngine
import com.eightsines.bpe.engine.Renderer
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.presentation.UiRenderer
import com.eightsines.bpe.presentation.UiView
import com.eightsines.bpe.util.ElapsedTimeProviderImpl
import com.eightsines.bpe.util.UidFactoryImpl
import kotlinx.browser.window
import org.w3c.dom.Document

class BpeComponent(private val document: Document) {
    private val painter by lazy { Painter() }
    private val renderer by lazy { Renderer() }
    private val uidFactory by lazy { UidFactoryImpl() }
    private val elapsedTimeProvider by lazy { ElapsedTimeProviderImpl() }
    private val graphicsEngine by lazy { GraphicsEngine(painter, renderer) }
    private val bpeEngine by lazy { BpeEngine(uidFactory, graphicsEngine) }
    private val uiRenderer by lazy { UiRenderer(elapsedTimeProvider) }

    val uiEngine by lazy { UiEngine(bpeEngine) }
    val uiView by lazy { UiView(document, uiRenderer) }
}

fun ready(bpeComponent: BpeComponent) {
    val uiEngine = bpeComponent.uiEngine
    val uiView = bpeComponent.uiView

    bpeComponent.uiView.render(uiEngine.state)

    uiView.onAction = {
        uiEngine.execute(it)
        uiView.render(uiEngine.state)
    }
}

fun main() {
    window.onload = { ready(BpeComponent(window.document)) }
}
