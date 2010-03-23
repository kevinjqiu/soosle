package soosle

import scala.collection.mutable.Set

object Soosle {
  def main(args:Array[String]) {
    val crawler = new Crawler("soosle.db", Set("http://kiwitobes.com/wiki/Programming_language.html"), 2)
    crawler.crawl

    val searcher = new Searcher("soosle.db")
    println(searcher.search("java"))
  }
}