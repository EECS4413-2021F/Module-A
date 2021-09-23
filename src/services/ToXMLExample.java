package services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import services.model.TaxBean;


/**
 * An example for serializing the retrieved database
 * records as a XML document. Returns a single province Tax record
 * that matches the given 2-letter province code.
 *
 * Usage from command-line:
 *
 *    java ToXMLExample <code>
 *
 * Example:
 *
 *    $ java ToXMLExample ON
 *    Connected to database: jdbc:sqlite:/cs/home/vwchu/4413/pkg/sqlite/Models_R_US.db
 *    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 *    <tax>
 *      <code>ON</code>
 *      <gst>5.0</gst>
 *      <name>Ontario</name>
 *      <pst>8.0</pst>
 *      <type>HST</type>
 *    </tax>
 *
 *    Disconnected from database.
 *
 */

public class ToXMLExample {
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

          try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JAXBContext context = JAXBContext.newInstance(TaxBean.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(bean, baos);
            log.println(baos);
          } catch (Exception e) {
            log.println(e);
          }
        }
      }
    } catch (SQLException e) {
      log.println(e);
    } finally {
      log.println("Disconnected from database.");
    }
  }
}
