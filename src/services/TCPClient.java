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
  public static PrintStream Log = System.out;

  private static final String[] ErrorMessage = {
    "ERROR: Not enough arguments. Usage: <host> <port>. Expected 2 args, got %d\n",
    "ERROR: Fail to parse host as an IP address or hostname, given '%s'.\n",
    "ERROR: Fail to parse port as an integer, given '%s'.\n",
    "ERROR: Port number out of range, expected integer between 0 and 65536, given %d.\n",
  };
 
  private static boolean isHostAddress(String address) {
    if (address.isEmpty()) {
      return false;
    }
    try {
      Object res = InetAddress.getByName(address);
      return res instanceof Inet4Address || res instanceof Inet6Address;
    } catch (final UnknownHostException exception) {
      return false;
    }
  }

  private static boolean isInteger(String integer, int radix) {
    try (Scanner sc = new Scanner(integer.trim())) {
      if(!sc.hasNextInt(radix)) return false;
      sc.nextInt(radix);
      return !sc.hasNext();
    }
  } 

  private static boolean inPortRange(int port) {
    return 0 <= port && port < 65536;
  }

  private static void abort(int exitCode, String message) {
    Log.println(message);
    System.exit(exitCode);
  }

  private static void validateArgs(String[] args) {
    if (args.length < 2)         abort(1, String.format(ErrorMessage[0], args.length));
    if (!isHostAddress(args[0])) abort(2, String.format(ErrorMessage[1], args[0]));
    if (!isInteger(args[1], 10)) abort(3, String.format(ErrorMessage[2], args[1]));

    int port = Integer.parseInt(args[1]);
    if (!inPortRange(port)) {
      abort(4, String.format(ErrorMessage[3], port));
    }
    return;
  }

  public static void main(String[] args) throws Exception {
    validateArgs(args);

    try (
      Socket client   = new Socket(args[0], Integer.parseInt(args[1]));
      PrintStream req = new PrintStream(client.getOutputStream(), true);
      Scanner res     = new Scanner(client.getInputStream());
      Scanner in      = new Scanner(System.in);
    ) {
      Log.printf("Connected to server %s:%d\n", client.getInetAddress(), client.getPort());
      Log.print("Enter your request, then press <Enter>: ");
      String request = in.nextLine();
      req.println(request);
      String response = res.nextLine();
      Log.print("The response is: ");
      Log.println(response);
    } catch (Exception e) {
      Log.println(e);
    } finally {
      Log.println("Client connection closed.");
    }
  }
}
