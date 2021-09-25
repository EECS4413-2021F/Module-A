package services;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

import model.TaxBean;


/**
 * A database access example for connecting and retrieving records
 * from a SQLite database. Returns a single province Tax record
 * that matches the given 2-letter province code and prints it.
 *
 * Usage from command-line:
 *
 *    java SqliteJDBCExample <code>
 *
 * Example:
 *
 *    $ java SqliteJDBCExample ON
 *    Connected to database: jdbc:sqlite:/cs/home/vwchu/4413/pkg/sqlite/Models_R_US.db
 *    Taxes in Ontario (ON):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 8.00%
 *
 *    Disconnected from database.
 *
 */

public class SqliteJDBCExample {
  private static PrintStream log = System.out;
  public static void main(String[] args) {
    String home = System.getProperty("user.home");
    String url  = "jdbc:sqlite:" + home + "/4413/pkg/sqlite/Models_R_US.db";
    String code = args[0];

    try (Connection connection = DriverManager.getConnection(url)) {
      log.printf("Connected to database: %s\n", connection.getMetaData().getURL());
      String query = "SELECT * FROM Tax WHERE code = ?";

      /**
       * !!! This is not secure (SQL injection) !!!
       *
       * String query = "SELECT * FROM Tax WHERE code = " + code;
       * Statement statement = connection.createStatement();
       * ResultSet rs = statement.executeQuery(query);
       */

      try (PreparedStatement statement = connection.prepareStatement(query)) {
        statement.setString(1, code);

        try (ResultSet rs = statement.executeQuery()) {
          TaxBean bean = new TaxBean();
          while (rs.next()) {
            bean.setName(rs.getString("province"));
            bean.setCode(rs.getString("code"));
            bean.setType(rs.getString("type"));
            bean.setGst(rs.getDouble("gst"));
            bean.setPst(rs.getDouble("pst"));
          }
          log.println(bean);
        }
      }
    } catch (SQLException e) {
      log.println(e);
    } finally {
      log.println("Disconnected from database.");
    }
  }
}
