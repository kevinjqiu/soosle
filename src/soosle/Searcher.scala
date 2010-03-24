package soosle

import models.WordLocation
import com.db4o.query.{Predicate, Query}

class Searcher(val dbname:String) extends NeedsDbConnection(dbname) {

  def query(queryStr:String):List[WordLocation] = {
    var retval:List[WordLocation] = List()
    
    withConnection {
      dbConn => {
        var re = queryStr.split("""\b+""").filter(_ != "").map(".*" + _ + ".*").mkString(" | ")
        println("re=" + re)

        var result = dbConn.query(new Predicate[WordLocation] {
          def `match`(wl:WordLocation):Boolean = {
            return wl.word.matches(re)
          }
        })

        // Make a copy of the result set
        // since the connection is going to be closed
        // the copied result set is going to be returned
        // XXX: more idiomatic way?
        for (i<-0 until result.size)
          retval = result.get(i) :: retval
      }
    }

    retval
  }

}