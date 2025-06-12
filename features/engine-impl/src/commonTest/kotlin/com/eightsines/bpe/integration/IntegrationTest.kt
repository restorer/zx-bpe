package com.eightsines.bpe.integration

import com.eightsines.bpe.bag.PackableStringBag
import com.eightsines.bpe.bag.UnpackableStringBag
import com.eightsines.bpe.exporters.ScrExporter
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.graphics.Renderer
import com.eightsines.bpe.presentation.BpeEngine
import com.eightsines.bpe.presentation.PaintingController
import com.eightsines.bpe.presentation.SelectionController
import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.presentation.UiEngineImpl
import com.eightsines.bpe.testing.TestLogger
import com.eightsines.bpe.testing.TestUidFactory
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegrationTest {
    @Test
    fun shouldLoadAndSaveAndHistoryForNutcrackV1() = checkLoadAndSaveAndHistory(DataV1.NUTCRACK)

    @Test
    fun shouldLoadAndSaveAndHistoryForBochkasV2() = checkLoadAndSaveAndHistory(DataV2.BOCHKAS)

    fun checkLoadAndSaveAndHistory(bagData: String) = performTest(
        arrange = {
            val logger = TestLogger()
            val uidFactory = TestUidFactory()

            val painter = Painter()
            val renderer = Renderer()
            val graphicsEngine = GraphicsEngine(logger = logger, painter = painter, renderer = renderer)
            val selectionController = SelectionController(graphicsEngine)
            val paintingController = PaintingController(graphicsEngine = graphicsEngine, selectionController = selectionController)
            val scrExporter = ScrExporter()

            val bpeEngine = BpeEngine(
                logger = logger,
                uidFactory = uidFactory,
                graphicsEngine = graphicsEngine,
                selectionController = selectionController,
                paintingController = paintingController,
                scrExporter = scrExporter,
            )

            UiEngineImpl(logger = logger, bpeEngine = bpeEngine)
        },
        act = { uiEngine ->
            UnpackableStringBag(bagData).getStuff(uiEngine.selfUnpacker())

            val originalSteps = uiEngine.state.historySteps
            var undoSteps = 0
            var redoSteps = 0

            while (uiEngine.state.toolboxUndo.isInteractable) {
                uiEngine.execute(UiAction.ToolboxUndoClick)
                ++undoSteps
            }

            while (uiEngine.state.toolboxRedo.isInteractable) {
                uiEngine.execute(UiAction.ToolboxRedoClick)
                ++redoSteps
            }

            val reSavedBagData = PackableStringBag()
                .also { it.put(uiEngine.selfPacker(), uiEngine) }
                .toString()

            UnpackableStringBag(reSavedBagData).getStuff(uiEngine.selfUnpacker())

            var reSavedUndoSteps = 0
            var reSavedRedoSteps = 0

            while (uiEngine.state.toolboxUndo.isInteractable) {
                uiEngine.execute(UiAction.ToolboxUndoClick)
                ++reSavedUndoSteps
            }

            while (uiEngine.state.toolboxRedo.isInteractable) {
                uiEngine.execute(UiAction.ToolboxRedoClick)
                ++reSavedRedoSteps
            }

            CheckResult(
                originalSteps = originalSteps,
                undoSteps = undoSteps,
                redoSteps = redoSteps,
                reSavedUndoSteps = reSavedUndoSteps,
                reSavedRedoSteps = reSavedRedoSteps,
            )
        },
        assert = {
            assertTrue(it.originalSteps > 0)
            assertEquals(it.originalSteps, it.undoSteps)
            assertEquals(it.originalSteps, it.redoSteps)
            assertEquals(it.originalSteps, it.reSavedUndoSteps)
            assertEquals(it.originalSteps, it.reSavedRedoSteps)
        },
    )

    private data class CheckResult(
        val originalSteps: Int,
        val undoSteps: Int,
        val redoSteps: Int,
        val reSavedUndoSteps: Int,
        val reSavedRedoSteps: Int,
    )
}
