package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class SquareRootService extends Thread
{
  public static PrintStream log = System.out;

  private Socket client;

  public SquareRootService(Socket client)
  {
    this.client = client;
  }

  public void run()
  {
    log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

    try (Scanner     in  = new Scanner(client.getInputStream());      
         PrintStream out = new PrintStream(client.getOutputStream(), true)) {

      String response;
      String request = in.nextLine();

      double root;

      if (request.matches("^[+-]?\\d+$")) {
        root     = Math.sqrt(Integer.parseInt(request));
        response = "" + root;       
      } else {
        response = "Don't understand: " + request; 
      }

      out.println(response);
    } catch (Exception e) {
      log.println(e);
    } finally {
      try {
        client.close();
        log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
      } catch (Exception e) {
        log.println(e);
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    int port = 0;

    InetAddress host = InetAddress.getLocalHost(); //.getLoopbackAddress();

    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());

      while (true) {
        Socket client = server.accept();

        (new SquareRootService(client)).start();
      }
    }
  }
}
