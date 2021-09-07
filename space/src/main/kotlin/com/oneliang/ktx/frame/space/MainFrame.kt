package com.oneliang.ktx.frame.space

import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel


class MainFrame {
    fun show(shapeList: List<Shape>) {
        val WIDTH = 1050
        val HEIGHT = 750
        val frame = JFrame()
        val screenDimension: Dimension = Toolkit.getDefaultToolkit().screenSize
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(WIDTH, HEIGHT)
        frame.setLocation((screenDimension.width - WIDTH) / 2, (screenDimension.height - HEIGHT) / 2)
        frame.title = "toolkit"
        val borderLayout = BorderLayout()
        val container: Container = frame.contentPane
        container.layout = borderLayout
        frame.isVisible = true
        val panel = JPanel()
        val gridLayout = GridLayout(1, 1)
        panel.layout = gridLayout
        container.add(panel)
        panel.add(PaintPanel(shapeList, 100))
    }
}