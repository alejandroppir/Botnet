import java.net.*;
import java.io.*;
import java.util.*;

public class ChatHandler extends Thread {
	protected Socket s;
	protected DataInputStream i;
	protected DataOutputStream o;

	public ChatHandler(Socket s) throws IOException {
		this.s = s;
		i = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		o = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
	}

	protected static Vector<ChatHandler> handlers = new Vector<ChatHandler>();

	public void run() {
		String name = s.getInetAddress().toString();
		try {
			//broadcast(name + " has joined.");
			handlers.addElement(this);
			while (true) {
				String msg = i.readUTF();
				broadcast(msg);
			}
		} catch (EOFException ex) {
			System.out.println("Bot " + name + " desconectado");
		}  catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			handlers.removeElement(this);
			//broadcast(name + " has left.");
			try {
				s.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected static void broadcast(String message) {
		synchronized (handlers) {
			Enumeration<ChatHandler> e = handlers.elements();
			while (e.hasMoreElements()) {
				ChatHandler c = (ChatHandler) e.nextElement();
				try {
					synchronized (c.o) {
						c.o.writeUTF(message);
					}
					c.o.flush();
				} catch (IOException ex) {
					c.stop();
				}
			}
		}
	}
}