/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.preview;

import java.io.File;

/**
 *
 * @author Administrator
 */
public interface IPreviewData {   
    public static final String DELETE = "delete";
    public static final String RECURSIVE = "recursive";
    public static final String NOT_CACHED = "notCached";
    public static final String ONLY_CACHED = "onlyCached";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    
    long getAttrIdx();
    File getPreviewImageFile();
    void setPreviewImageFile(File file);
    IMetaData getMetaData();  
    String getName();
}
