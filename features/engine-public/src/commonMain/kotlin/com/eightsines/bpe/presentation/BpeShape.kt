package com.eightsines.bpe.presentation

enum class BpeShape(val value: Int) {
    Point(1),
    Line(2),
    FillBox(3),
    StrokeBox(4),
    FillEllipse(5),
    StrokeEllipse(6);

    companion object {
        fun of(value: Int) = when (value) {
            Point.value -> Point
            Line.value -> Line
            FillBox.value -> FillBox
            StrokeBox.value -> StrokeBox
            FillEllipse.value -> FillEllipse
            StrokeEllipse.value -> StrokeEllipse
            else -> throw IllegalArgumentException("Unknown enum value=$value for BpeShape")
        }
    }
}
