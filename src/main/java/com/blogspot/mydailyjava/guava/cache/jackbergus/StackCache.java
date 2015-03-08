package com.blogspot.mydailyjava.guava.cache.jackbergus;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public class StackCache<T> extends Stack<T>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3644923435712459806L;
	private final Map<T, Integer> countMap = CacheBuilder.createTmpCacheBuilder();
    private final Map<Integer, Set<T>> stackMap = CacheBuilder.createTmpCacheBuilder();
    private int maxCount = 0;
    
    @Override
    public T push(T o) {
        Integer c = countMap.get(o);
        if (c == null) {
            countMap.put(o, c = 1);
        } else {
            countMap.put(o, ++c);
        }
        Set<T> set = stackMap.get(c);
        if (set == null)
            stackMap.put(c, set = new LinkedHashSet<T>());
        set.add(o);
        if (c > maxCount)
            maxCount = c;
        return o;
    }   
    
    @Override
    public T pop() {
        if (maxCount == 0)
            return null;
        Set<T> set = stackMap.get(maxCount);
        T o = set.iterator().next();
        set.remove(o);
        if (maxCount == 1) {
            countMap.remove(o);
        }
        if (set.size() == 0) {
            stackMap.remove(maxCount);
            --maxCount;
        }
        return o;
    }   
    
    @Override
    public T peek() {
        if (maxCount == 0) 
            return null;
        return stackMap.get(maxCount).iterator().next();
    }   
    
    @Override
    public String toString() {
    	return countMap.toString();
    }
}
