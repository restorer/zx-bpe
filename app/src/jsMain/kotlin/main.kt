import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.graphics.Renderer
import com.eightsines.bpe.middlware.BpeEngine
import com.eightsines.bpe.middlware.PaintingController
import com.eightsines.bpe.middlware.SelectionController
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.util.ElapsedTimeProviderImpl
import com.eightsines.bpe.util.LoggerImpl
import com.eightsines.bpe.util.UidFactoryImpl
import com.eightsines.bpe.view.BrowserEngine
import com.eightsines.bpe.view.BrowserRenderer
import com.eightsines.bpe.view.BrowserView
import kotlinx.browser.window
import org.w3c.dom.Window

class BpeComponent(private val window: Window) {
    private val document by lazy { window.document }
    private val logger by lazy { LoggerImpl() }
    private val painter by lazy { Painter() }
    private val renderer by lazy { Renderer() }
    private val uidFactory by lazy { UidFactoryImpl() }
    private val elapsedTimeProvider by lazy { ElapsedTimeProviderImpl() }
    private val graphicsEngine by lazy { GraphicsEngine(logger = logger, painter = painter, renderer = renderer) }
    private val selectionController by lazy { SelectionController(graphicsEngine) }

    private val paintingController by lazy {
        PaintingController(graphicsEngine = graphicsEngine, selectionController = selectionController)
    }

    private val bpeEngine by lazy {
        BpeEngine(
            logger = logger,
            uidFactory = uidFactory,
            graphicsEngine = graphicsEngine,
            selectionController = selectionController,
            paintingController = paintingController,
        )
    }

    private val browserRenderer by lazy { BrowserRenderer(elapsedTimeProvider) }
    private val uiEngine by lazy { UiEngine(logger = logger, bpeEngine = bpeEngine) }

    val browserEngine by lazy { BrowserEngine(logger = logger, window = window, document = document, uiEngine = uiEngine) }
    val browserView by lazy { BrowserView(document = document, renderer = browserRenderer) }
}

fun ready(bpeComponent: BpeComponent) {
    val browserEngine = bpeComponent.browserEngine
    val browserView = bpeComponent.browserView

    browserEngine.onUpdate = browserView::render
    browserView.onAction = browserEngine::execute

    browserEngine.ready()
}

fun main() {
    window.onload = { ready(BpeComponent(window)) }
}
