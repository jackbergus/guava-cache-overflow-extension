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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
    
    public CacheMap(FileSystemPersistingCache<K,V> c) {
        this.call = () -> null;
        this.c = c;
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
        } catch (ExecutionException ex) {
            Logger.getLogger(CacheMap.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (ExecutionException ex) {
            Logger.getLogger(CacheMap.class.getName()).log(Level.SEVERE, null, ex);
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
        return c.getKeys();
    }

    @Override @Deprecated
    public Collection<V> values() {
        throw new RuntimeException("entrySet should have never been used");
    }

    @Override @Deprecated
    public Set<Entry<K, V>> entrySet() {
        throw new RuntimeException("entrySet should have never been used");
    }
    
}
