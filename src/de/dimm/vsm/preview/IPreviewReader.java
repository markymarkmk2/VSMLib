/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.preview;

import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.records.FileSystemElemAttributes;
import de.dimm.vsm.records.FileSystemElemNode;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public interface IPreviewReader {
    IPreviewData getPreviewDataFile( FileSystemElemNode node, FileSystemElemAttributes attr) throws IOException;
    IPreviewData getPreviewDataFileAsync( FileSystemElemNode node, FileSystemElemAttributes attr, Properties props ) throws IOException;
    List<IPreviewData> getPreviewDataDir(  FileSystemElemNode node) throws IOException, SQLException;        
    List<IPreviewData> getPreviewDataDirAsync(  FileSystemElemNode node, Properties props) throws IOException, SQLException;        
    public List<IPreviewData> getPreviews(  List<RemoteFSElem> path, Properties props ) throws IOException, SQLException;   
    List<IPreviewData> getPreviewStatus( List<IPreviewData> list) throws IOException;        
    void abortPreview( List<IPreviewData> list) throws IOException;        
    void deletePreviewDataFile( FileSystemElemNode node, FileSystemElemAttributes attr) throws IOException;
}


