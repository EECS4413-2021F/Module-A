package services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.google.gson.Gson;

import services.model.TaxBean;
import services.model.TaxCollection;


/**
 * An example microservice for retrieve the Tax information
 * from the Tax table in the database based on the requested
 * information.
 * 
 * This service takes 2 request types: 'code_eq' and 'pst_gt'.
 * When 'code_eq', the service takes a two letter province code and
 * returns a single Tax record. When 'pst_gt', it takes a double, and
 * returns a list of Tax records, all of which have PST greater than the
 * given number. A third parameter specifies the response format,
 * either 'xml' or 'json'.
 * 
 * Usage from telnet, send:
 * 
 *  - code_eq <code> xml
 *  - code_eq <code> json
 *  - pst_gt <number> xml
 *  - pst_gt <number> json
 *
 * Examples:
 *
 *    $ telnet 130.63.96.85 44760
 *      > code_eq ON xml
 *      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 *      <tax>
 *        <code>ON</code>
 *        <gst>5.0</gst>
 *        <name>Ontario</name>
 *        <pst>8.0</pst>
 *        <type>HST</type>
 *      </tax>
 * 
 *    $ telnet 130.63.96.85 44760
 *      > code_eq ON json
 *      {"name":"Ontario","code":"ON","type":"HST","pst":8.0,"gst":5.0}
 * 
 *    $ telnet 130.63.96.85 44760
 *      > pst_gt 9.0 xml
 *      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 *      <taxes>
 *          <tax>
 *              <code>NB</code>
 *              <gst>5.0</gst>
 *              <name>New-Brunswick</name>
 *              <pst>10.0</pst>
 *              <type>HST</type>
 *          </tax>
 *          <tax>
 *              <code>NL</code>
 *              <gst>5.0</gst>
 *              <name>Newfoundland and Labrador</name>
 *              <pst>10.0</pst>
 *              <type>HST</type>
 *          </tax>
 *          <tax>
 *              <code>NS</code>
 *              <gst>5.0</gst>
 *              <name>Nova Scotia</name>
 *              <pst>10.0</pst>
 *              <type>HST</type>
 *          </tax>
 *          <tax>
 *              <code>PE</code>
 *              <gst>5.0</gst>
 *              <name>Prince Edward Island</name>
 *              <pst>10.0</pst>
 *              <type>HST</type>
 *          </tax>
 *          <tax>
 *              <code>QC</code>
 *              <gst>5.0</gst>
 *              <name>Québec</name>
 *              <pst>9.975000000000001</pst>
 *              <type>QST+GST</type>
 *          </tax>
 *      </taxes>
 * 
 *    $ telnet 130.63.96.85 44760
 *      > pst_gt 9.0 json
 *      {"taxes":[{"name":"New-Brunswick","code":"NB","type":"HST","pst":10.0,"gst":5.0},
 *      {"name":"Newfoundland and Labrador","code":"NL","type":"HST","pst":10.0,"gst":5.0},
 *      {"name":"Nova Scotia","code":"NS","type":"HST","pst":10.0,"gst":5.0},
 *      {"name":"Prince Edward Island","code":"PE","type":"HST","pst":10.0,"gst":5.0},
 *      {"name":"Québec","code":"QC","type":"QST+GST","pst":9.975000000000001,"gst":5.0}]}
 *
 */

public class TaxService extends Thread {
  private static PrintStream log = System.out;

  private final String Home = System.getProperty("user.home");
  private final String URL  = "jdbc:sqlite:" + Home + "/4413/pkg/sqlite/Models_R_US.db";

  private Socket client;
  private TaxService(Socket client) {
    this.client = client;
  }

  private String doRequest(String request) {
    String[] token = request.split("\\s+");
    String where   = token[0];
    String search  = token[1];
    String format  = token[2];

    try (Connection connection = DriverManager.getConnection(URL)) {
      log.printf("Connected to database: %s\n", connection.getMetaData().getURL());
      Object responseObject = null;

      if (where.equals("code_eq")) {
        String query = "SELECT * FROM Tax WHERE code = ?";
        if (!search.matches("^[A-Z]{2}$")) {
          return "Invalid search value. Expected two letter province code, got: " + search;
        }
        try (PreparedStatement statement = connection.prepareStatement(query)) {
          statement.setString(1, search);
          try (ResultSet rs = statement.executeQuery()) {
            TaxBean bean = new TaxBean();
            while (rs.next()) {
              bean.setName(rs.getString("province"));
              bean.setCode(rs.getString("code"));
              bean.setType(rs.getString("type"));
              bean.setGst(rs.getDouble("gst"));
              bean.setPst(rs.getDouble("pst"));
            }
            responseObject = bean;
          }
        }
      } else if (where.equals("pst_gt")) {
        String query = "SELECT * FROM Tax WHERE pst > ?";
        if (!search.matches("^[0-9]+(\\.[0-9]+)?$")) {
          return "Invalid search value. Expected percent value, got: " + search;
        }
        try (PreparedStatement statement = connection.prepareStatement(query)) {
          statement.setDouble(1, Double.parseDouble(search));
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
            responseObject = collection;
          }
        }
      } else {
        return "Not implemented: " + where;
      }

      if (format.equals("xml")) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          JAXBContext context = JAXBContext.newInstance(responseObject.getClass());
          Marshaller m = context.createMarshaller();

          m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
          m.marshal(responseObject, baos);
          return baos.toString();
        } catch (Exception e) {
          log.println(e);
          return "XML Error: " + e.getMessage();
        }
      } else if (format.equals("json")) {
        return (new Gson()).toJson(responseObject);
      } else {
        return "Unrecognized format: " + format;
      }
    } catch (SQLException e) {
      log.println(e);
      return "SQL Error: " + e.getMessage();
    } finally {
      log.println("Disconnected from database.");
    }
  }

  public void run() {
    log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

    try (
      Socket client   = this.client; // Makes sure that client is closed at end of try-statement.
      Scanner req     = new Scanner(client.getInputStream());
      PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {
      String response;
      String request = req.nextLine().trim();

      if (request.matches("^(code_eq|pst_gt)\\s+(\\S+)\\s+(json|xml)$")) {
        response = doRequest(request);
      } else {
        response = "Don't understand: " + request;
      }
      res.println(response);
    } catch (Exception e) {
      log.println(e);
    } finally {
      log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 0;
    InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
      while (true) {
        Socket client = server.accept();

        (new TaxService(client)).start();
      }
    }
  }
}
