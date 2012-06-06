/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.log;

import java.io.File;
import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Administrator
 */
public class VSMLogger
{

    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;
    static private FileHandler fileHTML;
    static private Formatter formatterHTML;

    static public void setup()
    {
        try
        {
            // Create Logger
            Logger logger = Logger.getLogger("");
            logger.setLevel(Level.INFO);
            File l = new File("logs");
            if (!l.exists())
            {
                l.mkdir();
            }
            fileTxt = new FileHandler("logs/vsmlog.txt");
            fileHTML = new FileHandler("logs/vsmlog.html");

            // Create txt Formatter
            formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);

            // Create HTML Formatter
            formatterHTML = new VSMLogFormatter();
            fileHTML.setFormatter(formatterHTML);
            logger.addHandler(fileHTML);
        }
        catch (Exception exception)
        {
            System.out.println("Cannot initialize Logging: " + exception.getLocalizedMessage());
            exception.printStackTrace();
        }
    }

    public static Logger get(String name )
    {
        try
        {
            // Create Logger
            Logger logger = Logger.getLogger(name);
            if (logger.getHandlers() == null)
            {
                logger.setLevel(Level.INFO);
                File l = new File("logs");
                if (!l.exists())
                {
                    l.mkdir();
                }
                fileTxt = new FileHandler("logs/name.txt");
                fileHTML = new FileHandler("logs/name.html");

                // Create txt Formatter
                formatterTxt = new SimpleFormatter();
                fileTxt.setFormatter(formatterTxt);
                logger.addHandler(fileTxt);

                // Create HTML Formatter
                formatterHTML = new VSMLogFormatter();
                fileHTML.setFormatter(formatterHTML);
                logger.addHandler(fileHTML);
            }
            return logger;
        }
        catch (Exception exception)
        {
            System.out.println("Cannot initialize Logging: " + exception.getLocalizedMessage());
            exception.printStackTrace();
        }
        return Logger.getLogger("");
    }

}
