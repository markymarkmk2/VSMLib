/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.HessianNoCloseSocket;

/**
 *
 * @author Administrator
 */
public interface SocketOwner
{
    HessianNoCloseSocket getSocket();
    void setSocket( HessianNoCloseSocket s );

}
