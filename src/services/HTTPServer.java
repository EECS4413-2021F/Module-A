package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.google.gson.Gson;


/**
 * Example HTTP Server.
 *
 * Supports GET and HEAD requests.
 * Takes a GET and returns a HTTP response to the following endpoints:
 *
 * GET / (root)   Response with the message: "Hello! Welcome to this Server.".
 * GET /gettime   Response with the current date and time on the server.
 * GET /headers   Response with the request headers as a JSON object.
 * GET /qs        Response with the request query-string as a JSON object.
 *
 * If the request is a HEAD request, returns the same response without the content.
 * If the request is not a GET or HEAD request, returns 501 NOT IMPLEMENTED response.
 * If the endpoint does not match one of the above, returns 404 NOT FOUND.
 *
 * Examples:
 *
 *  $ telnet 130.63.96.85 36430
 *    > GET / HTTP/1.1
 *    >
 *    HTTP/1.1 200 OK
 *    Server: Java HTTP Server : 1.0
 *    Date: Thu Sep 23 15:12:43 EDT 2021
 *    Content-type: text/plain
 *    Content-length: 30
 *
 *    Hello! Welcome to this Server.
 *
 *  $ telnet 130.63.96.85 36430
 *    > GET /gettime HTTP/1.1
 *    >
 *    HTTP/1.1 200 OK
 *    Server: Java HTTP Server : 1.0
 *    Date: Thu Sep 23 15:40:29 EDT 2021
 *    Content-type: text/plain
 *    Content-length: 28
 *
 *    Thu Sep 23 15:40:29 EDT 2021
 *
 *  $ telnet 130.63.96.85 36430
 *    > GET /qs?key1=value1&key2=value%20two&key3=value%5Cthree HTTP/1.1
 *    HTTP/1.1 200 OK
 *    Server: Java HTTP Server : 1.0
 *    Date: Thu Sep 23 15:29:54 EDT 2021
 *    Content-type: application/json
 *    Content-length: 58
 *
 *    {"key1":"value1","key2":"value two","key3":"value\\three"}
 *
 *  $ telnet 130.63.96.85 36430
 *    > GET /headers HTTP/1.1
 *    > Host: 130.63.96.85:42507
 *    > Connection: keep-alive
 *    > User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0
 *    > Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*\/*;q=0.8
 *    > Accept-Encoding: gzip, deflate
 *    > Accept-Language: en-US,en;q=0.5
 *    > Upgrade-Insecure-Requests: 1
 *    > DNT 1
 *    >
 *    HTTP/1.1 200 OK
 *    Server: Java HTTP Server : 1.0
 *    Date: Thu Sep 23 15:36:47 EDT 2021
 *    Content-type: application/json
 *    Content-length: 351
 *
 *    {"Accept":"text/html,application/xhtml+xml,application/xml;q\u003d0.9,image/webp,*\/*;
 *    q\u003d0.8","Upgrade-Insecure-Requests":"1","Connection":"keep-alive",
 *    "User-Agent":"Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0",
 *    "Host":"130.63.96.85:42507","Accept-Encoding":"gzip, deflate",
 *    "Accept-Language":"en-US,en;q\u003d0.5","DNT":"1"}
 *
 *  $ telnet 130.63.96.85 36430
 *    > GET /doesnotexist HTTP/1.1
 *    HTTP/1.1 404 NOT FOUND
 *    Server: Java HTTP Server : 1.0
 *    Date: Thu Sep 23 17:01:59 EDT 2021
 *    Content-type: text/plain
 *    Content-length: 9
 *
 *    NOT FOUND
 *
 *  $ telnet 130.63.96.85 36430
 *    > POST / HTTP/1.1
 *    POST / HTTP/1.1
 *    HTTP/1.1 501 NOT IMPLEMENTED
 *    Server: Java HTTP Server : 1.0
 *    Date: Thu Sep 23 17:02:52 EDT 2021
 *    Content-type: text/plain
 *    Content-length: 15
 *
 *  $ telnet 130.63.96.85 36430
 *    > GET / HTTP/2.0
 *    HTTP/1.1 505 HTTP VERSION NOT SUPPORTED
 *    Server: Java HTTP Server : 1.0
 *    Date: Thu Sep 23 17:04:07 EDT 2021
 *    Content-type: text/plain
 *    Content-length: 26
 *
 *    HTTP VERSION NOT SUPPORTED
 *
 */
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

  private String[] getComponents(String resourcePath) {
    if (!resourcePath.contains("?")) {
      return new String[]{ resourcePath, "" };
    } else {
      return resourcePath.split("?", 2);
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

  private Map<String, String> getHeaders(List<String> headerLines) {
    String[] keyvalue;
    Map<String, String> headers = new HashMap<String, String>();

    for (String header : headerLines) {
      keyvalue = header.split(":", 2);
      headers.put(keyvalue[0], keyvalue[1].trim());
    }
    return headers;
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
      String endpoint       = parse.nextToken().toLowerCase(); // The endpoint / URL
      String version        = parse.nextToken().toUpperCase(); // The HTTP version

      int status;
      String response    = "";
      String contentType = "text/plain";

      try {
        if (!method.equals("GET") && !method.equals("HEAD")) { // only support GET + HEAD methods
          status = 501;
        } else if (!version.equals("HTTP/1.1")) { // only support HTTP version 1.1
          status = 505;
        } else {
          status = 200;

          if (endpoint.equals("/")) {
            response = "Hello! Welcome to this Server.";

          } else if (endpoint.equals("/gettime")) {
            response = (new Date()).toString();

          } else if (endpoint.startsWith("/qs?")) {
            String[] components = getComponents(endpoint);
            contentType = "application/json";
            response    = (new Gson()).toJson(getQueryStrings(components[1]));

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
            response    = (new Gson()).toJson(getHeaders(headers));

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
