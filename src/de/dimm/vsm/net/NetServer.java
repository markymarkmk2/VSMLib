/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import com.caucho.hessian.server.HessianServlet;
import org.eclipse.jetty.server.Connector;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 * @author Administrator
 */
public class NetServer
{
    Server jetty_server;
    ServletContextHandler context;

    public NetServer()
    {
        context = new ServletContextHandler( ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.setResourceBase(".");



    }


    public void start_server(int port, boolean ssl,  String keystore, String keypwd) throws Exception
    {
        jetty_server = new Server(port);
        if (ssl)
        {
            SslSocketConnector connector = new SslSocketConnector();
            connector.setPort(port);

            connector.setKeyPassword(keypwd);
            connector.setKeystore(keystore);


            jetty_server.setConnectors(new Connector[]
            {
                connector
            });
        }
        else
        {
            jetty_server.getConnectors();
            for (int i = 0; i < jetty_server.getConnectors().length; i++)
            {
                Connector conn = jetty_server.getConnectors()[i];
                conn.setResponseBufferSize(32*1024);
                conn.setRequestBufferSize(32*1024);

                System.out.println("RequBuff: " + conn.getRequestBufferSize());
                System.out.println("RespBuff: " + conn.getResponseBufferSize());
                
            }
        }


        jetty_server.setHandler(context);

        try
        {
            jetty_server.start();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }



    public void stop_server()
    {
        try
        {
            jetty_server.stop();
            jetty_server.join();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void join_server()
    {
        try
        {
            jetty_server.join();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }


    public void addServlet( String web_context, HessianServlet servlet)
    {
        ServletHolder servletHolder = new ServletHolder();
        servletHolder.setServlet(servlet);
        context.addServlet(servletHolder, "/" + web_context);
    }
    public void addServletHolder( String web_context, ServletHolder servletHolder)
    {
        context.addServlet(servletHolder, "/" + web_context);
    }

}