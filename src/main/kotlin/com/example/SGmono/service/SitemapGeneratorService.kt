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
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@Service
class SitemapGeneratorService(
    private val env: Environment
) {

    private fun getSitemapsFromRobots(page: String): MutableList<String>? =
        runCatching {
            val listOfSitemaps = mutableListOf<String>()
            val listOfLines: List<String> =
                Jsoup
                    .parse(page)
                    .getElementsByTag("pre")
                    .get(0)
                    .text()
                    .split("\n")

            for (line in listOfLines) {
                val nameValuePair = line.split(": ")
                if (nameValuePair[0].lowercase(Locale.getDefault()) == "sitemap")
                    listOfSitemaps.add(nameValuePair[1])

            }
            listOfSitemaps
        }.onFailure {
        }.getOrNull()


    private fun generateMapFromSitemap(driver: WebDriver, sitemaps: List<String>, site: String): Set<String> {
        val map: MutableSet<String> = HashSet()

        class Local {
            tailrec fun recursive(driver: WebDriver, sitemaps: List<String>) {
                val hiddenSitemaps: MutableList<String> = ArrayList()
                for (sitemap in sitemaps) {
                    val xmlText = getPageWithTimeout(driver, sitemap)
                    val doc: Document = Jsoup.parse(xmlText)


                    val elements: Elements = doc.getElementsContainingOwnText(site)
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

    private fun generateMapFromSitemap1(driver: WebDriver, sitemaps: List<String>, site: String): Set<String> {
        val map = HashSet<String>()

        class Local {
            tailrec fun generateSitemapRecursive(driver: WebDriver, sitemaps: List<String>) {
                val hiddenSitemaps: MutableList<String> = ArrayList()
                for (sitemap in sitemaps) {
                    val elements: Elements = Jsoup
                        .parse(
                            getPageWithTimeout(driver, sitemap)
                        ).getElementsContainingOwnText(site)

                    for (element in elements) {
                        val link: String = element.text()

                        if(link.endsWith(".xml")) hiddenSitemaps.add(link)
                        else if((!
                            link
                                .subSequence(
                                    link.lastIndexOf("."),
                                    link.length
                                )
                                .matches(Regex("^\\.[^.]+\$"))

                        )) map.add(link)
                    }
                }
                if (hiddenSitemaps.size != 0) generateSitemapRecursive(driver, hiddenSitemaps)
            }
        }

        val loc = Local()
        loc.generateSitemapRecursive(driver, sitemaps)
        return map
    }

    private fun generateMapRecursive(
        driver: WebDriver,
        baseURL: String,
        map: HashSet<String>,
        site: String
    ): HashSet<String>? =
        runCatching {
            val js: JavascriptExecutor = driver as JavascriptExecutor
            driver.get(baseURL)
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);")

            val uniqueLinks =
                Jsoup
                    .parse(driver.pageSource)
                    .getElementsByTag("a")
                    .asSequence()
                    .map { it.attr("href") }
                    .filter { it.startsWith("/") }
                    .map { "${site.removePrefix("/")}$it" }
                    .distinct()
                    .toHashSet()

            for (link in uniqueLinks) {
                runCatching {
                    if (!map.contains(link)) {
                        map.add(link)
                        println("processing: " + link)
                        val timeout = ThreadLocalRandom.current().nextInt(CRAWL_DELAY, CRAWL_DELAY * 2 + 1) * 1000
                        Thread.sleep(timeout.toLong())
                        runCatching {
                            generateMapRecursive(driver, link, map, site)
                        }
                    }
                }
            }
            map
        }.onFailure {
        }.getOrNull()



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
            sitemaps = getSitemapsFromRobots(page)
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

        private fun getPageWithTimeout(driver: WebDriver, path: String): String {
            driver.get(path)
            val timeout = ThreadLocalRandom.current().nextInt(CRAWL_DELAY, CRAWL_DELAY * 2 + 1) * 1000
            Thread.sleep(timeout.toLong())
            return driver.pageSource
        }
    }

}
