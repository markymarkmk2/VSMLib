/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 *
 * @author Administrator
 */
public class ConcurrentCache {
    private final Cache cache;

    public ConcurrentCache( Cache cache )
    {
        this.cache = cache;
    }

    Element putIfAbsent( Element e)
    {
        synchronized(cache)
        {
            return cache.putIfAbsent(e);
        }
    }
    void put( Element e)
    {
        synchronized(cache)
        {
            cache.put(e);
        }
    }
    Element get( Object key)
    {
        synchronized(cache)
        {
            return cache.get(key);
        }
    }
    boolean remove( Object key)
    {
        synchronized(cache)
        {
            return cache.remove(key);
        }
    }

    public Cache getCache()
    {
        return cache;
    }


}
