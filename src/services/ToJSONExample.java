package services;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;

import model.TaxBean;
import model.TaxCollection;


/**
 * An example for serializing the retrieved database
 * records as a JSON object. Returns a list of the provinces
 * that have PST greater than the given number.
 *
 * Usage from command-line:
 *
 *    java ToJSONExample <pst>
 *
 * Example:
 *
 *    $ java ToJSONExample 9.0
 *    Connected to database: jdbc:sqlite:/cs/home/vwchu/4413/pkg/sqlite/Models_R_US.db
 *    {"taxes":[{"name":"New-Brunswick","code":"NB","type":"HST","pst":10.0,"gst":5.0},
 *    {"name":"Newfoundland and Labrador","code":"NL","type":"HST","pst":10.0,"gst":5.0},
 *    {"name":"Nova Scotia","code":"NS","type":"HST","pst":10.0,"gst":5.0},
 *    {"name":"Prince Edward Island","code":"PE","type":"HST","pst":10.0,"gst":5.0},
 *    {"name":"Québec","code":"QC","type":"QST+GST","pst":9.975000000000001,"gst":5.0}]}
 *    Disconnected from database.
 *
 */

public class ToJSONExample {
  private static PrintStream log = System.out;
  public static void main(String[] args) {
    String home = System.getProperty("user.home");
    String url  = "jdbc:sqlite:" + home + "/4413/pkg/sqlite/Models_R_US.db";
    double pst  = Double.parseDouble(args[0]);

    try (Connection connection = DriverManager.getConnection(url)) {
      log.printf("Connected to database: %s\n", connection.getMetaData().getURL());
      String query = "SELECT * FROM Tax WHERE pst > ?";

      /**
       * !!! This is not secure (SQL injection) !!!
       *
       * String query = "SELECT * FROM Tax WHERE pst > " + pst;
       * Statement statement = connection.createStatement();
       * ResultSet rs = statement.executeQuery(query);
       */

      try (PreparedStatement statement = connection.prepareStatement(query)) {
        statement.setDouble(1, pst);

        try (ResultSet rs = statement.executeQuery()) {
          List<TaxBean> list       = new ArrayList<>();
          TaxCollection collection = new TaxCollection();

          while (rs.next()) {
            TaxBean bean = new TaxBean();
            bean.setName(rs.getString("province"));
            bean.setCode(rs.getString("code"));
            bean.setType(rs.getString("type"));
            bean.setGst(rs.getDouble("gst"));
            bean.setPst(rs.getDouble("pst"));
            list.add(bean);
          }

          collection.setTaxes(list);

          Gson gson = new Gson();
          log.println(gson.toJson(collection));
        }
      }
    } catch (SQLException e) {
      log.println(e);
    } finally {
      log.println("Disconnected from database.");
    }
  }
}
