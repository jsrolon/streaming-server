package server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.net.*;
import java.io.*;

public class Server {

	static int serverPort = 9975;

	private int stageClients = 100;
	
	ArrayList<String> dosBlockedIPs;
	
	Hashtable<String, String> users;
	
	Hashtable<String, Integer> activeIPs;
	

	public Server() {
		users = new Hashtable<String, String>();
		activeIPs = new Hashtable<String, Integer>();
		dosBlockedIPs = new ArrayList<String>();

		users.put("emilnamen", hashlibMD5("emil"));
		activeIPs.put("192.168.0.16", 0);
		activeIPs.put("192.168.1.146", 0);

		try {

			while (true) {
				ServerSocket socket = new ServerSocket(serverPort);

				Socket connectionSocket = socket.accept();
				socket.close();
				
				new Atendedor(connectionSocket).start();
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());

			System.exit(1);
		}
	}

	public static void main(String argv[]) {
		new Server();
	}

	private String hashlibMD5(String param) {
		byte[] bytesOfMessage;
		try {
			bytesOfMessage = param.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			String password = new String(thedigest);
			return password;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private class Atendedor extends Thread {
		
		Socket connectionSocket;
		
		public Atendedor(Socket cS) {
			connectionSocket = cS;
		}
		
		public void run() {
			
			double averageAtentionTime = 0.0;
			int numClientsQueue = 0;
			int activeClients = 0;
			double averageTimeQueue = 0.0;
			
			try {
				double initialAttentionTime = System.currentTimeMillis();
				String activeIPAddress = connectionSocket.getRemoteSocketAddress().toString().split(":")[0].replace("/",
						"");

				if (dosBlockedIPs.contains(activeIPAddress) == false) {
					int contadorIPs = activeIPs.get(activeIPAddress) + 1;
					System.out.println("" + contadorIPs);
					activeIPs.put(activeIPAddress, contadorIPs);
					if (activeIPs.get(activeIPAddress) > 3) {
						System.out.println("D.O.S DETECTED");
						dosBlockedIPs.add(activeIPAddress);
						connectionSocket.close();
					} else {
						BufferedReader inFromClient = new BufferedReader(
								new InputStreamReader(connectionSocket.getInputStream()));
						String clientSentence = inFromClient.readLine();
						String userReceived = clientSentence.split(";")[0];
						String psswdReceived = hashlibMD5(clientSentence.split(";")[1]);
						if (users.get(userReceived).compareTo(psswdReceived) == 0) {
							activeClients++;
							numClientsQueue = stageClients - activeClients;
							System.out.println("CREDENTIALS OK");
							byte[] msjOK = "Credentials OK dude\n".getBytes();
							connectionSocket.getOutputStream().write(msjOK);
							Thread.sleep(1000);
							// esto hace que empiece udp
							new Streaming();
						} else {
							System.out.println("CHECK YOUR CREDENTIALS");
							byte[] msjFAIL = "Credentials FAIL dude\n".getBytes();
							connectionSocket.getOutputStream().write(msjFAIL);
							connectionSocket.getOutputStream().flush();
						}
						inFromClient.close();
						activeClients--;
						averageAtentionTime = System.currentTimeMillis() - initialAttentionTime;
						System.out.println("Active Clients: " + activeClients);
						System.out.println("Clients in Queue: " + numClientsQueue);
						System.out.println("Average Attention Time: " + averageAtentionTime);
						System.out.println("Average Queue Time: " + averageTimeQueue);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
