/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.Utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Simple implementation of a list that can have more than Integer.MAX_VALUE
 * values. Works as a normal {@link ArrayList} until the size gets bigger than
 * Integer.MAX_VALUE. After that, it works as a {@link List} of
 * {@link ArrayList}s, so {@link #get(long)} will be a little bit more
 * expensive.
 * <p/>
 * For now, only {@link #add(Object)}, {@link #get(long)}, and {@link #size()}
 * are supported.
 * 
*
 * 
* @param <T>
 * @since 2.0
 * @version 2.0
 */
public final class BigArrayList<T> implements Iterable<T> {

    private static final long serialVersionUID = 1L;
    /**
     * The default max size a {@link List} can have
     */
    public static final int DEFAULT_MAX_SIZE = Integer.MAX_VALUE - 1;
    /**
     * The concrete data
     */
    private BigList<T> data;
    /**
     * Flag checking if we are using a {@link SingleList}
     */
    private boolean singleData;
    /**
     * The max size a {@link List} can have
     */
    private final int maxSize;

    public BigArrayList() {
        this(DEFAULT_MAX_SIZE);
    }

    BigArrayList( int maxSize ) {
        data = new SingleList();
        singleData = true;
        this.maxSize = maxSize;
    }

    public static <T> BigArrayList<T> create() {
        return new BigArrayList<>();
    }

    public static <T> BigArrayList<T> create( int maxSize ) {
        return new BigArrayList<>(maxSize);
    }

    /**
     * Returns the element at the specified position in this list.
     *     
* @param index
     * @return
     */
    public T get( long index ) {
        return data.get(index);
    }

    /**
     * Set the value of the index
     *
     * @param theIndex the index
     * @param theElem the element to set at the index
     * @return the value
     */
    public T set( final long theIndex, final T theElem ) {
        return data.set(theIndex, theElem);
    }

    /**
     * Appends the specified element to the end of this list.
     *     
* @param element
     * @return
     */
    public boolean add( T element ) {
        if (singleData && data.size() >= maxSize) {
            data = new MultiList((SingleList) data);
            singleData = false;
        }
        return data.add(element);
    }

    /**
     * Returns the number of elements in this list.
     *     
* @return
     */
    public long size() {
        return data.size();
    }

    public void clear() {
        data.clear();
        singleData = true;
    }

    /**
     * An interface for datastructures that provide a long-sized {@link List}
     *     
* @author Pedro Oliveira <pedro@clarkparsia.com>
     *     
* @param <K>
     */
    private interface BigList<K> extends Iterable<K> {

        public K get( long index );

        public boolean add( K element );

        public long size();

        public K set( long theIndex, K theElement );

        public void clear();
    }

    /**
     * {@link BigList} implementation with a single {@link List}. Only supports
     * {@link Integer#MAX_VALUE} elements.
     *     
* @author Pedro Oliveira <pedro@clarkparsia.com>
     *     
*/
    private class SingleList implements BigList<T> {

        private List<T> data;

        public SingleList() {
            data = new ArrayList<>();
        }

        @Override
        public void clear() {
            data.clear();
        }

        @Override
        public T get( long index ) {
            return data.get((int) index);
        }

        @Override
        public boolean add( T element ) {
            return data.add(element);
        }

        @Override
        public long size() {
            return data.size();
        }

        @Override
        public T set( final long theIndex, final T theElement ) {
            if (theIndex + 1 > data.size()) {
                ((ArrayList) data).ensureCapacity((int) theIndex + 1);
                while (theIndex + 1 > data.size()) {
                    data.add(null);
                }
            }
            return data.set((int) theIndex, theElement);
        }

        @Override
        public Iterator<T> iterator() {
            return data.iterator();
        }
    }

    /**
     * {@link BigList} implementation with a {@link List} of {@link List}s
     *     
* @author Pedro Oliveira <pedro@clarkparsia.com>
     *     
*/
    private class MultiList implements BigList<T> {

        private List<List<T>> data;
        private transient List<T> currList;
        private long size;

        public MultiList() {
            data = new ArrayList<>();
            size = 0;
            grow();
        }

        public MultiList( SingleList otherData ) {
            this.data = new ArrayList<>();
            this.data.add(otherData.data);
            this.size = otherData.size();
            grow();
        }

        @Override
        public void clear() {
            data = new ArrayList<>();
            size = 0;
            grow();
        }

        @Override
        public T get( long index ) {
            int pos = (int) (index / maxSize);
            int offset = (int) (index - ((long) pos * maxSize));
            return data.get(pos).get(offset);
        }

        @Override
        public T set( final long theIndex, final T theElement ) {
            int pos = (int) (theIndex / maxSize);
            int offset = (int) (theIndex - ((long) pos * maxSize));
            while (pos > data.size() - 1) {
                grow();
            }
            return data.get(pos).set(offset, theElement);
        }

        @Override
        public boolean add( T element ) {
            if (currList.size() == maxSize) {
                grow();
            }
            if (currList.add(element)) {
                size++;
                return true;
            }
            return false;
        }

        @Override
        public long size() {
            return size;
        }

        private void grow() {
            currList = new ArrayList<>();
            data.add(currList);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                private final Iterator<List<T>> lists = data.iterator();
                private Iterator<T> iter = lists.next().iterator();

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public T next() {
                    T next = iter.next();
                    if (!iter.hasNext() && lists.hasNext()) {
                        iter = lists.next().iterator();
                    }
                    return next;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };
        }
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }
}