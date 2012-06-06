/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Exceptions;

/**
 *
 * @author mw
 */
public class PathResolveException extends Exception {

    /**
     * Creates a new instance of <code>PathResolveException</code> without detail message.
     */
    public PathResolveException() {
    }


    /**
     * Constructs an instance of <code>PathResolveException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PathResolveException(String msg) {
        super(msg);
    }
}
