import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.jsoup.Jsoup
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.time.LocalTime
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Timer

class NewsPlugin : AnAction(), ToolWindowFactory {
    // This is a NewsPLugin class, which shows news from IZ.ru in IJ Idea tool window

    private var toolWindow: ToolWindow? = null
    private var newsLabel: JLabel? = null
    private var news = mutableListOf<String>()
    private val newsTime = mutableListOf<Long>()

    override fun actionPerformed(e: AnActionEvent) {
        // do nothing
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // tool window setup
        this.toolWindow = toolWindow

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(createPanel(), "", false)
        toolWindow.contentManager.addContent(content)

        // Start a timer to update the labels every 5 seconds
        Timer(5000) {
            updateLabels()
        }.start()
    }

    private fun createPanel(): JPanel {
        // creating panel
        val panel = JPanel(BorderLayout())
        val infoPanel = JPanel(GridLayout(1, 1))
        (infoPanel.layout as GridLayout).vgap = 0

        val newsPanel = JPanel()
        newsLabel = JLabel("Нет новых новостей")
        newsPanel.add(newsLabel)
        infoPanel.add(newsPanel)
        panel.add(infoPanel, BorderLayout.CENTER)
        panel.preferredSize = Dimension(400, 300)

        return panel
    }

    private fun getNewsList() {
        // update news list
        val url = "https://iz.ru/news"
        val doc = Jsoup.connect(url).get()
        val jspPaneDiv = doc.select("div.short-last-news")
        val liList = jspPaneDiv.select("li")


        for (li in liList.reversed()) {
            var text = li.text()
            val time = text.substring(text.length - 5, text.length)
            var seconds = time.substring(0, 2).toLong() * 3600
            seconds += time.substring(3, 5).toLong() * 60

            text = time + ": " + text.substring(0, text.length - 5)
            if (text !in this.news) {
                this.news.add(text)
                this.newsTime.add(seconds)
            }
        }
    }

    private fun updateLabels() {
        // update label content
        getNewsList()
        val newsText = StringBuilder()
        val time = LocalTime.now().toSecondOfDay()
        for (i in this.news.size - 1 downTo this.news.size - 10) {
            if (time - this.newsTime[i] > 600 || this.newsTime[i] + 600 >= 24 * 3_600) {
                newsText.append(this.news[i]).append("<br>")
            } else {
                newsText.append("•${this.news[i]}").append("<br>")
            }
        }
        newsLabel?.text = "<html>$newsText</html>"
    }

}
