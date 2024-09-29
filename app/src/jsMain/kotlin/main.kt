import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.graphics.Renderer
import com.eightsines.bpe.middlware.BpeEngine
import com.eightsines.bpe.presentation.BrowserRenderer
import com.eightsines.bpe.presentation.BrowserView
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.util.ElapsedTimeProviderImpl
import com.eightsines.bpe.util.LoggerImpl
import com.eightsines.bpe.util.UidFactoryImpl
import kotlinx.browser.window
import org.w3c.dom.Document

class BpeComponent(private val document: Document) {
    private val logger by lazy { LoggerImpl() }
    private val painter by lazy { Painter() }
    private val renderer by lazy { Renderer() }
    private val uidFactory by lazy { UidFactoryImpl() }
    private val elapsedTimeProvider by lazy { ElapsedTimeProviderImpl() }
    private val graphicsEngine by lazy { GraphicsEngine(logger = logger, painter = painter, renderer = renderer) }
    private val bpeEngine by lazy { BpeEngine(logger = logger, uidFactory = uidFactory, graphicsEngine = graphicsEngine) }
    private val browserRenderer by lazy { BrowserRenderer(elapsedTimeProvider) }

    val uiEngine by lazy { UiEngine(logger = logger, bpeEngine = bpeEngine) }
    val browserView by lazy { BrowserView(document = document, renderer = browserRenderer) }
}

fun ready(bpeComponent: BpeComponent) {
    val uiEngine = bpeComponent.uiEngine
    val uiView = bpeComponent.browserView

    bpeComponent.browserView.render(uiEngine.state)

    uiView.onAction = {
        uiEngine.execute(it)
        uiView.render(uiEngine.state)
    }
}

fun main() {
    window.onload = { ready(BpeComponent(window.document)) }
}
