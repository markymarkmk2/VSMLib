/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import com.caucho.hessian.server.HessianServlet;
import de.dimm.vsm.Utilities.DefaultSSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;


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
    class VsmSslContextFactory extends SslContextFactory
    {
        String keyPwd;

        public VsmSslContextFactory( String keyPwd )
        {
            this.keyPwd = keyPwd;
        }

        @Override
        protected KeyStore loadKeyStore() throws Exception
        {
            // Gibt es eine Keystore Datei ?
            KeyStore ks = super.loadKeyStore(); //To change body of generated methods, choose Tools | Templates.
            if (ks != null)
                return ks;
            
            // Ansontsen aus Jar laden 
            ks = KeyStore.getInstance(KeyStore.getDefaultType());  
            InputStream keystoreStream = DefaultSSLSocketFactory.class.getClassLoader().getResourceAsStream("server.jks"); // note, not getSYSTEMResourceAsStream  
            ks.load(keystoreStream, keyPwd.toCharArray());              
            
            return ks;
        }
        
    }
    
    void initSsl( Server server, int port,  String keystore, String keypwd) throws IOException
    {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(port);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
        
        SslContextFactory sslContextFactory = new SslContextFactory(keypwd);
        if (new File(keystore).exists())
        {
            sslContextFactory.setKeyStorePath( keystore);
            sslContextFactory.setTrustStorePath(keystore);
        }
        else
        {
            Resource keystoreRsrc = Resource.newResource(DefaultSSLSocketFactory.class.getClassLoader().getResource("server.jks"));
            sslContextFactory.setKeyStoreResource( keystoreRsrc);
            sslContextFactory.setTrustStoreResource( keystoreRsrc);
        }
        sslContextFactory.setKeyStorePassword(keypwd);
        sslContextFactory.setKeyManagerPassword(keypwd);
        sslContextFactory.setTrustStorePassword(keypwd);
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setExcludeCipherSuites(
                "SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
        
        //sslContextFactory.setKeyStoreProvider(null);

        // SSL HTTP Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        // SSL Connector
        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory,"http/1.1"),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(port);
        server.addConnector(sslConnector);    
    }
    void initStandard( Server server, int port)
    {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(port);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(32*1024);
        http_config.setResponseHeaderSize(32*1024);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
     
        ServerConnector http = new ServerConnector(server,new HttpConnectionFactory(http_config));
        http.setPort(port);
        http.setIdleTimeout(30000);
        server.addConnector(http);                
    }

    public void start_server(int port, boolean ssl,  String keystore, String keypwd) throws Exception
    {
        // Setup Threadpool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(500);

        // Server
        jetty_server = new Server(threadPool);        
        //jetty_server = new Server(port);
        if (ssl)
        {
            initSsl( jetty_server, port, keystore, keypwd);
            /*SslSocketConnector connector = new SslSocketConnector();
            connector.setPort(port);

            connector.setKeyPassword(keypwd);
            connector.setKeystore(keystore);


            jetty_server.setConnectors(new Connector[]
            {
                connector
            });*/
        }
        else
        {
        
            initStandard( jetty_server, port);
            jetty_server.getConnectors();
            for (int i = 0; i < jetty_server.getConnectors().length; i++)
            {
//                Connector conn = jetty_server.getConnectors()[i];
//                conn.setResponseBufferSize(32*1024);
//                conn.setRequestBufferSize(32*1024);
//
//                System.out.println("RequBuff: " + conn.getRequestBufferSize());
//                System.out.println("RespBuff: " + conn.getResponseBufferSize());
                
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