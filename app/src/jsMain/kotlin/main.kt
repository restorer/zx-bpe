import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.graphics.Renderer
import com.eightsines.bpe.middlware.BpeEngine
import com.eightsines.bpe.middlware.PaintingController
import com.eightsines.bpe.middlware.SelectionController
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.resources.ResourceManager
import com.eightsines.bpe.util.ElapsedTimeProviderImpl
import com.eightsines.bpe.util.LoggerImpl
import com.eightsines.bpe.util.UidFactoryImpl
import com.eightsines.bpe.view.BrowserEngine
import com.eightsines.bpe.view.BrowserRenderer
import com.eightsines.bpe.view.BrowserView
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.w3c.dom.Window

class BpeComponent(val window: Window) {
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
    private val resourceManager by lazy { ResourceManager() }

    val browserEngine by lazy { BrowserEngine(logger = logger, document = document, uiEngine = uiEngine) }

    val browserView by lazy {
        BrowserView(
            document = document,
            elapsedTimeProvider = elapsedTimeProvider,
            renderer = browserRenderer,
            resourceManager = resourceManager,
        )
    }

    val mainDispatcher by lazy { Dispatchers.Main }
}

var isUnhandledErrorHandlerInitialized = false

fun handleUnhandledError(t: Throwable) {
    console.error(t.stackTraceToString(), t)
    val errorElement = document.querySelector(".js-error") ?: return

    if (!isUnhandledErrorHandlerInitialized) {
        isUnhandledErrorHandlerInitialized = true

        document.querySelector(".js-error-header")?.addEventListener("click", {
            errorElement.classList.add("hidden")
        })
    }

    document.querySelector(".js-error-content")?.textContent = t.stackTraceToString()
    errorElement.classList.remove("hidden")
}

fun refreshLoop(component: BpeComponent) {
    component.browserView.refresh()
    window.requestAnimationFrame { refreshLoop(component) }
}

fun ready(component: BpeComponent) {
    val browserEngine = component.browserEngine
    val browserView = component.browserView

    CoroutineScope(
        SupervisorJob() +
                component.mainDispatcher +
                CoroutineExceptionHandler { _, t -> handleUnhandledError(t) }
    ).launch {
        launch { browserView.actionFlow.collect(browserEngine::execute) }
        launch { browserEngine.browserStateFlow.collect(browserView::render) }
    }

    refreshLoop(component)
}

fun main() {
    window.onload = {
        try {
            ready(BpeComponent(window))
        } catch (t: Throwable) {
            handleUnhandledError(t)
        }
    }
}
