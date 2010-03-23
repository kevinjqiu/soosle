package soosle

import models.WordLocation
import com.db4o.query.{Predicate, Query}

class Searcher(val dbname:String) extends NeedsDbConnection(dbname) {

  def query(queryStr:String) = {
    withConnection {
      dbConn => {
        var re = queryStr.split("""\b+""").map(".*" + _ + ".*").mkString(" | ")
        var result = dbConn.query(new Predicate[WordLocation] {
          def `match`(wl:WordLocation):Boolean = {
            false // TODO
          }
        })
      }
    }
  }

}