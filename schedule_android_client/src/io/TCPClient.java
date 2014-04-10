package io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import io.packet.ServerPacket;
import io.packet.ClientPacket;

public abstract class TCPClient
{
	public static final int TIMEOUT = (1 * 60 * 1000);
	
	private Socket socket = null;
	private DataOutputStream outToServer = null;
	private BufferedReader inFromServer = null;
	
	private StreamReader reader = null;

	private boolean connected = false;

	private String host;
	private int port;

	public TCPClient(String host, int port) throws Exception {
		this.host = host;
		this.port = port;
	}

	protected void finalize() throws Exception {
		if (connected)
			disconnect();
	}

	public void connect() {
		while (!connected) {
			try {
				socket = new Socket(host, port);
				outToServer = new DataOutputStream(socket.getOutputStream());
				inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				reader = new StreamReader(inFromServer, this);
				reader.start();

				connected = true;
			} catch (IOException e) {
				System.out.println("No server connection");
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException sleepException) {
					System.out.print("Error on setting this thread to sleep");
					sleepException.printStackTrace();
				}
			}		
		}
	}

	public void disconnect() {
		try {
			reader.stopGracefully();

			inFromServer.close();
			outToServer.close();

			socket.close();
		} catch (IOException e) {
			System.out.println("Error on disconnecting from server");
			e.printStackTrace();
		}
		finally {
			connected = false;
			connect();
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void send(ClientPacket packet) throws Exception {
		outToServer.writeByte((byte)(packet.getType().ordinal()));
		outToServer.writeShort(packet.getSize());
		packet.write(outToServer);
	}

	public abstract void recv(ServerPacket packet);
}
