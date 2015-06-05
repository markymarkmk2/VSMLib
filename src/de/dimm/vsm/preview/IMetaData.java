/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.preview;

import java.util.Collection;

/**
 *
 * @author Administrator
 */
public interface IMetaData {  
    public static final String ATTR_RENDER_STATE = "__RenderState";
    public static final String ATTR_RENDER_ERROR = "__RenderError";
    public static final String RENDER_STATE_BUSY = "busy";
    public static final String RENDER_STATE_DONE = "done";
    public static final String RENDER_STATE_ERROR = "err";
    public static final String RENDER_STATE_TIMEOUT = "timeout";
    
    String getAttribute(String key);
    void setAttribute(String key, String value);
    Collection<String> getKeys();

    boolean isBusy();

    boolean isDone();

    boolean isError();

    boolean isTimeout();

    void setBusy();

    void setDone();

    void setError();

    void setTimeout();
    
    void setError( String txt);
}
