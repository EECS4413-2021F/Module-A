package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


/**
 * Example TCP service. This service returns the square
 * root value for the integer value given. The TCP server
 * opens and listens to a randomly assigned TCP port. You
 * can then make a request to it via Telnet.
 *
 *      telnet <host> <port>
 *
 * You can do this via a local terminal or remote from
 * another computer on the same local area network. Or
 * over the Internet if your computer is expose the wide
 * area network via your router. For security reasons,
 * normally, your personal computer isn't. And you will get:
 *
 *      $ telnet 130.63.96.85 36302
 *      Trying 130.63.96.85...
 *      telnet: Unable to connect to remote host: Connection timed out
 *
 * In the Remote Lab, you can start this server and login into
 * another lab computer or ssh into red.eecs.yorku.ca and send a
 * request to the server. Make sure your code is using:
 *
 *     InetAddress host = InetAddress.getLocalHost();
 *
 * This is your computer's IP as seemed from the local area network
 * or from the Internet. If you use:
 *
 *     InetAddress host = InetAddress.getLoopbackAddress();
 *
 * This will give you an IP only you can use from your current
 * computer. The IP address (127.0.0.1) is the loopback for localhost.
 * To connect to the server from another computer, requires your
 * computer's private/public IP address.
 *
 * Try these from red.eecs.yorku.ca:
 *
 *     $ telnet <host> <port>
 *     Trying 130.63.96.85...
 *     Connected to 130.63.96.85.
 *     Escape character is '^]'.
 *     > 25
 *     5.0
 *     Connection closed by foreign host.
 *
 *     $ telnet <host> <port>
 *     Trying 130.63.96.85...
 *     Connected to 130.63.96.85.
 *     Escape character is '^]'.
 *     > -15
 *     NaN
 *     Connection closed by foreign host.
 *
 *     $ telnet <host> <port>
 *     Trying 130.63.96.85...
 *     Connected to 130.63.96.85.
 *     Escape character is '^]'.
 *     > ABCD
 *     Don't understand: ABCD
 *     Connection closed by foreign host.
 *
 */

public class SquareRootService extends Thread {
  public static PrintStream Log = System.out;

  private Socket client;
  private SquareRootService(Socket client) {
    this.client = client;
  }

  public void run() {
    Log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

    try (
      Socket  _client = this.client; // Makes sure that client is closed at end of try-statement. 
      Scanner     req = new Scanner(client.getInputStream());
      PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {
      String response;
      String request = req.nextLine();

      double root;

      if (request.matches("^[+-]?\\d+$")) {
        root     = Math.sqrt(Integer.parseInt(request));
        response = "" + root;
      } else {
        response = "Don't understand: " + request;
      }
      res.println(response);
    } catch (Exception e) {
      Log.println(e);
    } finally {
      Log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 0;
    InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      Log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
      while (true) {
        Socket client = server.accept();

        (new SquareRootService(client)).start();
      }
    }
  }
}
