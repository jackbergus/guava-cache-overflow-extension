package com.blogspot.mydailyjava.guava.cache.jackbergus;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public class PersistedKeyIterator<K> implements Iterator<K> {

	
	
	private File[] it;
	private int pos;
	private  Function<String,K> fun;
	public  PersistedKeyIterator(File persistenceRootDirectory, Function<String,K> conv) {
		it = persistenceRootDirectory.listFiles();
		pos = 0;
		this.fun = conv;
	}
	
	private boolean posCheck(int pos) {
		if (pos>=it.length)
			return false;
		File x = it[pos];
		return (!(x.getName().contains("Icon")&&x.getName().endsWith("\r")))
			;
	}
	
	@Override
	public boolean hasNext() {
		while (!posCheck(pos)) {
			if (pos>=it.length)
				return false;
			else pos++;
		}
		return (!(pos>=it.length));
	}
	
	@Override
	public K next() {
		if (hasNext()) {
			File x = it[pos];
			K key = fun.apply(x.getName());
			pos++;
			return key;
		}
		return null;
	}
	
}
