package soosle

object Soosle {
  def main(args:Array[String]) {
    val crawler = new Crawler("", Set("http://kiwitobes.com/wiki/Programming_language.html"), 2)
    crawler.crawl
  }
}