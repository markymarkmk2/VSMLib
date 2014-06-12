/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.MMapi;

import de.dimm.vsm.log.LogManager;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Administrator
 */
public class MMapi
{

    String host;
    int port;
    Socket sock;
    public static final int CONNECT_TO = 5000;

    public MMapi( String host, int port )
    {
        this.host = host;
        this.port = port;
    }

    public int getPort()
    {
        return port;
    }

    public String getHost()
    {
        return host;
    }

    

    public boolean connect() throws IOException
    {
        if (isConnected())
            disconnect();

        sock = new Socket();
        try
        {
            sock.connect(new InetSocketAddress(host, port), CONNECT_TO);
        }
        catch (SocketTimeoutException socketTimeoutException)
        {
            return false;
        }
        sock.setTcpNoDelay(true);
        sock.setSoTimeout(0);


        MMAnswer a = sendMM("DiMMalive?");

        
        return (a != null && a.code == 0);
    }

    public boolean isConnected()
    {
        return sock != null && sock.isConnected();
    }

    public void disconnect()
    {
        try
        {
            sock.close();
        }
        catch (IOException iOException)
        {
        }
        sock = null;
    }
    public MMAnswer sendMM( String string) throws IOException
    {
        return sendMM(string, 0);
    }
    public MMAnswer sendMM( String string, int timeout )
    {
        try
        {
            if (!string.endsWith("\r\n"))
                string = string + " \r\n";

            return _sendMM(string, timeout);
        }
        catch (Exception e)
        {
            LogManager.msg_comm(LogManager.LVL_ERR, "Koomunikation mit MM schlug fehl: " + e.getMessage());
        }
        return null;
    }
    public MMAnswer _sendMM( String string, int timeout ) throws SocketException, IOException
    {
        
        sock.setSoTimeout(timeout);
        sock.getOutputStream().write(string.getBytes());
        sock.getOutputStream().flush();

        StringBuilder sb = new StringBuilder();

        BufferedInputStream is = new BufferedInputStream(sock.getInputStream());

        char ch = (char) is.read();
        sb.append(ch);
        while (is.available() > 0)
        {
            sb.append((char) is.read());
        }
        int maxWaitms = 30000;
        boolean ready = false;
        while (!ready && maxWaitms > 0)
        {
            while (is.available() > 0)
            {
                sb.append((char) is.read());
            }

            int endIdx = sb.lastIndexOf("|OK|");
            if (endIdx > 0 && endIdx == sb.length() - 4)
            {
                ready = true;
                break;
            }

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException interruptedException)
            {
            }
            maxWaitms -= 100;
        }
        if (!ready)
        {
           throw new IOException("Timeout in receive");
        }

        String ret = sb.toString();
        int idx = ret.indexOf(':');
        if (ret.length() < 4 || idx < 0 || idx +1 >  ret.length() - 4 )
            throw new IOException("Ungültige Antwort von MM: " + sb.toString() );


        int r = Integer.parseInt(ret.substring(0, idx));
        String rest = ret.substring(idx + 1, ret.length() - 4 ).trim();
        MMAnswer ma = new MMAnswer( r, rest );

        return ma;
    }

    public static ArrayList<String> getAnswerList( MMAnswer result )
    {
        String[] sa = result.txt.split("\n");
        ArrayList<String> ret = new ArrayList<String>();

        for (int i = 0; i < sa.length; i++)
        {
            ret.add(sa[i].trim());
        }
        return ret;
    }


    public JobStatus getJobState( boolean hasJobId, long jobId ) throws IOException
    {
        JobStatus js = null;
        MMAnswer ma = sendMM("list_tasks");
        if (ma == null)
            throw new IOException("Fehler beim Abruf der Tasks");

        List<String> tasks = MMapi.getAnswerList(ma);

        if (!hasJobId && tasks.size() > 1)
            throw new IOException("Mehr als eine Task aktiv");

        if (!hasJobId)
            js = new JobStatus(tasks.get(0));
        else
        {
            for (int i = 0; i < tasks.size(); i++)
            {
                String string = tasks.get(i);
                JobStatus _js = new JobStatus(string);
                if (_js.taskId == jobId)
                {
                    js = _js;
                    break;
                }
            }
        }
        return js;
    }

    
}
