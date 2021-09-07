package com.oneliang.ktx.frame.space

import java.awt.Graphics
import javax.swing.JPanel

class PaintPanel(private val shapeList: List<Shape>, private val zoom: Int = 1) : JPanel() {

    override fun paintComponent(graphics: Graphics?) {
        super.paintComponent(graphics)
        if (graphics == null) {
            return
        }
        for (shape in this.shapeList) {
            for (index in shape.points.indices) {
                val start = index
                var end = index + 1
                if (start == shape.points.size - 1) {
                    end = 0
                }
                val p1 = shape.points[start]//current point
                val p2 = shape.points[end]//next point
                val x1 = (p1.x * this.zoom).toInt()
                val y1 = (p1.y * this.zoom).toInt()
                val x2 = (p2.x * this.zoom).toInt()
                val y2 = (p2.y * this.zoom).toInt()
                graphics.drawLine(x1, y1, x2, y2)
            }
        }
    }
}