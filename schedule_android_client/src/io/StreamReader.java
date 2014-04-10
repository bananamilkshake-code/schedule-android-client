package io;

import io.packet.Packet;
import io.packet.ServerPacket;

import java.io.BufferedReader;

import utility.Utility;

public class StreamReader extends Thread {
	BufferedReader inputStream = null;
	TCPClient owner = null;
	
	boolean stopped = false;

	public StreamReader(BufferedReader inputStream, TCPClient tcpClient) {
		this.inputStream = inputStream;
		this.owner = tcpClient;
	}

	public void recv() throws Exception {
			char[] bufferType = new char[Packet.PACKET_TYPE_BYTE];
			inputStream.read(bufferType, 0, Packet.PACKET_TYPE_BYTE);
			ServerPacket.Type type = ServerPacket.Type.values()[bufferType[0]];

			char[] bufferSize = new char[Packet.PACKET_SIZE_BYTE];
			inputStream.read(bufferSize, 0, Packet.PACKET_SIZE_BYTE);
			Short size = Utility.getShort(bufferSize);

			char[] buffer = new char[size];
			inputStream.read(buffer, 0, size);

			ServerPacket packet = ServerPacket.get(type);
			packet.init(buffer);

			owner.recv(packet);	
	}

	public void stopGracefully() {
		stopped = true;
	}
	
	public void run() {
		while (true) {
			if (stopped)
				return;
			
			try {
				recv();
			} catch (Exception e) {
				System.out.println("Exception on packet recieving: " + e.getLocalizedMessage());
				e.printStackTrace();

				owner.disconnect();
			}		
		}
	}
}
