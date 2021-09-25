package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTTPCalcService extends Thread {

  private static final PrintStream log = System.out;
  private static final Map<Integer, String> httpResponseCodes = new HashMap<>();
  private static final Pattern isDouble = Pattern.compile("^[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?$");
  private static final String[] endpoints = {
    "/add",
    "/subtract",
    "/multiply",
    "/divide",
    "/power"
  };

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

  private HTTPCalcService(Socket client) {
    this.client = client;
  }

  private void sendHeaders(PrintStream res, int code, String contentType, String response) {
    sendHeaders(res, code, contentType, response, new String[]{});
  }
  private void sendHeaders(PrintStream res, int code, String contentType, String response, String[] headers) {
    // send HTTP Headers
    res.printf("HTTP/1.1 %d %s\n", code, httpResponseCodes.get(code));
    res.println("Server: Java HTTP Server : 1.0");
    res.println("Date: " + new Date());
    res.println("Content-type: " + contentType);
    res.println("Content-length: " + response.getBytes().length);
    Arrays.stream(headers).forEach(h -> res.println(h));
    res.println(); // blank line between headers and content, very important !
  }

  private String[] getComponents(String resourcePath) {
    if (!resourcePath.contains("?")) {
      return new String[]{ resourcePath, "" };
    } else {
      return resourcePath.split("\\?", 2);
    }
  }
  
  private Map<String, String> getQueryStrings(String qs) throws Exception {
    Map<String, String> queries = new HashMap<>();
    String[] fields = qs.split("&");
    
    for (String field : fields) {
      String[] pairs = field.split("=", 2);
      if (pairs.length == 2) {
        queries.put(pairs[0], URLDecoder.decode(pairs[1], "UTF-8"));
      }
    }

    return queries;
  }

  public void run() {
    final String clientAddress = String.format("%s:%d", client.getInetAddress(), client.getPort());
    log.printf("Connected to %s\n", clientAddress);

    try (
      Socket client   = this.client; // Makes sure that client is closed at end of try-statement.
      Scanner req     = new Scanner(client.getInputStream());
      PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {
      String request = req.nextLine();
      String method, resource, version;
      String response = "";

      List<String> headers = new ArrayList<>();
      
      try (Scanner parse = new Scanner(request)) {
        method   = parse.next();
        resource = parse.next();
        version  = parse.next(); 
      }

      int status = 200;

      try {
        if (!method.equals("GET")) {
          status = 501;
        } else if (!version.equals("HTTP/1.1")) {
          status = 505;
        } else if (Arrays.stream(endpoints).anyMatch((s) -> resource.startsWith(s + "?"))) {
          String[] components = getComponents(resource);
          String   location   = String.format("/calc?op=%s&%s", components[0].substring(1), components[1]);

          status = 301;
          headers.add("Location: " + location); // redirection

        } else if (resource.startsWith("/calc?")) {
          Map<String, String> qs = getQueryStrings(resource.substring(resource.indexOf('?') + 1));

          if (qs.containsKey("op") && qs.containsKey("a") && qs.containsKey("b")) {
            String  op = qs.get("op");
            Matcher matcherA = isDouble.matcher(qs.get("a"));
            Matcher matcherB = isDouble.matcher(qs.get("b"));

            if (matcherA.find() && matcherB.find()) {
              double a = Double.parseDouble(qs.get("a"));
              double b = Double.parseDouble(qs.get("b"));

              if (op.equals("add")) {
                response = "" + (a + b);
              } else if (op.equals("subtract")) {
                response = "" + (a - b);                
              } else if (op.equals("multiply")) {
                response = "" + (a * b);
              } else if (op.equals("divide")) {
                response = "" + (a / b);
              } else if (op.equals("power")) {
                response = "" + Math.pow(a, b);
              }
            }
          }

          if (status == 200 && response.isEmpty()) {
            status = 400;
          }
        } else {
          status = 404;
        }
      } catch (Exception e) {
        log.println(e);
        e.printStackTrace(log);
        status = 500;
      }

      if (status != 200 && response.isEmpty()) {
        response = httpResponseCodes.get(status);
      }

      if (headers.size() > 0) {
        sendHeaders(res, status, "text/plain", response, headers.toArray(new String[]{}));
      } else {
        sendHeaders(res, status, "text/plain", response);
      }

      res.println(response);
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
        (new HTTPCalcService(client)).start();
      }
    }
  }
}
