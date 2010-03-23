package soosle

import com.db4o.{ObjectContainer, Db4oEmbedded}

/**
 * Because both Crawler and Searcher need DbConnection
 * 
 *
 */

abstract class NeedsDbConnection(dbname:String) {
  
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
}