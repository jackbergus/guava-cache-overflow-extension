package com.blogspot.mydailyjava.guava.cache.jackbergus;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Creates a set view over a map
 * @author Giacomo Bergami <giacomo@openmailbox.org>
 *
 * 
 * @param <K>
 * @param <V>
 */
public class MapSetView<K,V> implements Set<Entry<K,V>> {

	private Map<K,V> amap;
	
	public MapSetView(Map<K,V> arg) {
		this.amap = arg;
	}
	
	@Override
	public int size() {
		return amap.size();
	}

	@Override
	public boolean isEmpty() {
		return amap.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Entry))
			return false;
		Entry<K,V> e = (Entry<K,V>)o;
		if (amap.get(e.getKey())!=null)
			return (amap.get(e.getKey()).equals(e.getValue()));
		else 
			return false;
	}

	@Override
	public Iterator<java.util.Map.Entry<K, V>> iterator() {
		return new Iterator<java.util.Map.Entry<K, V>>(){

			Iterator<K> keyIterator = amap.keySet().iterator();
			
			@Override
			public boolean hasNext() {
				return keyIterator.hasNext();
			}

			@Override
			public java.util.Map.Entry<K, V> next() {
				final K k = keyIterator.next();
				final V v = amap.get(k);
				return new java.util.Map.Entry<K, V>(){
					private K key = k;
					private V value = v;
					@Override
					public K getKey() {
						return this.key;
					}

					@Override
					public V getValue() {
						return this.value;
					}

					@Override
					public V setValue(V value) {
						return null;
					}};
			}};
	}

	@Override
	public Object[] toArray() {
		Object o[] = new Object[size()];
		int  i = 0;
		Iterator<java.util.Map.Entry<K, V>> it = this.iterator();
		while (it.hasNext()) {
			o[i] = it.next();
			i++;
		}
		return o;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		Object o[] = new Object[size()];
		int  i = 0;
		Iterator<java.util.Map.Entry<K, V>> it = this.iterator();
		while (it.hasNext()) {
			o[i] = (T)it.next();
			i++;
		}
		return (T[])o;
	}

	@Override
	public boolean add(java.util.Map.Entry<K, V> e) {
		return (amap.put(e.getKey(), e.getValue()).equals(e.getValue()));
	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Entry))
			return false;
		Entry<K,V> e = (Entry<K,V>)o;
		return amap.remove(e.getKey(), e.getValue());
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object x : c) {
			if (!contains(x))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(
			Collection<? extends java.util.Map.Entry<K, V>> c) {
		boolean toret = true;
		for (java.util.Map.Entry<K, V> x : c) {
			if (!add(x))
				toret = false;
		}
		return toret;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean toret = true;
		Iterator<java.util.Map.Entry<K, V>> i = this.iterator();
		while (i.hasNext()) {
			Entry<K,V> e = i.next();
			if (!c.contains(e))
				amap.remove(e.getKey(), e.getValue());
		}
		for (Object o : c) {
			if (!contains(o))
				toret = false;
		}
		return toret;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean toret = true;
		for (Object x : c) {
			if (!remove(x))
				toret = false;
		}
		return toret;
	}

	@Override
	public void clear() {
		amap.clear();
	}
}
