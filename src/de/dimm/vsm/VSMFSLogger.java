package de.dimm.vsm;

import de.dimm.vsm.log.CompressingDailyRollingFileAppender;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class VSMFSLogger
{

    private final static Logger log = Logger.getLogger("VSMFS");
    static final String logName = "vsm.log";

    static
    {        
        log.setLevel(Level.DEBUG);
        Layout lay = new SimpleLayout();
        
        try {
            CompressingDailyRollingFileAppender app = new CompressingDailyRollingFileAppender(lay, logName, "'.'yyyy-MM-dd");
            File f = new File(logName);
            app.setFile(f.getAbsolutePath());
            log.addAppender(app);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
        
        
    }

    public static void setLevel(Level l)
    {
        log.setLevel(l);
    }
    public static Logger getLog()
    {
        return log;
    }
}
