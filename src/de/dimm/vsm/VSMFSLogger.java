package de.dimm.vsm;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class VSMFSLogger
{

    private final static Logger log = Logger.getLogger("VSMFS");

    static
    {        
        log.setLevel(Level.INFO);
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
