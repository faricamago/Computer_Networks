

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.*;

public class StopWaitFtp {
	
	private static final Logger logger = Logger.getLogger("StopWaitFtp"); // global logger	

	/**
	 * Constructor to initialize the program 
	 * 
	 * @param timeout		The time-out interval for the retransmission timer, in milli-seconds
	 */

	private int timeout; 

	public StopWaitFtp(int timeout)
	{
		this.timeout = timeout;
	}


	/**
	 * Send the specified file to the remote server
	 * 
	 * @param serverName	Name of the remote server
	 * @param serverPort	Port number of the remote server
	 * @param fileName		Name of the file to be trasferred to the rmeote server
	 */
	public void send(String serverName, int serverPort, String fileName)
	{
		try
		{
			// Setting up a TCP connaction
			InetAddress iAdd = InetAddress.getByName(serverName);
			InetSocketAddress sAdd = new InetSocketAddress(iAdd,serverPort);

			// Creating a socket and connecting it to the server to form a connection between host and server
			Socket socket = new Socket();
			socket.connect(sAdd);

			// Opening a UDP Socket
			DatagramSocket clientSocket = new DatagramSocket();

			// Begin handshake process
			// opening input and output streams using java binary stream clasess
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// client sends:
			// sending local UDP port#
			out.writeInt(clientSocket.getLocalPort());
			// sending file name (UTF format)
			out.writeUTF(fileName);
			//seding file length
			long lengthOfFile = 0;
			File fileCheck  = new File(fileName);
			if(fileCheck.exists())
			{
				lengthOfFile = Files.size(Paths.get(fileName));
			}

			out.writeLong(lengthOfFile);

			// flushing output stream after the handshake as suggested in assignment 4
			out.flush();

			//Client recieves:
			// server UDP port number
			int UDP_port_number_for_server = in.readInt();
			// initial sequence number
			int initial_sequence_number = in.readInt();
		
			// ending handshake process

			
			//initialising sequence number
			int seqNum = initial_sequence_number;

			//initilising ack number
			int ackNum = initial_sequence_number;
			//initialising buffer size
			int size_of_buffer = FtpSegment.MAX_PAYLOAD_SIZE;
			// creating a buffer
			byte[] buffer = new byte[size_of_buffer];
			// input stream to read data from the file
			//FileInputStream input = new FileInputStream(fileCheck);
			InputStream input = new BufferedInputStream(new FileInputStream(fileCheck),FtpSegment.MAX_PAYLOAD_SIZE);
			// bytes read
			int byte_read_out = input.read(buffer);
			Timer timer = new Timer();
			while((byte_read_out!=-1))
        	{
				// creating a segment
				FtpSegment segment = new FtpSegment(seqNum, buffer,byte_read_out);
				//creating a packet
				DatagramPacket packet = FtpSegment.makePacket(segment, iAdd, UDP_port_number_for_server);
				//sending the packet
				clientSocket.send(packet);
				System.out.println("send		"+seqNum);
				//setting up the timer
				TimerTask timertask = new TimeoutHandler(clientSocket, packet, seqNum);
				timer.scheduleAtFixedRate(timertask,this.timeout,this.timeout);
				

				
				//check if the ack was correct ,means, if we got a new sequence number
				while(seqNum == ackNum)
				{
					clientSocket.receive(packet);
					FtpSegment segment2 = new FtpSegment(packet);
					//reading ack
					ackNum = segment2.getSeqNum();
					
				}
				timertask.cancel();
				seqNum = ackNum;
				System.out.println("ack		"+seqNum);	
				byte_read_out = input.read(buffer);
				
        	}
			in.close();
			out.close();
			socket.close();
			clientSocket.close();
			timer.cancel();
			timer.purge();

		}catch(Exception e)
		{

		}
	}

} // end of class