
/**
 * GzipClient Class
 * 
 * CPSC 441
 * Assignment 1
 * 
 * NAME: FARICA MAGO
 * UCID: 30111924
 *
 */


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.*;


public class GzipClient {

	private static final Logger logger = Logger.getLogger("GzipClient"); // global logger

	/**
	 * Constructor to initialize the class.
	 * 
	 * To Do: you should implement this method.
	 * 
	 * @param serverName	remote server name
	 * @param serverPort	remote server port number
	 * @param bufferSize	buffer size used for read/write
	 */

	private String serverName;
	private int serverPort;
	private int bufferSize;

	public GzipClient(String serverName, int serverPort, int bufferSize)
	{
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.bufferSize = bufferSize;
	}

	
	/**
	 * Compress the specified file using the remote server.
	 * 
	 * To Do: you should implement this method.
	 * 
	 * @param inName		name of the input file to be compressed
	 * @param outName		name of the output compressed file
	 */
	public void gzip(String inName, String outName)
	{
		// Setting up TCP connection
		try
		{
			// Setting up a TCP connaction
			InetAddress iAdd = InetAddress.getByName(this.serverName);
			InetSocketAddress sAdd = new InetSocketAddress(iAdd, this.serverPort);

			// Creating a socket and connecting it to the server to form a connection between host and server
			Socket socket = new Socket();
			try
			{
				socket.connect(sAdd);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Created a SWrite class to transfer file to the server which implements Runnable
			SWriting sWrite = new SWriting(inName,bufferSize,socket);

			// Created a new thread and linked it to the object of SWrite class
			Thread thr1 = new Thread(sWrite);

			// Started the thread of SWrite class
			thr1.start();

			// Created a SRead class to transfer file from server which implements Runnable
			SReading sRead = new SReading(outName,bufferSize,socket);		
			
			//Created a new thread and linked it to the object of SRead class
			Thread thr2 = new Thread(sRead);			

			// Started the thread of SRead class
			thr2.start();

			thr1.join();
			thr2.join();

			// Socket is to be closed
			socket.close();

		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}
	
}
