/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public abstract class SnapshotHandle implements Serializable
{
     public abstract Date getCreated();
        
}
