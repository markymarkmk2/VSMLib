/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.CdpEvent;
import de.dimm.vsm.net.CdpTicket;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public interface ServerApi
{
    public static final String SOP_OS_NAME = "os.name";
    public static final String SOP_OS_VER = "os.version";
    public static final String SOP_OS_ARCH = "os.arch";
    public static final String SOP_SV_VER = "sv.version";
    public static final String SOP_IP = "sv.ip";
    public static final String SOP_PORT = "sv.port";

    Properties get_properties();
    boolean alert( String reason, String msg );
    boolean alert( List<String> reason, String msg );


    boolean cdp_call( CdpEvent ev, CdpTicket ticket );
    boolean cdp_call( List<CdpEvent> ev, CdpTicket ticket );
}
