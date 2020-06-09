package de.bamberg.uni.isosysc.dsg.detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import java.util.Collection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.sf.ehcache.config.CacheConfiguration;

/**
 * Main class to run the application
 */
@SpringBootApplication
@EnableCaching
public class Application extends CachingConfigurerSupport {
	

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	/*
	 * Bean to declare caches for images.
	 */
	@Bean
	public net.sf.ehcache.CacheManager ehCacheManager() {
		CacheConfiguration imagesCache = new CacheConfiguration();
		imagesCache.setName("imagesCache");
		imagesCache.setMemoryStoreEvictionPolicy("LRU");
		imagesCache.setMaxEntriesLocalHeap(1000);
		imagesCache.setTimeToLiveSeconds(100);

		CacheConfiguration imagesCacheSorted = new CacheConfiguration();
		imagesCacheSorted.setName("imagesCacheSorted");
		imagesCacheSorted.setMemoryStoreEvictionPolicy("LRU");
		imagesCacheSorted.setMaxEntriesLocalHeap(1000);
		imagesCacheSorted.setTimeToLiveSeconds(200);

		net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
		config.addCache(imagesCache);
		config.addCache(imagesCacheSorted);
		return net.sf.ehcache.CacheManager.newInstance(config);
	}

	@Bean
	@Override
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheManager());
	}
	
}



