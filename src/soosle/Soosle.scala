package soosle

import scala.collection.mutable.Set

object Soosle {
  def main(args:Array[String]) {
//    val crawler = new Crawler("soosle.db", Set("http://kiwitobes.com/wiki/Programming_language.html"), 2)
//    crawler.crawl

    val searcher = new Searcher("soosle.db")
    searcher.query("language").foreach {
      result => {
        println("" + result.word + " found in page " + result.pageUrl + ":" + result.location)
      }
    }
  }
}