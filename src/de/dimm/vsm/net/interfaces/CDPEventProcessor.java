/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.CdpEvent;

/**
 *
 * @author Administrator
 */
public interface CDPEventProcessor
{
    boolean process( CdpEvent ev );
}
