/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author Administrator
 */
public class CdpEvent implements Serializable
{
    public static final int CDP_SYNC_DIR = 1;
    public static final int CDP_SYNC_DIR_RECURSIVE = 2;
    public static final int CDP_DELETE_DIR_RECURSIVE = 3;
    public static final int CDP_FULLSYNC = 5;


    
    RemoteFSElem elem;
    int mode;
    InetAddress client;
    long lastTouched;
    long created;
    String path;    

    
    public static CdpEvent createDirSyncRecursiveEvent( InetAddress client, String path, RemoteFSElem elem)
    {
        return new CdpEvent(client, path, CDP_SYNC_DIR_RECURSIVE, elem);
    }
    public static CdpEvent createFullSyncRecursiveEvent( InetAddress client)
    {
        return new CdpEvent(client, null, CDP_FULLSYNC, null);
    }
    public static CdpEvent createDirDeleteRecursiveEvent( InetAddress client, String path, RemoteFSElem elem)
    {
        return new CdpEvent(client, path, CDP_DELETE_DIR_RECURSIVE, elem);
    }
    public static CdpEvent createParentDirEvent( InetAddress client, String parentPath, RemoteFSElem elem)
    {
        return new CdpEvent(client, parentPath, CDP_SYNC_DIR, elem);
    }

    private CdpEvent(InetAddress client, String path, int mode,  RemoteFSElem e)
    {
        this.client = client;
        this.path = path;
        this.mode = mode;

        elem = e;
        touch();
        created = lastTouched;
        
    }

    @Override
    public String toString()
    {
        return "P:" + path + " M:" + getModeText(mode);
    }

    String getModeText( int m )
    {
        switch( m )
        {
            case CDP_SYNC_DIR: return "SyncDir";
            case CDP_SYNC_DIR_RECURSIVE: return "SyncDirRecursive";
            case CDP_DELETE_DIR_RECURSIVE: return "DelDirRecursive";
            case CDP_FULLSYNC: return "FullSync";
        }
        return "?";
    }


    public int getMode()
    {
        return mode;
    }

    public RemoteFSElem getElem()
    {
        return elem;
    }

    public String getPath()
    {
        return path;
    }

    public long getLastTouched()
    {
        return lastTouched;
    }

    public long getCreated()
    {
        return created;
    }

    public InetAddress getClient()
    {
        return client;
    }

    
    public final void touch()
    {
        lastTouched = System.currentTimeMillis();
    }

    public boolean isDirectParentof( String ev )
    {
        if (ev.length() < path.length())
            return false;

        for (int i = 0; i < path.length(); i++)
        {
            if (ev.charAt(i) != path.charAt(i))
                return false;
        }
        for (int i =  path.length() + 1; i < ev.length(); i++)
        {
            if (ev.charAt(i) == '/' || ev.charAt(i) == '\\' )
                return false;
        }

        return true;
    }
    public boolean isParentof( String ev )
    {
        if (ev.length() <= path.length()  )
            return false;

        for (int i = 0; i < path.length(); i++)
        {
            if (ev.charAt(i) != path.charAt(i))
                return false;
        }
        return true;
    }
    public boolean isChildof( String  ev )
    {
        if ( path.length() <= ev.length() )
            return false;

        for (int i = 0; i < ev.length(); i++)
        {
            if (ev.charAt(i) != path.charAt(i))
                return false;
        }
        return true;
    }
}