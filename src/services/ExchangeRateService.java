package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * A simple TCP service that converts Euros (EUR) to Canadian dollars (CAD).
 * Takes a positive integer via a TCP request and responses with the
 * decimal amount in Canadian dollars.
 *
 * Requires the environment variable APIKEY to be set. 
 * To obtain an API key, refer to: https://exchangeratesapi.io/
 * Requires signing up with a free account.
 */
public class ExchangeRateService extends Thread {
  
  private static PrintStream log = System.out;

  private Socket client;

  public ExchangeRateService(Socket client) {
    this.client = client;
  }

  public void run() {
    log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());
    
    try (
      Socket client   = this.client;
      Scanner req     = new Scanner(client.getInputStream()); 
      PrintStream res = new PrintStream(client.getOutputStream(), true)
    ) {	
      String response;
      String request = req.nextLine();

      // Load API Key from environment variable
      String apiKey = System.getenv("APIKEY");

      // Check input is number (value x EUR)
      if (request.matches("^\\d+$")) {
        URL url = new URL("http://data.fixer.io/api/latest?access_key=" + apiKey);

        String payload = "";
        try (Scanner http = new Scanner(url.openStream())) {
          while (http.hasNextLine()) {
            payload += http.nextLine();
          }
        }

        // Get exchange rate from JSON API response
        JsonParser parser = new JsonParser();
        JsonObject data   = parser.parse(payload).getAsJsonObject();

        int value   = Integer.parseInt(request);
        double rate = data.get("rates").getAsJsonObject()
                          .get("CAD").getAsDouble();

        response = "CAD: " + (value * rate);
      } else {
        response = "Don't understand: " + request;
      }
      res.println(response);
    } catch (Exception e) {
      log.println("Error: " + e);
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
        (new ExchangeRateService(client)).start();
      }
    }
  }
}
