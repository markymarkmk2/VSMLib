/**
 *   FUSE-J: Java bindings for FUSE (Filesystem in Userspace by Miklos Szeredi (mszeredi@inf.bme.hu))
 *
 *   Copyright (C) 2003 Peter Levart (peter@select-tech.si)
 *
 *   This program can be distributed under the terms of the GNU LGPL.
 *   See the file COPYING.LIB
 */
package de.dimm.vsm.fuse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;


public class FuseMount
{
    private static final Log log = LogFactory.getLog(FuseMount.class);

    static
    {
        System.loadLibrary("javafs");
    }

    private FuseMount()
    {
        // no instances
    }

    //
    // compatibility APIs
    //
    // prefered String level API
    public static void mount( String[] args, Filesystem3 filesystem3, Logger log ) throws Exception
    {
        mount(args, new FilesystemToFuseFSAdapter(filesystem3, log));
    }

    //
    // byte level API
    public static void mount( String[] args, FuseFS fuseFS ) throws Exception
    {
        ThreadGroup threadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "FUSE Threads");
        threadGroup.setDaemon(true);

        log.info("Mounting filesystem");

        mount(args, fuseFS, threadGroup);

        log.info("Filesystem is unmounted");

        if (log.isDebugEnabled())
        {
            int n = threadGroup.activeCount();
            log.debug("ThreadGroup(\"" + threadGroup.getName() + "\").activeCount() = " + n);

            Thread[] threads = new Thread[n];
            threadGroup.enumerate(threads);
            for (int i = 0; i < threads.length; i++)
            {
                log.debug("thread[" + i + "] = " + threads[i] + ", isDaemon = "
                        + threads[i].isDaemon());
            }
        }
        System.exit(1);
    }

    private static native void mount( String[] args, FuseFS fuseFS, ThreadGroup threadGroup ) throws Exception;
}
