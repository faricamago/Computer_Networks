import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkerThread extends Thread
{
    private final String name_of_my_server;
    private int timeout;
    private Socket sock;    
    private String default_path;
    private SimpleDateFormat format_of_date;
    String httpres;

    public WorkerThread(int timeout,Socket socket)
    {
        this.sock = socket;
        this.timeout = timeout;
        this.format_of_date = new SimpleDateFormat("EEE, dd MMM yyy hh:mm:ss zzz");
        this.name_of_my_server = "Farica's localhost";
    }

    public void run()
    {
        try
        {
            //opening output stream to write HTTP response to the socket
            OutputStream sockO = new BufferedOutputStream(sock.getOutputStream(),4096);

            // setting timer for the client which do not request anything
            sock.setSoTimeout(this.timeout);

            // opening an input stream to read HTTP request from the client
            //Scanner sockI = new Scanner(new InputStreamReader(this.sock.getInputStream()));


            InputStream in = new BufferedInputStream(sock.getInputStream());

            // reading from the input stream socket byte by byte until the end of response header.
            // the response header is (\r\n\r\n) which is being checked by nested if else stements        

            String ss = "";
            int byte_read2 = 0;
            while((byte_read2 = in.read())!=-1)
            {
                ss+=((char)byte_read2);
                if(((char)byte_read2)  == '\r')
                {
                    byte_read2 = in.read();
                    ss+=((char)byte_read2);
                    if(((char)byte_read2) == '\n')
                    {
                        byte_read2 = in.read();
                        ss+=((char)byte_read2);
                        if(((char)byte_read2) == '\r')
                        {
                            byte_read2 = in.read();
                            ss+=((char)byte_read2);
                            if(((char)byte_read2) == '\n')
                            {
                                break;
                            }
                        }
                    }
                }
            }
            String[] ss_ss = ss.split("\r\n",3);
            System.out.println();
            String read_request = ss_ss[0];
            System.out.println(read_request);
            String read_req2 = ss_ss[1];
            System.out.println(read_req2);
            System.out.println(ss_ss[2]);
            
            System.out.println();
            String[] httpreq = read_request.split("\\s{1,}");
            //String[] httpreq = sockI.nextLine().split("\\s{1,}");
            if(!(httpreq[1].equals("/")))
            {
                default_path = httpreq[1];
            }
            else
            {
                default_path = "/index.html";
            }
            String final_path = default_path.substring(1);
            //System.out.println(final_path);
            File file= new File(final_path);
            String today_date = format_of_date.format(new Date(System.currentTimeMillis()));

            
            // condition to check 400 Bad Request
            if(!(httpreq[0].equals("GET")) || !(httpreq[1].indexOf('/')==0) || !(httpreq[2].equals("HTTP/1.1")))
            {
                // formatting the response message
                httpres = "HTTP/1.1 400 Bad Request\r\n"+"Date: "+today_date+"\r\nServer: "+name_of_my_server + "\r\nConnection: close\r\n\r\n";

                System.out.println(httpres);
                //writing response to socket to send it back to the client
                sockO.write(httpres.getBytes("US-ASCII"));
                sockO.flush();
            }
            else if(!file.exists()) // error if file cannot be found
            {   
                // formatting the response message
                httpres = "HTTP/1.1 404 Not Found\r\n"+"Date: "+today_date+"\r\nServer: "+name_of_my_server + "\r\nConnection: close\r\n\r\n";

                System.out.println(httpres);
                //writing response to socket to send it back to the client
                sockO.write(httpres.getBytes("US-ASCII"));
                sockO.flush();
            }
            else // when the reques id totally valid and can be responded back
            {
                long length = file.length();
                String type = Files.probeContentType(file.toPath());
                String date_of_recent_modification = format_of_date.format(file.lastModified());
                httpres = "Http/1.1 200 OK"+"\r\nDate: " + today_date + "\r\nServer: "+name_of_my_server+"\r\nLast-Modified: " + date_of_recent_modification+"\r\nContent-Length:"+length+"\r\nContent-Type: "+type+"\r\nConnection: close\r\n\r\n";
               
                System.out.println(httpres);
                //writing response to socket
                sockO.write(httpres.getBytes("US-ASCII"));
                sockO.flush();

                // opening input stream to read content
                int byte_read = 0;
                InputStream filein = new BufferedInputStream(new FileInputStream(file),4096);
                byte[] buffer = new byte[4096];

                while((byte_read=filein.read(buffer)) != -1)
                {
                    sockO.write(buffer,0,byte_read);
                    sockO.flush();
                }
                filein.close();;
            }
            sockO.close();

        }
        catch(SocketTimeoutException e)
        {
            String today_date = format_of_date.format(new Date(System.currentTimeMillis()));
            String httpres = "HTTP/1.1 408 Request Timeout\r\n"+"Date: "+today_date+"\r\nServer: "+name_of_my_server + "\r\nConnection: close\r\n\r\n";

            try
            {
                OutputStream sockO = new BufferedOutputStream(sock.getOutputStream(),4096);
                System.out.println(httpres);
                //writing response to socket to send it back to the client
                sockO.write(httpres.getBytes("US-ASCII"));
                sockO.flush();
                sockO.close();
            } catch (IOException a) {
                // TODO Auto-generated catch block
                //a.printStackTrace();
            }    
                     
        }catch(IOException e)
        {
           // e.printStackTrace();
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
        
    }
}

