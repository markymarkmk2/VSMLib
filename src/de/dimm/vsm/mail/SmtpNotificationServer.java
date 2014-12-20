/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.mail;

import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPTransport;
import de.dimm.vsm.Utilities.DefaultTextProvider;
import de.dimm.vsm.Utilities.DefaultVariableResolver;
import de.dimm.vsm.Utilities.VariableResolver;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.records.MailGroup;
import de.dimm.vsm.records.MailNotifications;
import de.dimm.vsm.records.SmtpLoginData;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author Administrator
 */
public class SmtpNotificationServer extends NotificationServer
{
    private final String DEFAULT_SSL_FACTORY = "de.dimm.vsm.Utilities.DefaultSSLSocketFactory";
    private final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    Session session;
    
    private SmtpNotificationServer( )
    {
        Properties props = new Properties();

        //props.put("mail.smtp.starttls.enable", useTLS );
        props.put("mail.transport.protocol", "smtp");


        session = Session.getInstance(props);
    }

    public static NotificationServer createSmtpNotificationServer()
    {
        return new SmtpNotificationServer();
    }
    
    public void addEntry( NotificationEntry en, MailGroup gr )
    {

        Notification no = new SmtpNotification(en, gr, this);

        if (!existsNotification( no ))
        {
            addNotification(no);
        }
    }
    public String get_ssl_socket_classname( SmtpLoginData smtpData )
    {
        if (smtpData.isSsl())
        {
            return SSL_FACTORY;
        }
        else
        {
            return DEFAULT_SSL_FACTORY;
        }
    }


    void initMailSettings(SmtpLoginData smtpData)
    {
        Properties props = session.getProperties();
        String protocol = "smtp";
        if (smtpData.isTls() && !smtpData.isSsl())
        {
            props.put("mail." + protocol + ".starttls.enable", "true");
            props.put("mail." + protocol + ".socketFactory.fallback", "true");
        }
        else if (smtpData.isTls() && smtpData.isSsl())
        {
            props.put("mail." + protocol + ".starttls.enable", "true");
            props.put("mail." + protocol + ".socketFactory.fallback", "false");
        }
        else if (smtpData.isSsl())
        {
            protocol = protocol + "s";
            props.put("mail." + protocol + ".socketFactory.port", smtpData.getServerport());
        }
        else
        {
            // DO NOT ALLOW TLS
            props.put("mail." + protocol + ".starttls.enable", "false");
        }
    }

    public void rawFireNotification( NotificationEntry n, MailGroup group, String extraText, VariableResolver vr ) throws AddressException, MessagingException
    {
        MimeMessage msg =createMessage(n, group, extraText, vr);
        sendMessage(msg, group);
    }

    
    void fireNotification( NotificationEntry n, MailGroup group, String extraText, VariableResolver vr )
    {
        if (n == null || group == null || group.getSmtpdata() == null )
            return;

        if (group.getSmtpdata().getServerip() == null || group.getSmtpdata().getServerip().isEmpty())
            return;
        
        try
        {            
            rawFireNotification(n, group, extraText, vr);            
        }
        catch (Exception messagingException)
        {           
                LogManager.msg_system(LogManager.LVL_ERR, DefaultTextProvider.Txt("Senden der Notification schlug fehl") +
                        " " +  n.getKey() + ": " + group.getSmtpdata().getServerip() + ": " +  messagingException.getMessage());           
        }
    }

    void sendMessage( MimeMessage msg, MailGroup group ) throws NoSuchProviderException, MessagingException
    {
        initMailSettings( group.getSmtpdata() );
        session.getProperties();

        SMTPTransport tr = (SMTPTransport) session.getTransport();
        tr.setStartTLS(group.getSmtpdata().isTls());

        String smtpUser = group.getSmtpdata().getUsername();
        String smtpPwd = group.getSmtpdata().getUserpwd();

        if (smtpUser == null || smtpUser.isEmpty())
        {
            tr.connect(group.getSmtpdata().getServerip(), group.getSmtpdata().getServerport(), null, null);
        }
        else
        {
            tr.connect(group.getSmtpdata().getServerip(), group.getSmtpdata().getServerport(), smtpUser, smtpPwd);
        }

        msg.saveChanges();
        tr.sendMessage(msg,  msg.getAllRecipients());
        tr.close();

    }

    MimeMessage createMessage(  NotificationEntry n, MailGroup group, String extraText, VariableResolver vr) throws AddressException, MessagingException
    {
        MimeMessage msg = new SMTPMessage(session);
        InternetAddress from = new InternetAddress(group.getSmtpdata().getSmtpfrom());
        Address[] fromArr =
        {
            from
        };
        msg.addFrom(fromArr);
        List<String> ma = group.getEmails();
        for (int i = 0; i < ma.size(); i++)
        {
            String mail = ma.get( i );
            InternetAddress in = null;
            try
            {
                in = new InternetAddress(mail);
            }
            catch (AddressException addressException)
            {
                LogManager.msg_system(LogManager.LVL_WARN, "Ignoriere ungültige Mailadresse" + " " + mail);
            }

            if (in == null)
                continue;

            msg.addRecipient(RecipientType.TO, in );
        }
        if (msg.getRecipients(RecipientType.TO).length == 0)
            return null;
        
        String subject = "VSM " + n.getLeveltext() + ": " + DefaultVariableResolver.resolveVariableText( n.getSubject(vr) );
        String mailText = DefaultVariableResolver.resolveVariableText( n.getText(vr) );
        mailText += "\n" + extraText;

        LogManager.msg_system(LogManager.LVL_DEBUG, DefaultTextProvider.Txt("Sende Notification") + 
                        " " +  n.getKey() + ": " + group.getSmtpdata().getServerip() + ": " + subject + ": " + mailText);    

        msg.setSubject(subject);

        // CREATE MESSAGE PART
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(mailText);

        if (mailText.contains("<body") || mailText.contains("<body"))
        {
            messageBodyPart.addHeader("Content-Type", "text/html;charset=\"iso-8859-1\"");
            messageBodyPart.addHeader("Content-Transfer-Encoding", "quoted-printable");
        }
        messageBodyPart.setDisposition(MimeBodyPart.INLINE);


        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);


        msg.setContent(multipart);

        return msg;
    }

    private void addRecursiveNotificationEntry( NotificationEntry ne, MailGroup group )
    {
        if (ne.isGroupNotificationEntry())
        {
            String[] subKeys = ne.getSubKeys();
            for (int j = 0; j < subKeys.length; j++)
            {
                String subKey = subKeys[j];
                NotificationEntry subNe = getNotificationEntry(subKey);
                if (subNe != null)
                {
                    addRecursiveNotificationEntry( subNe, group );
                }
            }
        }
        else
        {
            addEntry(ne, group);
        }
    }

    @Override
    public void loadNotifications(GenericEntityManager em)
    {
        synchronized(notificationMap)
        {
            clear();

            try
            {
                List<MailNotifications> list = em.createQuery("select T1 from MailNotifications T1", MailNotifications.class);
                for (int i = 0; i < list.size(); i++)
                {
                    MailNotifications mailNotifications = list.get(i);

                    NotificationEntry ne = getNotificationEntry(mailNotifications.getKeyString());
                    if (ne != null)
                    {
                        // ALLOW RECURSIVE NOTIFICATION ENTRIES (HF_GROUP_ERROR CONTAINS BA_GROUP_ERROR CONTAINS ....
                        addRecursiveNotificationEntry( ne, mailNotifications.getGroup() );
                    }
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(SmtpNotificationServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }




}
