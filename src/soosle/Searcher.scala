package soosle

import models.WordLocation
import com.db4o.query.{Predicate, Query}
import com.db4o.ObjectContainer

class Searcher(val dbname:String) extends NeedsDbConnection(dbname) {

  def query(queryStr:String):List[WordLocation] = {
    var retval:List[WordLocation] = List()

    var retval:Map[String, WordLocation] = Map()
    
    withConnection {
      dbConn => {
        queryStr.split("""\b+""").filter(_.trim != "").foreach {
          word => {
            println("word: " + word)
            retval = retval ::: matchWord(dbConn, word)
          }
        }
      }
    }

    retval
  }

  private def matchWord(dbConn:ObjectContainer, word:String):List[WordLocation] = {
    val result = dbConn.query(new Predicate[WordLocation] {
      def `match`(wl:WordLocation):Boolean = wl.word == word
    })
    
    var retval = List[WordLocation]()
    for (i <- 0 until result.size)
      retval = result.get(i) :: retval

    retval
  }

}