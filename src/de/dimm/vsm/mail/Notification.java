/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.mail;

import de.dimm.vsm.Utilities.VariableResolver;

/**
 *
 * @author Administrator
 */
public interface Notification
{
    NotificationEntry getEntry();
    boolean isFired();
    void release();
    void fire(String extraText, VariableResolver vr);
    String getKey();
    int getFiredCnt();
}
