package com.google.gerrit.gerritconsoleapi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.server.cache.CacheBinding;
import com.google.gerrit.server.cache.PersistentCacheFactory;

import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.plugins.Plugin;
import com.google.inject.Inject;

import com.google.inject.Singleton;

import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NonPersistanceCacheFactory implements PersistentCacheFactory, LifecycleListener {
  private static final Logger log =
      LoggerFactory.getLogger(NonPersistanceCacheFactory.class);


  @Inject
  NonPersistanceCacheFactory(
      @GerritServerConfig Config cfg,
      SitePaths site,
      DynamicMap<Cache<?, ?>> cacheMap) {

  }

  @Override
  public void start() {
    throw new RuntimeException(new NotSupportedException("NonPersistanceCache should not be called, use persist=false"));

  }

  @Override
  public void stop() {
    throw new RuntimeException(new NotSupportedException("NonPersistanceCache should not be called, use persist=false"));
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public <K, V> Cache<K, V> build(CacheBinding<K, V> def) {
    throw new RuntimeException(new NotSupportedException("NonPersistanceCache should not be called, use persist=false"));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> LoadingCache<K, V> build(
      CacheBinding<K, V> def,
      CacheLoader<K, V> loader) {
    throw new RuntimeException(new NotSupportedException("NonPersistanceCache should not be called, use persist=false"));
  }

  @Override
  public void onStop(Plugin plugin) {
    throw new RuntimeException(new NotSupportedException("NonPersistanceCache should not be called, use persist=false"));
  }

}
