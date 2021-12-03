package com.example.SGmono.service

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.net.URI
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@Service
class SitemapGeneratorService(
    private val env: Environment
) {
    private fun analyzeRobotsTxt(page: String): Map<String, MutableList<String>> {
        val robotsTXT: MutableMap<String, MutableList<String>> = HashMap()
        val doc: Document = Jsoup.parse(page)
        try {
            val text: Element = doc.getElementsByTag("pre").get(0)
            val list: List<String> = text.text().split("\n").toList()

            for (line in list) {
                val two = line.split(": ".toRegex()).toTypedArray()
                if (two.size == 2) {
                    var values = robotsTXT[two[0]]
                    if (values == null) values = ArrayList()
                    values.add(two[1])
                    robotsTXT[two[0].lowercase(Locale.ROOT)] = values
                }
            }
            robotsTXT.forEach { (key: String, value: List<String>) ->
                println(
                    "KEY: $key VALUE: $value"
                )
            }

            val crawlVals: List<String>? = robotsTXT["crawl-delay"]
            if (crawlVals != null) {
                for (s in crawlVals) {
                    if (s.toInt() > CRAWL_DELAY) {
                        CRAWL_DELAY = s.toInt()
                    }
                }
            }
        } catch (e: Exception) {
            println()
        }
        return robotsTXT
    }

    private fun generateMapFromSitemap(driver: WebDriver, sitemaps: List<String>, site: String): Set<String> {
        val map: MutableSet<String> = HashSet()

        class Local {
            tailrec fun recursive(driver: WebDriver, sitemaps: List<String>) {
                val hiddenSitemaps: MutableList<String> = ArrayList()
                for (sitemap in sitemaps) {
                    val xmlText = getPageWithTimeout(driver, sitemap)
                    val doc: Document = Jsoup.parse(xmlText)


                    val elements: Elements = doc.getElementsContainingOwnText(site!!)
                    for (e in elements) {
                        val text: String = e.text()
                        val textLength = text.length
                        val indexOfXML = text.indexOf(".xml")

                        // если .xml находиться в конце urlа, то это сайтмап
                        if (textLength - 4 == indexOfXML) {
                            println("SITEmap $text")
                            hiddenSitemaps.add(text)
                        } else if (indexOfXML == -1) {
                            println("URL $text")
                            map.add(text)
                        }
                    }
                }
                // Если хранилище сайтмап, на которые указывали сайтмапы, не пусто, то проходимся и по ним.
                if (hiddenSitemaps.size != 0) recursive(driver, hiddenSitemaps)
            }
        }

        val loc = Local()
        loc.recursive(driver, sitemaps)
        return map
    }

    private fun generateMapRecursive(driver: WebDriver, baseURL: String, map: MutableSet<String>, site: String): Set<String> {
        val js: JavascriptExecutor = driver as JavascriptExecutor
        driver.get(baseURL)

        js.executeScript("window.scrollTo(0, document.body.scrollHeight);")

        val html: String = driver.pageSource
        val doc: Document = Jsoup.parse(html)
        val links: List<Element> = doc.getElementsByTag("a")
        try {
            // Выбираем только уникальные ссылки, нам же не нужно обходить одну и ту же страницу несколько раз, так?
            val uniqueLinks = links
                .stream()
                .map { it.attr("href") }
                .filter { it.startsWith("/") }
                .map { link: Any ->
                    (site + link).replace(
                        "://",
                        "321TEMP123"
                    ).replace("//", "/").replace("321TEMP123", "://")
                }
                .collect(Collectors.toSet())

            for (link in uniqueLinks) {
                try {
                    if (!map.contains(link)) {
                        URL_COUNTER++
                        map.add(link)
                        println("№" + URL_COUNTER + " processing: " + link)
                        val timeout = ThreadLocalRandom.current().nextInt(CRAWL_DELAY, CRAWL_DELAY * 2 + 1) * 1000
                        Thread.sleep(timeout.toLong())
                        try {
                            generateMapRecursive(driver, link, map, site)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    println("Invalid URL: $link")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println("Some shitty urls out there")
            e.printStackTrace()
        }
        URL_COUNTER = 0
        return map
    }

    private fun createDriver(): WebDriver {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe")
        val options = ChromeOptions()
        options.addArguments(
            "--headless",
            "--disable-gpu",
            "--window-size=1920,1200",
            "--ignore-certificate-errors",
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36"
        )
        options.setBinary(env.getProperty("browser.path"))
        return ChromeDriver(options)
    }

    fun getMap(site: String): Set<String>? {
        val driver = createDriver()
        var sitemaps: MutableList<String>? = null
        var map: Set<String>? = null
        try {
            val page = getPageWithTimeout(driver, site + "robots.txt")
            sitemaps = analyzeRobotsTxt(page)["sitemap"]
        } catch (e: Exception) {

        } finally {
            map = sitemaps?.let {
                generateMapFromSitemap(driver, it, site)
            } ?: generateMapRecursive(driver, site, HashSet(), site)
            driver.close()
        }
        return map
    }

    companion object {
        private var CRAWL_DELAY = 15
        private var URL_COUNTER = 0

        private fun getPageWithTimeout(driver: WebDriver, path: String): String {
            driver.get(path)
            val timeout = ThreadLocalRandom.current().nextInt(CRAWL_DELAY, CRAWL_DELAY * 2 + 1) * 1000
            Thread.sleep(timeout.toLong())
            return driver.pageSource
        }
    }

}
