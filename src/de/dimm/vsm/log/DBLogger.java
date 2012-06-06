/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.log;

/**
 *
 * @author Administrator
 */
public interface DBLogger
{
    public void saveLog( int level, String key, String addText, Throwable t, int user);

}
