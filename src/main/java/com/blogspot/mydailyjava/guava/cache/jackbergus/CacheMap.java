/*
 * Copyright (C) 2015 Giacomo Bergami <giacomo@openmailbox.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.blogspot.mydailyjava.guava.cache.jackbergus;



import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemPersistingCache;
import com.google.common.cache.Cache;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a Map view over the actual cache.
 * @author Giacomo Bergami
 * @param <K>	Key
 * @param <V>	Value
 */
public class CacheMap<K,V> implements Map<K,V> {
    
    private FileSystemPersistingCache<K,V> c;
    private final Callable<? extends V> call;
    private Function<String,K> cast;
    
    public CacheMap(FileSystemPersistingCache<K,V> c, Function<String,K> conv) {
        this.call = () -> null;
        this.c = c;
        this.cast = conv;
    }

    @Override
    public int size() {
        return (int)c.size();
    }

    @Override
    public boolean isEmpty() {
        return (c.size()==0);
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            return (c.get((K)key, call)!=null);
        } catch (Throwable ex) {
            //Logger.getLogger(CacheMap.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        for (K k : keySet()) {
        	if (get(k).equals(value))
        		return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        try {
            return (c.get((K)key, call));
        } catch (Throwable ex) {
            //Logger.getLogger(CacheMap.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        V prev = get(key);
        c.put(key,value);
        return prev;
    }

    @Override
    public V remove(Object key) {
        V prev = get(key);
        c.invalidate(key);
        return prev;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        c.putAll(m);
    }

    @Override
    public void clear() {
        c.invalidateAll();
    }

    @Override
    public Set<K> keySet() {
        return c.getKeys(cast);
    }

    @Override 
    public Collection<V> values() {
    	List<V> values = new LinkedList<V>();
        for (K k : keySet()) {
        	values.add(get(k));
        }
        return values;
    }

    @Override 
    public Set<Entry<K, V>> entrySet() {
    	final CacheMap<K,V> self = this;
        return new MapSetView(this);
    }
    
    /**
     * Performs the persistency in the local file system
     */
    public void persist() {
    	c.persist();
    }
    
    /**
     * Persists the in-ram memory and clean the RAM
     */
    public void cleanUp() {
    	c.persist();
    	c.cleanUp();
    	
    }
    
}
