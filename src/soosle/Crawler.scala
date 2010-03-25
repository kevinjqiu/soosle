package soosle

import models.{Link, WordLocation, PageURL}
import org.xml.sax.InputSource
import xml.{NodeSeq, Node}
import tagsoup.TagSoupFactoryAdapter
import java.net.URI
import collection.mutable.{Set,HashSet}
import com.db4o.{Db4oEmbedded, ObjectContainer}

class Crawler(val dbname:String, val pages:Set[String], val depth:Int) {
  val ignored = Set("the", "of", "to", "and", "a", "in", "is", "it", "")
  
  var connection = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration, dbname)

  def withConnection(op:ObjectContainer=>Unit) {

    try {
      op(connection)
      connection.commit
    } catch {
      case e:Exception => e.printStackTrace
    } finally {
      connection.close
    }
    
  }

  def crawl() {

    withConnection {
      dbConn => {
        // Now we have connection object
        // that's visible to the closures here
        def isIndexed(url:String) = dbConn.queryByExample(new PageURL(url)).size != 0

        def addLinkRef(from:String, to:String, linkText:String) {
          dbConn.store(new Link(from, to, linkText))
        }

        val addToIndex = (url:String, rootNode:Node) => {
          if (isIndexed(url))
            false

          dbConn.store(new PageURL(url))

          val text = extractText(rootNode)
          val words = text.split("""\W+""").filter(!ignored.contains(_)).map(_.toLowerCase)

          var wordLoc = 0
          words.foreach(word=>{
            dbConn.store(new WordLocation(word, url, wordLoc))
            wordLoc += 1
          })
          true

        }

        val linkFoundHandler = (newPages:Set[String], currentPageUri:URI, link:Node) => {
          val rel:String = (link \ "@href").text
          val newPageUri = currentPageUri.resolve(rel).toString
          if (!isIndexed(newPageUri))
            newPages += newPageUri
          addLinkRef(currentPageUri.toString, newPageUri, link.text)
        }

        for (i <- 0 until depth) {
          println("Depth=" + depth)
          _crawl(pages.toList, new HashSet[String](),
            addToIndex, linkFoundHandler, (e:Exception) => e.printStackTrace)
        }
      }
    }
  }

  def _crawl(pages:List[String],
             seenPages:Set[String],
             newPageFoundHandler:(String,Node)=>Boolean,           
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
      newPageFoundHandler(currentPage, rootNode)
      // Process the links appear on the current page
      val links = rootNode \\ "a"

      // XXX: Add the condition @href!=None in the XPath itself? How?
      links.filter(link=>link.attribute("href") != None).foreach(link=>{
        linkFoundHandler(newPages, currentPageUri, link)
      })

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
    _crawl(newPages.toList, seenPages, newPageFoundHandler, linkFoundHandler, errorHandler)
  }

  private def extractText(node:Node):String = {
    val text = node.text
    if (text == Nil) {
      var retval:List[String] = List()
      node.child.foreach(child=> retval = extractText(child) :: retval)
      retval.mkString("\n")
    } else {
      text.trim 
    }
  }

  private def getRootNodeOfPage(uri:URI):Node = {
    val p = new TagSoupFactoryAdapter
    val conn = uri.toURL.openConnection
    p.loadXML(new InputSource(conn.getInputStream))
  }

}
