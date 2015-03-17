package com.blogspot.mydailyjava.guava.cache.jackbergus;

import com.blogspot.mydailyjava.guava.cache.jackbergus.CacheMap;
import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemCacheBuilder;
import com.blogspot.mydailyjava.guava.cache.overflow.FileSystemPersistingCache;
import com.google.common.primitives.UnsignedLongs;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CacheBuilder {
    private final static int TMP_CACHE_SIZE = 200;
    
    public static String getnerateTmpFileName() {
        return new StringBuilder()
                    .append("tmpCache")
                    .append(UnsignedLongs.toString(new Date().getTime()))
                    .append(UUID.randomUUID())
                    .toString();
        
    }
    
    public static <K,V> CacheMap<K,V> createTmpCacheBuilder(Function<String,K> cast) {
        try {
            
            //File f = File.createTempFile(getnerateTmpFileName(), "");
            File f= java.nio.file.Files.createTempDirectory(getnerateTmpFileName()).toFile();
            f.deleteOnExit();
            return new CacheMap<>((FileSystemPersistingCache<K, V>) FileSystemCacheBuilder.<K,V>newBuilder().persistenceDirectory(f).maximumSize(TMP_CACHE_SIZE).softValues().build(),cast);
        } catch (IOException ex) {
            Logger.getLogger(CacheBuilder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}