package services;

import java.net.*;
import java.util.Scanner;
import java.io.PrintStream;


/**
 * Example TCP Client. Given as argument the
 * hostname or IP address of the remote server and
 * the port number which it is resides, sends a single
 * request to the server and print the response.
 *
 * Usage:
 *
 *    java TCPClient <host> <port>
 *
 * The request text is read from standard input.
 *
 * Most of this code is error handling and validating
 * input arguments. We check if the host is a valid
 * hostname or IP address by attempting to resolve via
 * a DNS request. We check that the port number is an
 * integer between 0 and 65535 (16-bits).
 *
 * To use with your web service, first start the server:
 *
 *    $ java services.SquareRootService
 *    Server listening on ea78/130.63.96.34:39653
 *
 * Then, run this program:
 *
 *    $ java services.TCPClient 130.63.96.34 39653
 *    Connected to server /130.63.96.34:39653
 *    Enter your request, then press <Enter>: 169
 *    The response is: 13.0
 *    Client connection closed.
 *
 * On the server's console, you should see like this:
 *
 *	  Connected to /130.63.96.34:59150
 *    Disconnected from /130.63.96.34:59150
 *
 */
public class TCPClient {
  private static PrintStream log = System.out;
  public static void main(String[] args) throws Exception {
    // Normally, I would validate my arguments first, but to keep this example succinct, I won't.
    try (
      Socket client   = new Socket(args[0], Integer.parseInt(args[1]));
      PrintStream req = new PrintStream(client.getOutputStream(), true);
      Scanner res     = new Scanner(client.getInputStream());
      Scanner in      = new Scanner(System.in);
    ) {
      log.printf("Connected to server %s:%d\n", client.getInetAddress(), client.getPort());
      log.print("Enter your request, then press <Enter>: ");
      String request = in.nextLine();
      req.println(request);

      String response = res.nextLine();
      log.print("The response is: ");
      log.println(response);
    } catch (Exception e) {
      log.println(e);
    } finally {
      log.println("Client connection closed.");
    }
  }
}
