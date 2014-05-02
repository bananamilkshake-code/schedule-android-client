package io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import utility.Utility;
import android.util.Log;
import io.packet.Packet;
import io.packet.ServerPacket;
import io.packet.ClientPacket;

public abstract class TCPClient
{
	public static final int TIMEOUT = (1 * 60 * 1000);
	
	private Socket socket = null;
	private DataOutputStream outToServer = null;
	private BufferedReader inFromServer = null;

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

	public void tryConnect() {
		if (connected) 
			return;
	
		try {
			socket = new Socket(host, port);
			outToServer = new DataOutputStream(socket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			Log.w("TCPClient connect", "Server connection failed", e);
			return;
		}

		connected = true;

		new Thread() {
			@Override
			public void run() {
				while (true) {
					if (!connected)
						return;

					recvPacket();
				}
			}
		}.start();
	}

	public void disconnect() {
		this.clear();

		try {
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

	public void recvPacket() {
		try {
			char[] bufferType = new char[Packet.PACKET_TYPE_BYTE];
			inFromServer.read(bufferType, 0, Packet.PACKET_TYPE_BYTE);
			ServerPacket.Type type = ServerPacket.Type.values()[bufferType[0]];

			char[] bufferSize = new char[Packet.PACKET_SIZE_BYTE];
			inFromServer.read(bufferSize, 0, Packet.PACKET_SIZE_BYTE);
			Short size = Utility.getShort(bufferSize);

			char[] buffer = new char[size];
			inFromServer.read(buffer, 0, size);

			ServerPacket packet = ServerPacket.get(type);
			packet.init(buffer);

			recv(packet);
		} catch (Exception e) {
			Log.e("TCPClient", "Exception on packet recieving", e);
			disconnect();
		}
	}
	
	public void send(ClientPacket packet) {
		if (!connected)
			return;

		try {
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
	protected abstract void clear();
}
