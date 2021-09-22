package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.google.gson.Gson;

public class HTTPServer extends Thread {
  private static PrintStream log = System.out;
  private static final Map<Integer, String> httpResponseCodes = new HashMap<>();

  static {
    httpResponseCodes.put(100, "HTTP CONTINUE");
    httpResponseCodes.put(101, "SWITCHING PROTOCOLS");
    httpResponseCodes.put(200, "OK");
    httpResponseCodes.put(201, "CREATED");
    httpResponseCodes.put(202, "ACCEPTED");
    httpResponseCodes.put(203, "NON AUTHORITATIVE INFORMATION");
    httpResponseCodes.put(204, "NO CONTENT");
    httpResponseCodes.put(205, "RESET CONTENT");
    httpResponseCodes.put(206, "PARTIAL CONTENT");
    httpResponseCodes.put(300, "MULTIPLE CHOICES");
    httpResponseCodes.put(301, "MOVED PERMANENTLY");
    httpResponseCodes.put(302, "MOVED TEMPORARILY");
    httpResponseCodes.put(303, "SEE OTHER");
    httpResponseCodes.put(304, "NOT MODIFIED");
    httpResponseCodes.put(305, "USE PROXY");
    httpResponseCodes.put(400, "BAD REQUEST");
    httpResponseCodes.put(401, "UNAUTHORIZED");
    httpResponseCodes.put(402, "PAYMENT REQUIRED");
    httpResponseCodes.put(403, "FORBIDDEN");
    httpResponseCodes.put(404, "NOT FOUND");
    httpResponseCodes.put(405, "METHOD NOT ALLOWED");
    httpResponseCodes.put(406, "NOT ACCEPTABLE");
    httpResponseCodes.put(407, "PROXY AUTHENTICATION REQUIRED");
    httpResponseCodes.put(408, "REQUEST TIME OUT");
    httpResponseCodes.put(409, "CONFLICT");
    httpResponseCodes.put(410, "GONE");
    httpResponseCodes.put(411, "LENGTH REQUIRED");
    httpResponseCodes.put(412, "PRECONDITION FAILED");
    httpResponseCodes.put(413, "REQUEST ENTITY TOO LARGE");
    httpResponseCodes.put(414, "REQUEST URI TOO LARGE");
    httpResponseCodes.put(415, "UNSUPPORTED MEDIA TYPE");
    httpResponseCodes.put(500, "INTERNAL SERVER ERROR");
    httpResponseCodes.put(501, "NOT IMPLEMENTED");
    httpResponseCodes.put(502, "BAD GATEWAY");
    httpResponseCodes.put(503, "SERVICE UNAVAILABLE");
    httpResponseCodes.put(504, "GATEWAY TIME OUT");
    httpResponseCodes.put(505, "HTTP VERSION NOT SUPPORTED");
  }

  private Socket client;
  private HTTPServer(Socket client) {
    this.client = client;
  }

  private void sendHeaders(PrintStream res, int code, String contentType, String response) {
    // send HTTP Headers
    res.printf("HTTP/1.1 %d %s\n", code, httpResponseCodes.get(code));
    res.println("Server: Java HTTP Server : 1.0");
    res.println("Date: " + new Date());
    res.println("Content-type: " + contentType);
    res.println("Content-length: " + response.getBytes().length);
    res.println(); // blank line between headers and content, very important !
  }

  private String getQueryStrings(String qs) {
    Map<String, String> queries = new HashMap<>();
    String[] fields = qs.split("&");

    for (String field : fields) {
      String[] pairs = field.split("=");
      if (pairs.length == 2) {
        queries.put(pairs[0], pairs[1]);
      }
    }

    Gson gson = new Gson();
    return gson.toJson(queries);
  }

  private String getHeaders(List<String> headerLines) {
    String[] keyvalue;
    HashMap<String, String> headers = new HashMap<String, String>();

    for (String header : headerLines) {
      keyvalue = header.split(":");
      headers.put(keyvalue[0], keyvalue[1].trim());
    }

    Gson gson = new Gson();
    return gson.toJson(headers);
  }

  public void run() {
    final String clientAddress = String.format("%s:%d", client.getInetAddress(), client.getPort());
    log.printf("Connected to %s\n", clientAddress);
    
    try (
      Socket client   = this.client; // Makes sure that client is closed at end of try-statement.
      Scanner req     = new Scanner(client.getInputStream());
      PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {
      String request        = req.nextLine();
      StringTokenizer parse = new StringTokenizer(request);
      String method         = parse.nextToken().toUpperCase(); // The HTTP method requested
      String endpoint       = parse.nextToken().toLowerCase(); // The endpoint.

      int status;
      String response    = "";
      String contentType = "text/plain";

      try {
        // we support only GET and HEAD methods, we check
        if (!method.equals("GET") && !method.equals("HEAD")) {
          status = 501;
        } else {
          status = 200;
  
          if (endpoint.equals("/")) {
            response = "Hello! Welcome to this Server.";
  
          } else if (endpoint.equals("/gettime")) {
            response = (new Date()).toString();
  
          } else if (endpoint.startsWith("/qs?")) {
            String qs   = endpoint.substring(endpoint.indexOf('?') + 1); // The query string
            endpoint    = endpoint.substring(0, endpoint.indexOf('?'));
            contentType = "application/json";
            response    = getQueryStrings(qs);
  
          } else if (endpoint.equals("/headers")) {
            // Read the request headers
            String buff;
            List<String> headers = new ArrayList<>();

            while (req.hasNextLine()) {
              buff = req.nextLine();
              if (buff.isEmpty()) break;
              headers.add(buff);
            }
  
            contentType = "application/json";
            response    = getHeaders(headers);
  
          } else {
            status = 404;
          }
        }
      } catch (Exception e) {
        status = 500;
      }

      if (status != 200) {
        response = httpResponseCodes.get(status);
      }

      log.printf("%s: %d - %s\n", clientAddress, status, request);
      sendHeaders(res, status, contentType, response);

      if (method.equals("GET")) {
        res.println(response);
      }

      res.flush(); // flush character output stream buffer
    } catch (Exception e) {
      log.println(e);
    } finally {
      log.printf("Disconnected from %s\n", clientAddress);
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 0;
    InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
      while (true) {
        Socket client = server.accept();
        (new HTTPServer(client)).start();
      }
    }
  }
}
