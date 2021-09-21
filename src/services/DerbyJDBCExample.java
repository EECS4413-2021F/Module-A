package services;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;


public class DerbyJDBCExample {
  private static PrintStream log = System.out;
  public static void main(String[] args) {
    String url   = "jdbc:derby://localhost:64413/EECS";
    String major = args[0];
    double gpa   = Double.parseDouble(args[1]);

    try (Connection connection = DriverManager.getConnection(url)) {
      log.printf("Connected to database: %s\n", connection.getMetaData().getURL());
      String query = "SELECT * FROM Roumani.Sis "
                   + "WHERE major = ? "
                   + "AND gpa >= ?";

      /**
       * !!! This is not secure (SQL injection) !!!
       *
       * String query = "SELECT * FROM Roumani.Sis "
       *              + "WHERE major = ? "
       *              + "AND gpa >= ?";
       * Statement statement = connection.createStatement();
       * ResultSet rs = statement.executeQuery(query);
       */

      try (PreparedStatement statement = connection.prepareStatement(query)) {
        statement.setString(1, major);
        statement.setDouble(2, gpa);

        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            int studentID    = rs.getInt("id");
            String surname   = rs.getString("surname");
            String givenName = rs.getString("givenname");
            double studGPA   = rs.getDouble("gpa");
            int yearAdmitted = rs.getInt("yearadmitted");

            System.out.printf("Student: %d | %s, %s | %f | %d\n",
              studentID,
              surname,
              givenName,
              studGPA,
              yearAdmitted
            );
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
