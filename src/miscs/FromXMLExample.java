package miscs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import model.TaxBean;
import model.TaxCollection;


/**
 * An example for de-serializing the given XML into a Java Object.
 *
 * Usage from command-line:
 *
 *    java FromXMLExample <taxes|tax>
 *
 * Examples:
 *
 *    $ java FromXMLExample tax
 *    > <tax>
 *    >  <code>ON</code>
 *    >  <gst>5.0</gst>
 *    >  <name>Ontario</name>
 *    >  <pst>8.0</pst>
 *    >  <type>HST</type>
 *    > </tax>
 *    >
 *    Taxes in Ontario (ON):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 8.00%
 *
 *    $ java FromXMLExample taxes
 *    > <taxes>
 *    >   <tax>
 *    >     <code>NB</code>
 *    >     <gst>5.0</gst>
 *    >     <name>New-Brunswick</name>
 *    >     <pst>10.0</pst>
 *    >     <type>HST</type>
 *    >   </tax>
 *    >   <tax>
 *    >     <code>NL</code>
 *    >     <gst>5.0</gst>
 *    >     <name>Newfoundland and Labrador</name>
 *    >     <pst>10.0</pst>
 *    >     <type>HST</type>
 *    >   </tax>
 *    > </taxes>
 *    >
 *    Taxes in New-Brunswick (NB):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 10.00%
 *    Taxes in Newfoundland and Labrador (NL):
 *    - Type = HST
 *    - GST  = 5.00%
 *    - PST  = 10.00%
 *
 */
public class FromXMLExample {
  private static PrintStream log = System.out;
  public static void main(String[] args) {
    String type = args[0];
    String input = "";
    String line;

    try (Scanner in = new Scanner(System.in)) {
      while (in.hasNextLine()) {
        line = in.nextLine();
        if (line.isEmpty()) break;
        input += line;
      }
    }

    try (InputStream stream = new ByteArrayInputStream(input.getBytes())) {
      if (type.equals("taxes")) {
        JAXBContext context = JAXBContext.newInstance(TaxCollection.class);
        Unmarshaller u = context.createUnmarshaller();
        TaxCollection collection = (TaxCollection)u.unmarshal(stream);

        log.println(collection);
      } else if (type.equals("tax")) {
        JAXBContext context = JAXBContext.newInstance(TaxBean.class);
        Unmarshaller u = context.createUnmarshaller();
        TaxBean bean = (TaxBean)u.unmarshal(stream);

        log.println(bean);
      } else {
        log.println("Unrecognized type: " + type);
      }
    } catch (Exception e) {
      log.println(e);
    }
  }
}
