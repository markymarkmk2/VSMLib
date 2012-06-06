package de.dimm.vsm.hash;

import fr.cryptohash.Digest;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class HashFunctionPool
{

    private int poolSize;
    private ArrayList<Digest> passiveObjects = new ArrayList<Digest>();
    private ArrayList<Digest> activeObjects = new ArrayList<Digest>();
    private ReentrantLock plock = new ReentrantLock();
    private ReentrantLock alock = new ReentrantLock();

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public HashFunctionPool( int size )
    {
        this.poolSize = size;
        this.populatePool();
    }

    public final void populatePool()
    {
        for (int i = 0; i < poolSize; i++)
        {
            try
            {
                plock.lock();
                this.passiveObjects.add(this.makeObject());
            }
            catch (Exception e)
            {
                plock.unlock();
                e.printStackTrace();
                System.out.println("Cannot create hash pool");
            }
            finally
            {
                if (plock.isLocked())
                {
                    plock.unlock();
                }
            }
        }
    }

    public void activateObject( MessageDigest hc )
    {
    }

    public boolean validateObject( MessageDigest hc )
    {
        return false;
    }

    public Digest get() throws IOException
    {
        Digest hc = null;
        try
        {
            plock.lock();
            if (this.passiveObjects.size() > 0)
            {
                hc = this.passiveObjects.remove(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException("Unable to get object out of pool "
                    + e.toString());

        }
        finally
        {
            plock.unlock();
        }
        if (hc == null)
        {
            try
            {
                hc = makeObject();
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new IOException(e);
            }
            catch (NoSuchProviderException e)
            {
                throw new IOException(e);
            }
        }
        try
        {
            this.alock.lock();
            this.activeObjects.add(hc);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException("Unable to get object out of pool "
                    + e.toString());

        }
        finally
        {
            alock.unlock();
        }
        return hc;
    }

    public void release( Digest hc ) throws IOException
    {
        try
        {
            hc.reset();
            alock.lock();
            this.activeObjects.remove(hc);
        }
        catch (Exception e)
        {
            alock.unlock();
            e.printStackTrace();
            throw new IOException("Unable to get object out of pool "
                    + e.toString());

        }
        finally
        {
            alock.unlock();
        }
        try
        {
            plock.lock();
            this.passiveObjects.add(hc);
        }
        catch (Exception e)
        {
            plock.unlock();
            e.printStackTrace();
            throw new IOException("Unable to get object out of pool "
                    + e.toString());

        }
        finally
        {
            plock.unlock();
        }
    }

    public Digest makeObject() throws NoSuchAlgorithmException,
            NoSuchProviderException
    {
        Digest hc = new fr.cryptohash.SHA1();
        return hc;
    }

    public void destroyObject( Digest hc )
    {
        hc.reset();
        hc = null;
    }
}
