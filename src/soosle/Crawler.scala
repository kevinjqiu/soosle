package soosle

import org.xml.sax.InputSource
import xml.{NodeSeq, Node}
import tagsoup.TagSoupFactoryAdapter
import java.net.{URI, URL}
import collection.mutable.HashSet
import java.io.FileNotFoundException

class Crawler(val dbname:String, val pages:Set[String], val depth:Int) {

  def addToIndex(url:String, rootNode:Node) {
    println("Adding the page '%s' to index".format(url))
  }

  def addLinkRef(from:String, to:String, linkText:String) {
    println("Adding link reference from '%s' to '%s' with link text '%s'".format(from, to, linkText))
    
  }

  def commit() {
    println("Committing changes")
  }

  def isIndexed(uri:String):Boolean = false

  def crawl() {
    for (i <- 0 until depth) {
      println("Depth=" + depth)
      _crawl(pages.toList, new HashSet[String]())  
    }
  }

  def _crawl(pages:List[String], seenPages:HashSet[String]) {
    println("Unprocessed pages=" + pages.size)
    if (pages.size == 0) return

    val currentPage = pages.head
    seenPages += currentPage
    var newPages = new HashSet[String]()

    // Add the current page to the index
    try {
      val currentPageUri = new URI(currentPage)
      val rootNode = getRootNodeOfPage(currentPageUri)
      addToIndex(currentPage, rootNode)
      // Process the links appear on the current page
      val links = rootNode \\ "a"

      links.filter(link=>link.attribute("href") != None).foreach(link=>{
        val rel:String = (link \ "@href").text
        val newPageUri = currentPageUri.resolve(rel).toString
        if (!isIndexed(newPageUri))
          newPages += newPageUri
        addLinkRef(currentPage, newPageUri, link.text)
      })
      commit()

      // XXX: is there a more succinct way?
      newPages ++= pages
      newPages --= seenPages
    } catch {
      case e:FileNotFoundException => println(e.getMessage)
    }
    _crawl(newPages.toList, seenPages)
  }

  private def getRootNodeOfPage(uri:URI):Node = {
    val p = new TagSoupFactoryAdapter
    val conn = uri.toURL.openConnection
    p.loadXML(new InputSource(conn.getInputStream))
  }

}