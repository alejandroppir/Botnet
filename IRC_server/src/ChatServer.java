import java.net.*;
import java.io.*;

public class  ChatServer  {
  public ChatServer (int port) throws IOException {
    ServerSocket server = new ServerSocket (port);
    while (true) {
      Socket client = server.accept ();
      System.out.println ("Accepted from " + client.getInetAddress ());
      ChatHandler c = new ChatHandler (client);
      c.start ();
    }
  }
  public static void main (String args[]) throws IOException {
    new ChatServer (6666);
  }
}