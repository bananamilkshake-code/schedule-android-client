package io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import android.util.Log;
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

	public TCPClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	protected void finalize() throws Exception {
		if (connected)
			disconnect();
	}

	public void try_connect() {
		if (connected) 
			return;
	
		try {
			socket = new Socket(host, port);
			outToServer = new DataOutputStream(socket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			reader = new StreamReader(inFromServer, this);
			reader.start();

			connected = true;
		} catch (IOException e) {
			Log.w("TCPClient connect", "Server connection failed", e);
		}
	}

	public void disconnect() {
		try {
			reader.stopGracefully();

			inFromServer.close();
			outToServer.close();

			socket.close();
		} catch (IOException e) {
			Log.w("TCP disconnect", "Error on disconnecting from server", e);
		}
		finally {
			connected = false;
		}
	}

	public void send(ClientPacket packet) {
		try {
			outToServer.writeByte((byte)(packet.getType().ordinal()));
			outToServer.writeShort(packet.getSize());
			packet.write(outToServer);
		} catch (IOException e) {
			Log.w("TCPCLient", "Error on send", e);
			disconnect();
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public abstract void recv(ServerPacket packet);
}
