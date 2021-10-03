package miscs;

import java.io.PrintStream;
import java.util.Scanner;

import com.google.gson.Gson;

import model.TaxCollection;


/**
 * An example for de-serializing the given JSON into a Java Object.
 *
 * Usage from command-line:
 *
 *    java FromJSONExample
 *
 * Example:
 *
 *    $ java FromJSONExample
 *    > {"taxes":[{"name":"New-Brunswick","code":"NB","type":"HST","pst":10.0,"gst":5.0},
 *    > {"name":"Newfoundland and Labrador","code":"NL","type":"HST","pst":10.0,"gst":5.0},
 *    > {"name":"Nova Scotia","code":"NS","type":"HST","pst":10.0,"gst":5.0},
 *    > {"name":"Prince Edward Island","code":"PE","type":"HST","pst":10.0,"gst":5.0},
 *    > {"name":"Québec","code":"QC","type":"QST+GST","pst":9.975000000000001,"gst":5.0}]}
 *    >
 *    Taxes in New-Brunswick (NB):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 10.00%
 *    Taxes in Newfoundland and Labrador (NL):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 10.00%
 *    Taxes in Nova Scotia (NS):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 10.00%
 *    Taxes in Prince Edward Island (PE):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 10.00%
 *    Taxes in Québec (QC):
 *    - Type = QST+GST
 *    - GST  = 5.00%
 *    - PST  = 9.98%
 *
 */

public class FromJSONExample {
  private static PrintStream log = System.out;
  public static void main(String[] args) {
    try (Scanner in = new Scanner(System.in)) {
      String line;
      String input = "";

      while (in.hasNextLine()) {
        line = in.nextLine();
        if (line.isEmpty()) break;
        input += line;
      }

      Gson gson = new Gson();
      TaxCollection collection = (TaxCollection)gson.fromJson(input, TaxCollection.class);
      log.println(collection);
    } catch (Exception e) {
      log.println(e);
    }
  }
}
