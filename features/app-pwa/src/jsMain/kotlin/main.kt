import com.eightsines.bpe.exporters.ScrExporter
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.graphics.Renderer
import com.eightsines.bpe.presentation.BpeEngine
import com.eightsines.bpe.presentation.PaintingController
import com.eightsines.bpe.presentation.SelectionController
import com.eightsines.bpe.presentation.UiEngineImpl
import com.eightsines.bpe.util.ResourceManager
import com.eightsines.bpe.util.ElapsedTimeProviderImpl
import com.eightsines.bpe.util.LoggerImpl
import com.eightsines.bpe.util.UidFactoryImpl
import com.eightsines.bpe.view.BrowserEngine
import com.eightsines.bpe.view.BrowserRenderer
import com.eightsines.bpe.view.BrowserView
import com.eightsines.bpe.view.BrowserSheetController
import com.eightsines.bpe.view.UnhandledErrorView
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val scrExporter by lazy { ScrExporter() }

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
            scrExporter = scrExporter,
        )
    }

    private val browserRenderer by lazy { BrowserRenderer(elapsedTimeProvider) }
    private val uiEngine by lazy { UiEngineImpl(logger = logger, bpeEngine = bpeEngine) }
    private val resourceManager by lazy { ResourceManager() }
    private val browserSheetController by lazy { BrowserSheetController() }

    val mainDispatcher by lazy { Dispatchers.Main }

    val browserEngine by lazy {
        BrowserEngine(
            logger = logger,
            window = window,
            document = document,
            uiEngine = uiEngine,
            sheetController = browserSheetController,
            mainDispatcher = mainDispatcher,
        )
    }

    val browserView by lazy {
        BrowserView(
            document = document,
            elapsedTimeProvider = elapsedTimeProvider,
            renderer = browserRenderer,
            resourceManager = resourceManager,
            sheetController = browserSheetController,
        )
    }

    val unhandledErrorView by lazy { UnhandledErrorView(document = document, logger = logger) }
}

fun refreshLoop(component: BpeComponent) {
    component.browserView.refresh()
    window.requestAnimationFrame { refreshLoop(component) }
}

fun ready(component: BpeComponent) {
    val browserEngine = component.browserEngine
    val browserView = component.browserView
    val unhandledErrorView = component.unhandledErrorView

    CoroutineScope(
        SupervisorJob() +
                component.mainDispatcher +
                CoroutineExceptionHandler { _, t -> unhandledErrorView.show(t) }
    ).apply {
        launch { browserView.actionFlow.collect(browserEngine::execute) }
        launch { browserEngine.browserStateFlow.collect(browserView::render) }
    }

    window.addEventListener("resize", { browserView.reposition() })
    refreshLoop(component)
}

fun main() {
    window.onload = { ready(BpeComponent(window)) }
}
