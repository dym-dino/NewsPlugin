import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Timer
import java.io.IOException
import java.net.URL
import java.net.URLConnection

class NewsPlugin : AnAction(), ToolWindowFactory {

    private var toolWindow: ToolWindow? = null
    private var memoryLabel: JLabel? = null
    private var cpuLabel: JLabel? = null
    private var wifiLabel: JLabel? = null

    override fun actionPerformed(e: AnActionEvent) {
        // do nothing
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        this.toolWindow = toolWindow

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(createPanel(), "", false)
        toolWindow.contentManager.addContent(content)

        // Start a timer to update the labels every second
        Timer(1000) {
            updateLabels(getMemoryUsage(), getCpuUsage(), measureInternetSpeed())
        }.start()
    }

    private fun createPanel(): JPanel {
        val panel = JPanel(BorderLayout())

        val infoPanel = JPanel(GridLayout(3, 1))
        (infoPanel.layout as GridLayout).vgap = 0 // Устанавливаем вертикальное расстояние между метками на минимум

        val memoryPanel = JPanel()
        memoryLabel = JLabel("Memory usage: 0 MB")
        memoryPanel.add(memoryLabel)

        val cpuPanel = JPanel()
        cpuLabel = JLabel("CPU usage: 0%")
        cpuPanel.add(cpuLabel)

        val wiFiPanel = JPanel()
        wifiLabel = JLabel("Internet speed: 0 Mbps")
        wiFiPanel.add(wifiLabel)

        infoPanel.add(memoryPanel)
        infoPanel.add(cpuPanel)
        infoPanel.add(wiFiPanel)

        panel.add(infoPanel, BorderLayout.CENTER)

        panel.preferredSize = Dimension(300, 50)

        return panel
    }


    private fun getCpuUsage(): Double {
        val mxBean =
            java.lang.management.ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
        val cpuUsage = mxBean.processCpuLoad
        return cpuUsage * 100
    }

    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        return (totalMemory - freeMemory) / (1024 * 1024)
    }

    private fun measureInternetSpeed(): Double {
        try {
            val url = URL("http://speedtest.tele2.net/1MB.zip") // URL для загрузки файла для измерения скорости
            val connection: URLConnection = url.openConnection()
            val startTime = System.currentTimeMillis()
            connection.getInputStream().readAllBytes() // Загрузка файла для измерения времени
            val endTime = System.currentTimeMillis()
            val downloadTime = (endTime - startTime) / 1000.0 // Время загрузки файла в секундах
            val fileSizeInMB = 1 // Размер файла для загрузки в мегабайтах
            return fileSizeInMB / downloadTime // Вычисление скорости интернета в мегабайтах в секунду
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0.0
    }

    private fun updateLabels(memoryUsage: Long, cpuUsage: Double, internetSpeed: Double) {
        when (memoryUsage){
            in 1..1024 -> memoryLabel?.text = "Memory usage: ${memoryUsage} bytes"
            in 1025..1024*1024 ->  memoryLabel?.text = "Memory usage: ${memoryUsage / 1024} Kb"
            else -> memoryLabel?.text = "Memory usage: ${memoryUsage / 1024 / 1024} MB"
        }

        cpuLabel?.text = "CPU usage: ${String.format("%.2f", cpuUsage)}%"
        wifiLabel?.text = "Internet speed: ${String.format("%.2f", internetSpeed)} Mbps"
    }

}
