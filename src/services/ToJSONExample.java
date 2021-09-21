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

import services.model.TaxBean;
import services.model.TaxCollection;


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
          List<TaxBean> taxes      = new ArrayList<>();
          TaxCollection collection = new TaxCollection();

          while (rs.next()) {
            TaxBean bean = new TaxBean();
            bean.setName(rs.getString("province"));
            bean.setCode(rs.getString("code"));
            bean.setType(rs.getString("type"));
            bean.setGst(rs.getDouble("gst"));
            bean.setPst(rs.getDouble("pst"));
            taxes.add(bean);
          }

          collection.setTaxes(taxes);

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
