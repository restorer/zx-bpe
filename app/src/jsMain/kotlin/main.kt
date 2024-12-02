import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.graphics.Renderer
import com.eightsines.bpe.middlware.BpeEngine
import com.eightsines.bpe.middlware.PaintingController
import com.eightsines.bpe.middlware.SelectionController
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.util.ElapsedTimeProviderImpl
import com.eightsines.bpe.util.LoggerImpl
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UidFactoryImpl
import com.eightsines.bpe.view.BrowserAction
import com.eightsines.bpe.view.BrowserRenderer
import com.eightsines.bpe.view.BrowserView
import kotlinx.browser.window
import org.w3c.dom.Document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

class BpeComponent(private val document: Document) {
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

    val uiEngine by lazy { UiEngine(logger = logger, bpeEngine = bpeEngine) }
    val browserView by lazy { BrowserView(document = document, renderer = browserRenderer) }
}

fun ready(bpeComponent: BpeComponent) {
    val uiEngine = bpeComponent.uiEngine
    val browserView = bpeComponent.browserView

    browserView.render(uiEngine.state)

    browserView.onAction = {
        when (it) {
            is BrowserAction.Ui -> {
                uiEngine.execute(it.action)
                browserView.render(uiEngine.state)
            }

            is BrowserAction.Load -> {
            }

            is BrowserAction.Save -> {
                val bagData = PackableStringBag()
                    .also { uiEngine.putInTheBagSelf(it) }
                    .toString()

                (window.document.createElement("a") as HTMLAnchorElement).apply {
                    download = "painting.bpe"
                    href = URL.createObjectURL(Blob(arrayOf(bagData), BlobPropertyBag("text/plain")))
                    click()
                }
            }
        }
    }
}

fun main() {
    window.onload = { ready(BpeComponent(window.document)) }
}
