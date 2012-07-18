/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.Utilities;

import com.ning.compress.lzf.LZFInputStream;
import com.ning.compress.lzf.LZFOutputStream;
import de.dimm.vsm.CS_Constants;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Administrator
 */
public class ZipUtilities
{

    private static final int BUFFSIZE = 2048;
    int file_count;
    long total_size;
    boolean abort;

    public void do_abort()
    {
        abort = true;
    }

    public boolean is_aborted()
    {
        return abort;
    }

    private void notify_listeners_state( int state, String status )
    {
        for (int j = 0; j < listeners.size(); j++)
        {
            ZipListener zipListener = listeners.get(j);
            zipListener.new_status(state, status);
        }
    }

    private void notify_listeners_file( String source_file )
    {
        for (int j = 0; j < listeners.size(); j++)
        {
            ZipListener zipListener = listeners.get(j);
            zipListener.act_file_name(source_file);
        }
    }

    private void notify_listeners_total_percent( int pc )
    {
        for (int j = 0; j < listeners.size(); j++)
        {
            ZipListener zipListener = listeners.get(j);
            zipListener.total_percent(pc);
        }
    }

    private void notify_listeners_file_percent( int pc )
    {
        for (int j = 0; j < listeners.size(); j++)
        {
            ZipListener zipListener = listeners.get(j);
            zipListener.file_percent(pc);
        }
    }
    ArrayList<ZipListener> listeners;

    public ZipUtilities()
    {
        listeners = new ArrayList<ZipListener>();
        file_count = 0;
        total_size = 0;
    }

    public void addListener( ZipListener l )
    {
        listeners.add(l);
    }

    public void removeListener( ZipListener l )
    {
        listeners.remove(l);
    }

    public boolean zip( String path, String target_file )
    {
        return zip(path, target_file, null);
    }

    public boolean zip( String path, String target_file, String[] exclude_list )
    {
        try
        {
            //create a ZipOutputStream to zip the data to 
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(target_file));

            File f = new File(path);
            if (f.isDirectory())
            {
                zipDir(f.getAbsolutePath(), f.getAbsolutePath(), zos, exclude_list);
            }
            else
            {
                zipFile(f.getParentFile().getAbsolutePath(), f.getPath(), zos);
            }

            //close the stream 
            zos.close();
        }
        catch (Exception e)
        {
            notify_listeners_state(ZipListener.ST_ERROR, e.getMessage());
            return false;
        }
        return true;
    }

    public void zipDir( String start_path, String dir2zip, ZipOutputStream zos, String[] exclude_list ) throws FileNotFoundException, IOException, Exception
    {
        //create a new File object based on the directory we have to zip File    
        File zipDir = new File(dir2zip);
        //get a listing of the directory content 
        String[] dirList = zipDir.list();

        //loop through dirList, and zip the files 
        for (int i = 0; i < dirList.length; i++)
        {
            File f = new File(zipDir, dirList[i]);

            // HANDLE EXCLUDES
            if (exclude_list != null)
            {
                String rel_path = f.getAbsolutePath().substring(start_path.length() + 1);

                if (File.separatorChar != '/')
                {
                    rel_path.replace(File.separatorChar, '/');
                }

                int j = 0;
                for (j = 0; j < exclude_list.length; j++)
                {
                    String string = exclude_list[j];
                    if (string.equals(rel_path))
                    {
                        break;
                    }
                }

                if (j != exclude_list.length)
                {
                    continue;
                }
            }

            if (f.isDirectory())
            {
                String filePath = f.getPath();
                zipDir(start_path, filePath, zos, exclude_list);
                //loop again 
                continue;
            }
            if (abort)
            {
                throw new Exception("Aborted");
            }

            zipFile(start_path, f.getPath(), zos);
        }
    }

    public void zipFile( String start_path, String file, ZipOutputStream zos ) throws FileNotFoundException, IOException, Exception
    {
        byte[] readBuffer = new byte[64 * 1024];
        int bytesIn = 0;
        File f = new File(file);
        long file_size = f.length();

        // NOTIFY
        notify_listeners_file(f.getName());

        InputStream fis = null;


        try
        {
            //create a FileInputStream on top of f
            fis = new BufferedInputStream(new FileInputStream(f), CS_Constants.STREAM_BUFFER_LEN);
            //create a new zip entry
            String e_name = f.getAbsolutePath().substring(start_path.length() + 1);

            // REPLACE BS WITH SLASH
            if (System.getProperty("os.name").startsWith("Win"))
            {
                e_name = e_name.replace('\\', '/');
            }

            ZipEntry anEntry = new ZipEntry(e_name);
            //place the zip entry in the ZipOutputStream object
            anEntry.setTime(f.lastModified());
            zos.putNextEntry(anEntry);


            int act_size = 0;
            while ((bytesIn = fis.read(readBuffer)) != -1)
            {
                if (abort)
                {
                    throw new Exception("Aborted");
                }

                zos.write(readBuffer, 0, bytesIn);

                if (file_size > 0)
                {
                    act_size += bytesIn;
                    int percent = (int) ((act_size * 100) / file_size);
                    notify_listeners_file_percent(percent);
                }
            }
        }
        catch (IOException exception)
        {
            throw exception;
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException iOException)
                {
                }
            }
        }
    }

    public final void copyInputStream( InputStream in, OutputStream out, long file_size )
            throws IOException, Exception
    {
        byte[] buffer = new byte[BUFFSIZE];
        int len;
        long act_file_size = 0;

        while ((len = in.read(buffer)) >= 0)
        {
            if (abort)
            {
                throw new Exception("Aborted");
            }
            out.write(buffer, 0, len);

            if (file_size > 0)
            {
                act_file_size += len;
                int percent = (int) ((act_file_size * 100) / file_size);
                notify_listeners_file_percent(percent);
            }
        }

        in.close();
        out.close();
    }

    public boolean unzip( String path, String source_file )
    {
        return unzip(path, source_file, null);
    }

    public boolean unzip( String path, String source_file, String select_file )
    {
        Enumeration entries;
        ZipFile zipFile;
        long act_total_size = 0;

        notify_listeners_state(ZipListener.ST_STARTED, "Unzipping " + source_file + " to " + path);

        try
        {
            zipFile = new ZipFile(source_file);

            entries = zipFile.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                // DE WE WANT TO EXTRACT A SINGLE FILE ?
                if (select_file != null)
                {
                    if (select_file.compareTo(entry.getName()) != 0)
                    {
                        continue;
                    }
                }

                File f = new File(entry.getName());


                // CREATE DIRS ON THE FLY
                File targ_path = null;
                if (path.equals("."))
                {
                    targ_path = new File(entry.getName());
                }
                else
                {
                    targ_path = new File(path + File.separator + entry.getName());
                }

                if (entry.isDirectory())
                {
                    targ_path.mkdirs();
                    continue;
                }

                // NOTIFY
                notify_listeners_file(entry.getName());


                // RELA PATH ?

                File parent_path = targ_path.getParentFile();
                if (!parent_path.exists())
                {
                    parent_path.mkdirs();
                }

                // COPY OUT DATA
                copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(targ_path)),
                        entry.getSize());

                // NOTIFY
                if (total_size > 0)
                {
                    act_total_size += entry.getSize();
                    int percent = (int) ((act_total_size * 100) / total_size);
                    notify_listeners_total_percent(percent);
                }

                // SET FILESTAT
                targ_path.setLastModified(entry.getTime());
            }

            zipFile.close();
        }
        catch (Exception ioe)
        {
            notify_listeners_state(ZipListener.ST_ERROR, ioe.getMessage());
            return false;
        }
        return true;
    }

    public boolean list( String source_file )
    {
        Enumeration entries;
        ZipFile zipFile;


        try
        {
            zipFile = new ZipFile(source_file);

            entries = zipFile.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File f = new File(entry.getName());

                if (entry.isDirectory())
                {

                    // This is not robust, just for demonstration purposes.
                    System.out.println("Dir:  " + f.getPath());
                    continue;
                }
                System.out.println("File: " + f.getPath());
            }

            zipFile.close();
        }
        catch (Exception ioe)
        {
            notify_listeners_state(ZipListener.ST_ERROR, ioe.getMessage());
            return false;
        }
        return true;
    }

    public boolean read_stat( String source_file )
    {
        Enumeration entries;
        ZipFile zipFile;

        file_count = 0;
        total_size = 0;

        try
        {
            zipFile = new ZipFile(source_file);

            entries = zipFile.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File f = new File(entry.getName());

                if (entry.isDirectory())
                {
                    continue;
                }
                file_count++;
                total_size += entry.getSize();
            }

            zipFile.close();
        }
        catch (Exception ioe)
        {
            file_count = 0;
            total_size = 0;
            notify_listeners_state(ZipListener.ST_ERROR, ioe.getMessage());
            return false;
        }
        return true;
    }

    public static String compress( String stream )
    {
        if (stream == null)
        {
            return null;
        }
        if (stream.length() == 0)
        {
            return stream;
        }

        ByteArrayInputStream fis = null;
        ByteArrayOutputStream fos = null;
        String erg = "";

        try
        {
            fis = new ByteArrayInputStream(stream.getBytes("UTF-8"));
            fos = new ByteArrayOutputStream();

            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry("CS");  // COMPRESSED STRING
            ze.setTime(0);
            zos.putNextEntry(ze);
            final int BUFSIZ = 4096;
            byte inbuf[] = new byte[BUFSIZ];
            int n;
            while ((n = fis.read(inbuf)) != -1)
            {
                zos.write(inbuf, 0, n);
            }
            fis.close();
            fis = null;
            zos.close();

            erg = new String(Base64.encodeBase64(fos.toByteArray()), "UTF-8");
        }
        catch (IOException iOException)
        {
            // CANNOT HAPPEN
            return null;
        }

        return erg;
    }

    public static String uncompress( String stream ) throws IllegalArgumentException
    {
        if (stream == null)
        {
            return null;
        }
        if (stream.length() == 0)
        {
            return stream;
        }

        ByteArrayInputStream fis = null;
        ByteArrayOutputStream fos = null;
        String erg = "";

        try
        {
            byte[] data = Base64.decodeBase64(stream.getBytes("UTF-8"));
            fis = new ByteArrayInputStream(data);
            fos = new ByteArrayOutputStream();
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            if (ze.getName().compareTo("CS") != 0)
            {
                throw new IllegalArgumentException("Data is not a CS data element");
            }
            final int BUFSIZ = 4096;
            byte inbuf[] = new byte[BUFSIZ];
            int n;
            while ((n = zis.read(inbuf, 0, BUFSIZ)) != -1)
            {
                fos.write(inbuf, 0, n);
            }
            zis.close();
            fis = null;
            fos.close();

            erg = new String(fos.toByteArray(), "UTF-8");
        }
        catch (IOException iOException)
        {
            // CANNOT HAPPEN
            return null;
        }

        return erg;
    }

    public static String deflateString( String data )
    {
        try
        {
            byte[] dataByte = data.getBytes("UTF-8");

            return deflateData(dataByte);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException)
        {
            // CANNOT HAPPEN
        }
        return null;
    }

    public static String inflateString( String str )
    {
        try
        {

            byte[] data = inflateData(str);
            if (data == null)
                return null;
            
            String s = new String(data, "UTF-8");
            return s;
        }
        catch (UnsupportedEncodingException unsupportedEncodingException)
        {
            // CANNOT HAPPEN
        }
        return null;
    }

    public static String deflateData( byte[] dataByte )
    {
        try
        {
            Deflater def = new Deflater();
            def.setLevel(Deflater.BEST_COMPRESSION);
            def.setInput(dataByte);
            def.finish();
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream(dataByte.length);
            byte[] buf = new byte[1024];
            while (!def.finished())
            {
                int compByte = def.deflate(buf);
                byteArray.write(buf, 0, compByte);
            }

            try
            {
                byteArray.close();
            }
            catch (IOException ioe)
            {
                System.out.println("When we will close straem error : " + ioe);
            }

            byte[] comData = byteArray.toByteArray();

            String data = new String(Base64.encodeBase64(comData), "UTF-8");
            return data;
        }
        catch (UnsupportedEncodingException unsupportedEncodingException)
        {
            // CANNOT HAPPEN
        }

        return null;
    }

    public static byte[] inflateData( String s )
    {
        try
        {
            byte[] input = Base64.decodeBase64(s);


            Inflater inflator = new Inflater();
            inflator.setInput(input);

            ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
            byte[] buf = new byte[1024];

            try
            {
                while (true)
                {
                    int count = inflator.inflate(buf);
                    if (count == 0 && inflator.finished())
                    {
                        break;
                    }
                    else if (count == 0)
                    {
                        throw new RuntimeException("bad zip data, size:"
                                + input.length);
                    }
                    else
                    {
                        bos.write(buf, 0, count);
                    }
                }
            }
            catch (Throwable t)
            {
                throw new RuntimeException(t);
            }
            finally
            {
                inflator.end();
            }
            return bos.toByteArray();

        }
        catch (Exception exc)
        {
            // BAD ZIP DATA
        }

        return null;
    }

    public static String toBase64( String stream )
    {
        String erg = null;
        try
        {
            erg = new String(Base64.encodeBase64(stream.getBytes("UTF-8")), "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            //cannot_happen
        }
        return erg;
    }

    public static String toBase64( byte[] data )
    {
        String erg = null;
        try
        {
            erg = new String(Base64.encodeBase64(data), "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            //cannot_happen
        }
        return erg;
    }

    public static String fromBase64( String stream ) throws UnsupportedEncodingException
    {
        String erg;
        erg = new String(Base64.decodeBase64(stream.getBytes("UTF-8")), "UTF-8");
        return erg;
    }

    public static byte[] fromBase64toByte( String stream ) throws UnsupportedEncodingException
    {
        return Base64.decodeBase64(stream.getBytes("UTF-8"));
    }

    static void get_clen( String in )
    {
        System.out.println("Len unc:" + in.length());
        String cin = ZipUtilities.compress(in);
        System.out.println("Len   c:" + cin.length());
        String din = ZipUtilities.deflateString(in);
        System.out.println("Len   d:" + din.length());
    }

    public static void main( String[] args )
    {
        boolean ret = false;

        String in = "askdjhaskjhdkajsawieoiu claikrliahd.Ã¶Ã¤Ã¼awid.,, awij13874b#''''\"\\*";
        in += in;
        in += in;
        in += in;

        try
        {
            String cin = ZipUtilities.compress(in);

            String cout = ZipUtilities.uncompress(cin);

            ret = (in.compareTo(cout) == 0);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            illegalArgumentException.printStackTrace();
        }
        try
        {
            get_clen("askdjhaskjhdkajsawieoiu");
            get_clen("askdjhaskjhdkajsawieoiuaskdjhaskjhdkajsawieoiuaskdjhaskjhdkajsawieoiu");
            get_clen("Thus is a onnonrepititiveText with lots of hdtus jusd78qwnx09q34nxqn9zwrgfg98473652920nxpojfhf56aölc0998rjq2jqwd9pfuqnmx xi4081308x1r");
            get_clen(ZipUtilities.toBase64("Thus is a onnonrepititiveText with lots of hdtus jusd78qwnx09q34nxqn9zwrgfg98473652920nxpojfhf56aölc0998rjq2jqwd9pfuqnmx xi4081308x1r"));

        }
        catch (Exception e)
        {
        }
        /*
        ZipUtilities zu = new ZipUtilities();

        if (args[0].equals("-d"))
        {
        ret = zu.unzip(args[1], args[2]);
        }
        else if (args[0].equals("-l"))
        {
        ret = zu.list(args[1]);
        }
        else
        {
        ret = zu.zip(args[0], args[1]);
        }
         */
        System.exit(ret == false ? 1 : 0);

    }

    public static byte[] lzf_compressblock( byte[] data )
    {
        OutputStream os = null;
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            os = new LZFOutputStream(bos);
            os.write(data);
            os.close();

            data = bos.toByteArray();
        }
        catch (IOException iOException)
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException iOException1)
                {
                }
            }
            return null;
        }
        
        return data;
    }
    public static byte[] lzf_decompressblock( byte[] data )
    {
        LZFInputStream lzis = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try
        {
            lzis = new LZFInputStream(bis);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            lzis.readAndWrite(bos);
            lzis.close();

            data = bos.toByteArray();
            bos.close();
        }
        catch (IOException iOException)
        {
            if (lzis != null)
            {
                try
                {
                    lzis.close();
                }
                catch (IOException iOException1)
                {
                }
            }
            return null;
        }

        return data;
    }
}
