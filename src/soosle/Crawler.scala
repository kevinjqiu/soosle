package soosle

import org.xml.sax.InputSource
import xml.{NodeSeq, Node}
import tagsoup.TagSoupFactoryAdapter
import java.net.URI
import collection.mutable.{Set,HashSet}

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
    val linkFoundHandler = (newPages:Set[String], currentPageUri:URI, link:Node) => {
      val rel:String = (link \ "@href").text
      val newPageUri = currentPageUri.resolve(rel).toString
      if (!isIndexed(newPageUri))
        newPages += newPageUri
      addLinkRef(currentPageUri.toString, newPageUri, link.text)
    }

    val errorHandler = (e:Exception) => {
      println(e)
    }

    for (i <- 0 until depth) {
      println("Depth=" + depth)
      _crawl(pages.toList, new HashSet[String](), linkFoundHandler, errorHandler)  
    }
  }

  def _crawl(pages:List[String],
             seenPages:Set[String],
             linkFoundHandler:(Set[String],URI,Node)=>Unit,
             errorHandler:Exception=>Unit) {
    println("Unprocessed pages=" + pages.size)
    if (pages.size == 0) return

    val currentPage = pages.head
    seenPages += currentPage
    var newPages:Set[String] = new HashSet()

    // Add the current page to the index
    try {
      val currentPageUri = new URI(currentPage)
      val rootNode = getRootNodeOfPage(currentPageUri)
      addToIndex(currentPage, rootNode)
      // Process the links appear on the current page
      val links = rootNode \\ "a"

      // XXX: Add the condition @href!=None in the XPath itself? How?
      links.filter(link=>link.attribute("href") != None).foreach(link=>{
        linkFoundHandler(newPages, currentPageUri, link)
      })
      commit()

      /*
        XXX: if in the above line where
        var newPages = new HashSet[String](),
        the following line will cause a compiler error
        but if I indicate the type of newPages explicitly
        using var newPages:Set[String] = new HashSet(),
        it passes the compiler.
        otherwise, I'll have to use the following two statements
        newPages ++= pages
        newPages --= seenPages
      */
      newPages = newPages ++ pages -- seenPages
    } catch {
      case e:Exception => errorHandler(e)
    }
    _crawl(newPages.toList, seenPages, linkFoundHandler, errorHandler)
  }

  private def getRootNodeOfPage(uri:URI):Node = {
    val p = new TagSoupFactoryAdapter
    val conn = uri.toURL.openConnection
    p.loadXML(new InputSource(conn.getInputStream))
  }

}