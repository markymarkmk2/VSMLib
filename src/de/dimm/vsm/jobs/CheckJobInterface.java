/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.jobs;

import de.dimm.vsm.fsengine.checks.ICheck;

/**
 *
 * @author mw
 */
public interface CheckJobInterface extends JobInterface{
    ICheck getCheck();
}
