package com.eightsines.bpe.engine.layer

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.engine.canvas.MutableCanvas
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

interface MutableLayer : Layer

private fun MutableLayer.putInTheBagBase(bag: PackableBag) {
    bag.put(isVisible)
    bag.put(isLocked)
}

data class MutableBackgroundLayer(
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var color: SciiColor,
    override var bright: SciiLight,
) : BackgroundLayer, MutableLayer {
    override val bagStuffVersion = 1

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

        bag.put(color.value)
        bag.put(bright.value)
    }

    companion object : BagStuff.Unpacker<MutableBackgroundLayer> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableBackgroundLayer {
            if (version != 1) {
                throw BagUnpackException("Unsupported version=$version for MutableBackgroundLayer")
            }

            return MutableBackgroundLayer(
                isVisible = bag.getBoolean(),
                isLocked = bag.getBoolean(),
                color = SciiColor(bag.getInt()),
                bright = SciiLight(bag.getInt()),
            )
        }
    }
}

data class MutableCanvasLayer<T : Cell>(
    override val uid: LayerUid,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override val canvas: MutableCanvas<T>,
) : CanvasLayer<T>, MutableLayer {
    override val bagStuffVersion = 1

    override fun copyMutable() = MutableCanvasLayer(
        uid = uid,
        isVisible = isVisible,
        isLocked = isLocked,
        canvas = canvas.copyMutable(),
    )

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

        bag.put(uid.value)
        bag.put(canvas)
    }

    companion object : BagStuff.Unpacker<MutableCanvasLayer<*>> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableCanvasLayer<*> {
            if (version != 1) {
                throw BagUnpackException("Unsupported version=$version for MutableBackgroundLayer")
            }

            return MutableCanvasLayer(
                uid = LayerUid(bag.getString()),
                isVisible = bag.getBoolean(),
                isLocked = bag.getBoolean(),
                canvas = bag.getStuff(MutableCanvas.Companion),
            )
        }
    }
}
