package client;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
	private final static int PORT = 12345;
	private DatagramSocket datagramSocket;
	private InetAddress inetAddress;
	private byte[] buffer;
	private int startOffset = 0;	//offset byte counter. Dynamic
	private int packetCounter = 1;
	private File outputFile;	//The file we want to send. May not need
	private byte[] fileContent;	//total number of bytes in file
	private int packetSize; //command line argument for packet size
	private int timeout; //timeout interval to know when to re-send packets
	private double corruptchance; //chance to corrupt the datagrams to send
	private final int DEFAULT_PACKET_SIZE = 200;
	private final int DEFAULT_TIMEOUT = 200;
	private final double DEFAULT_CORRUPTCHANCE = 0;
	
	public Client(DatagramSocket datagramSocket, InetAddress inetAddress, int packetSize, int timeout, double corruptchance) {
		super();
		this.datagramSocket = datagramSocket;
		this.inetAddress = inetAddress;
		this.packetSize = packetSize;
		
		if (packetSize == -1) {
			this.buffer = new byte[DEFAULT_PACKET_SIZE];
		} else {
			this.buffer = new byte[packetSize];
		}
		if (timeout == -1) {
			this.timeout = DEFAULT_TIMEOUT;
		} else {
			this.timeout = timeout;
		}
		if (corruptchance == -1) {
			this.corruptchance = DEFAULT_CORRUPTCHANCE;
		} else {
			this.corruptchance = corruptchance;
		}
	}
	
	/**
	 * Sets the byte[] array to the File we wish to send via the readAllBytes method.
	 * @param args
	 */
	public void setFileContent(String[] args) {
		try {
			this.fileContent = Files.readAllBytes(new File(args[0]).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used to send a requestPacket from Client to Server. 
	 */
	public void sendPacket() {
		//Buffer length decides how many packets we send. If buffer is larger = less #packets. Smaller = more #packets.
		System.out.println("Content Length:" + fileContent.length + "\nbuffer Length: " + buffer.length); //TODO: DEBUG STATEMENT DELETE AFTER
		for (int i = 0; i < Math.floor(fileContent.length/buffer.length); i++) {
			try {
				DatagramPacket requestPacket = new DatagramPacket(fileContent, startOffset, buffer.length, inetAddress, PORT);
				datagramSocket.send(requestPacket);
				printToConsole(requestPacket);	
				startOffset += requestPacket.getLength();
				packetCounter++;
				
				//Create responsePacket and receive it from server. Not really needed for Project 1 but we will fix this for
				//Project 2. Need to change it to an AckPacket. TODO: NEED DISCUSSION
				DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
				datagramSocket.receive(responsePacket);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		//Send last packet whose length < buffer.length and send flag packet
		int lastPackLen = fileContent.length - startOffset;
		DatagramPacket lastDataPacket = new DatagramPacket(fileContent, startOffset, lastPackLen, inetAddress, PORT);
		printToConsole(lastDataPacket);
		startOffset += lastPackLen;
		DatagramPacket flagPacket = new DatagramPacket(new byte[0], 0, inetAddress, PORT);	//TODO: check if we can send 0 length
		try {
			datagramSocket.send(lastDataPacket);		//Send last packet whose length < buffer.length
			System.out.println("Sending flag: " + flagPacket.getData() + " " + flagPacket.getLength()); //TODO: DEBUG STATEMENT DELETE AFTER
			datagramSocket.send(flagPacket);	//Send an empty packet to denote no data left to send. (Our flag)
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method is used to print to console what 
	 * @param request
	 */
	public void printToConsole(DatagramPacket request) {
		System.out.println(String.format("[Packet %d] - [start byte offset]: %d - [end byte offset]: %d", 
				packetCounter, startOffset, startOffset+request.getLength()-1));
	}
	
	// TODO: IMPLEMENT ON SERVER SIDE send the file name string to server there first. May not need
	public void sendFileName() {
		String fileName = outputFile.getName();
		DatagramPacket sendFileNamePacket = new DatagramPacket(fileName.getBytes(), fileName.getBytes().length,
				inetAddress, PORT);
		try {
			datagramSocket.send(sendFileNamePacket);
		} catch (IOException e) {
			System.out.println("FileNamePacket unsuccessful");
			e.printStackTrace();
		}
		System.out.println("Sent file: " + fileName);
	}
	
	/**
	 * Main method. Executes the rest of the program when user inputs the file from cmd line.
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException {
		int packetSize = -1;
		int timeout = -1;
		double corruptchance = -1;
		int i = 0;
		while (i < args.length) {
			if (args[i].equals("-s")) {
				try {
					packetSize = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid packet length specified.");
					return;
				}
				i += 2;
			} else if (args[i].equals("-t")) {
				try {
					timeout = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid timeout length specified.");
					return;
				}
				i += 2;
			} else if (args[i].equals("-d")) {
				try {
					corruptchance = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid packet corruption chance specified.");
					return;
				}
			} else {
				i++;
			}
		}
		
		try (DatagramSocket datagramSocket = new DatagramSocket(0)) {
			InetAddress inetAddress = InetAddress.getLocalHost();
			Client sender = new Client(datagramSocket, inetAddress, packetSize, timeout, corruptchance);
			sender.setFileContent(args);
			sender.sendPacket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
}
