

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.*;

public class GoBackFtp
{
	// global logger	
	private static final Logger logger = Logger.getLogger("GoBackFtp");

	/**
	 * Constructor to initialize the program 
	 * 
	 * @param windowSize	Size of the window for Go-Back_N in units of segments
	 * @param rtoTimer		The time-out interval for the retransmission timer
	 */
	private int windowSize;
	private int rtoTimer;
	public static ConcurrentLinkedQueue<FtpSegment> segment_queue = new ConcurrentLinkedQueue<FtpSegment>();
	public static InetAddress iAdd; 
	public static int UDP_port_number_for_server;
	public static Thread sendThread;
	public static DatagramSocket clientSocket;
	public static int timeout;
	public GoBackFtp(int windowSize, int rtoTimer)
	{
		this.windowSize = windowSize;
		this.rtoTimer = rtoTimer;
		this.timeout = rtoTimer;
	}


	/**
	 * Send the specified file to the specified remote server
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
			iAdd = InetAddress.getByName(serverName);
			InetSocketAddress sAdd = new InetSocketAddress(iAdd,serverPort);

			// Creating a socket and connecting it to the server to form a connection between host and server
			Socket socket = new Socket();
			socket.connect(sAdd);

			// Opening a UDP Socket
			clientSocket = new DatagramSocket();

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
			UDP_port_number_for_server = in.readInt();
			// initial sequence number
			int initial_sequence_number = in.readInt();		
			// ending handshake process
			
			// Creating a timer

			//Start Sending thread
			SendingThread st= new SendingThread(initial_sequence_number, fileCheck, clientSocket, rtoTimer, windowSize);
			sendThread = new Thread(st);
			sendThread.start();

			ReceivingThread rt = new ReceivingThread(initial_sequence_number,clientSocket,rtoTimer,windowSize);
			Thread recvThread = new Thread(rt);
			recvThread.start();
			
			sendThread.join();
			recvThread.join();			

			//shutdowntransmission timer
			SendingThread.tm.stopTimer();
			socket.close();
			clientSocket.close();
			in.close();
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
} // end of class