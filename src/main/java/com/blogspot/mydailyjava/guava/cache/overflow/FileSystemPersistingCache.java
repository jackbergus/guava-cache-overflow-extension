package com.blogspot.mydailyjava.guava.cache.overflow;

import com.blogspot.mydailyjava.guava.cache.jackbergus.PersistedKeyIterator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class FileSystemPersistingCache<K, V> extends AbstractPersistingCache<K, V> {

    //private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemPersistingCache.class);

    private final File persistenceRootDirectory;

    protected FileSystemPersistingCache(CacheBuilder<Object, Object> cacheBuilder) {
        this(cacheBuilder, Files.createTempDir());
    }

    protected FileSystemPersistingCache(CacheBuilder<Object, Object> cacheBuilder, File persistenceDirectory) {
        this(cacheBuilder, persistenceDirectory, null);
    }

    protected FileSystemPersistingCache(CacheBuilder<Object, Object> cacheBuilder, RemovalListener<K, V> removalListener) {
        this(cacheBuilder, Files.createTempDir(), removalListener);
    }

    protected FileSystemPersistingCache(CacheBuilder<Object, Object> cacheBuilder, File persistenceDirectory, RemovalListener<K, V> removalListener) {
        super(cacheBuilder, removalListener);
        this.persistenceRootDirectory = validateDirectory(persistenceDirectory);
        //LOGGER.info("Persisting to {}", persistenceDirectory.getAbsolutePath());
    }

    private File validateDirectory(File directory) {
    	//Some issues with the MacOS tmp folder making
    	directory.delete();
        directory.mkdir();
        directory.mkdirs();
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead() || !directory.canWrite()) {
        	if (!directory.exists())
                System.err.println("Err: dir does not exist");
            if (!directory.isDirectory())
                System.err.println("Err: not a directory");
            if (!directory.canRead())
                System.err.println("Err: cannot read");
            if (!directory.canWrite())
                System.err.println("Err: cannot write");
        	throw new IllegalArgumentException(String.format("A Directory %s cannot be used as a persistence directory",
                    directory.getAbsolutePath()));
        }
        return directory;
    }

    private File pathToFileFor(K key) {
        List<String> pathSegments = directoryFor(key);
        File persistenceFile = persistenceRootDirectory;
        for (String pathSegment : pathSegments) {
            persistenceFile = new File(persistenceFile, pathSegment);
        }
        if (persistenceRootDirectory.equals(persistenceFile) || persistenceFile.isDirectory()) {
            throw new IllegalArgumentException();
        }
        return persistenceFile;
    }
    
    @SuppressWarnings("unchecked")
	public Set<K> getPersistedKeys(Function<String,K> conv) {
    	Set<K> toret = new TreeSet<K>();
    	for (File x : persistenceRootDirectory.listFiles()) {
    		if (x.isFile()) {
    			//Shitty Mac
				if (x.getName().contains("Icon")||(x.getName().contains(".DS_Store"))||x.getName().startsWith("."))
					continue;
				//System.out.println(x.getName());
				K key = conv.apply(x.getName());
				toret.add(key);
	    		
    		}
    	}
    	return toret;
    }
    
    public Set<K> getKeys(Function<String,K> conv) {
    	Set<K> toret = getPersistedKeys(conv);
    	toret.addAll(super.asMap().keySet());
    	return toret;
    }
    
    public Iterator<K> getKeyIterator(Function<String,K> conv) {
    	return Iterators.concat(new PersistedKeyIterator(persistenceRootDirectory,conv),super.asMap().keySet().iterator());
    }

    @Override
    protected V findPersisted(K key) throws IOException {
        if (!isPersist(key)) return null;
        File persistenceFile = pathToFileFor(key);
        if (!persistenceFile.exists()) return null;
        FileInputStream fileInputStream = new FileInputStream(persistenceFile);
        try {
            FileLock fileLock = fileInputStream.getChannel().lock(0, Long.MAX_VALUE, true);
            try {
                return readPersisted(key, fileInputStream);
            } finally {
                fileLock.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            fileInputStream.close();
        }
    }
    
    @Override
    public V get(K key, Callable<? extends V> valueLoader)  {
        try {
            return super.get(key, valueLoader);
        } catch(Throwable ex1) {
            try {
                return findPersisted(key);
            } catch (Throwable ex2) {
                ex1.printStackTrace();
                ex2.printStackTrace();
                return null;
            }
        }
    }

    @Override
    protected void persistValue(K key, V value) throws IOException {
        if (!isPersist(key)) return;
        File persistenceFile = pathToFileFor(key);
        persistenceFile.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(persistenceFile);
        try {
            FileLock fileLock = fileOutputStream.getChannel().lock();
            try {
                persist(key, value, fileOutputStream);
            } finally {
                fileLock.release();
            }
        } finally {
            fileOutputStream.close();
        }
    }

    @Override
    protected void persist(K key, V value, OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
    }

    @Override
    protected boolean isPersist(K key) {
        return true;
    }

    @Override
    protected List<String> directoryFor(K key) {
        return Arrays.asList(key.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected V readPersisted(K key, InputStream inputStream) throws IOException {
        try {
            return (V) new ObjectInputStream(inputStream).readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Serialized version assigned by %s was invalid", key), e);
        }
    }

    @Override
    protected void deletePersistedIfExistent(K key) {
        File file = pathToFileFor(key);
        file.delete();
    }

    @Override
    protected void deleteAllPersisted() {
        for (File file : persistenceRootDirectory.listFiles()) {
            file.delete();
        }
    }

    @Override
    protected int sizeOfPersisted() {
        return countFilesInFolders(persistenceRootDirectory);
    }

    private int countFilesInFolders(File directory) {
        int size = 0;
        if (directory==null)
        {
        	System.err.println("ERROR: directory passed is null");
        	return 0;
        }
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                size += countFilesInFolders(file);
            } else if (!file.getName().startsWith(".")) {
                size++;
            }
        }
        return size;
    }

    public File getPersistenceRootDirectory() {
        return persistenceRootDirectory;
    }
}
