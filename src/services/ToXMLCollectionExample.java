package services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import services.model.TaxBean;
import services.model.TaxCollection;


/**
 * An example for serializing the retrieved database
 * records as a XML document. Returns a list of the provinces
 * that have PST greater than the given number.
 * 
 * Usage from command-line:
 * 
 *    java ToXMLCollectionExample <pst>
 *
 * Example:
 * 
 *    $ java ToXMLCollectionExample 9.0
 *    Connected to database: jdbc:sqlite:/cs/home/vwchu/4413/pkg/sqlite/Models_R_US.db
 *    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 *    <taxes>
 *      <tax>
 *        <code>NB</code>
 *        <gst>5.0</gst>
 *        <name>New-Brunswick</name>
 *        <pst>10.0</pst>
 *        <type>HST</type>
 *      </tax>
 *      <tax>
 *        <code>NL</code>
 *        <gst>5.0</gst>
 *        <name>Newfoundland and Labrador</name>
 *        <pst>10.0</pst>
 *        <type>HST</type>
 *      </tax>
 *      <tax>
 *        <code>NS</code>
 *        <gst>5.0</gst>
 *        <name>Nova Scotia</name>
 *        <pst>10.0</pst>
 *        <type>HST</type>
 *      </tax>
 *      <tax>
 *        <code>PE</code>
 *        <gst>5.0</gst>
 *        <name>Prince Edward Island</name>
 *        <pst>10.0</pst>
 *        <type>HST</type>
 *      </tax>
 *      <tax>
 *        <code>QC</code>
 *        <gst>5.0</gst>
 *        <name>Québec</name>
 *        <pst>9.975000000000001</pst>
 *        <type>QST+GST</type>
 *      </tax>
 *    </taxes>
 *
 *    Disconnected from database.
 *
 */

public class ToXMLCollectionExample {
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

          try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JAXBContext context = JAXBContext.newInstance(TaxCollection.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(collection, baos);
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
