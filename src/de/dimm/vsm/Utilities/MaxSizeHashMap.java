/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.Utilities;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
 public class MaxSizeHashMap<S, T> extends LinkedHashMap<S, T> 
    {
        private final int maxSize;

        public MaxSizeHashMap(int maxSize) {
            this.maxSize = maxSize;
        }
    
        @Override
        protected boolean removeEldestEntry(Map.Entry<S, T> eldest) 
        {
            return size() > maxSize;
        }
    }    