/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.preview;

import de.dimm.vsm.records.FileSystemElemNode;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
public interface IPreviewRenderer {
    
    File renderPreviewFile(FileSystemElemNode node ) throws IOException;
    void startRenderPreviewFile(FileSystemElemNode node, IPreviewData data ) throws IOException;
    void renderPreviewDir(FileSystemElemNode node ) throws IOException;
    void clearPreviewFile(FileSystemElemNode node ) throws IOException;
    void clearPreviewDir(FileSystemElemNode node ) throws IOException;  
    boolean canRenderFile( String name );

    File getOutFile( FileSystemElemNode node ) throws IOException;
}
