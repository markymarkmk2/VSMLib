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
    long getAttrIdx();
    File getPreviewImageFile();
    void setPreviewImageFile(File file);
    IMetaData getMetaData();  
    String getName();
}
