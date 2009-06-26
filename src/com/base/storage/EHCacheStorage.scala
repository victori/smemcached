/*
 * EHCacheStorage.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.base.storage
import com.base.cache.ICache
import com.base.cache.Ehcache
import com.base.cache.ICacheStat


class EHCacheStorage(cacheName:String,ttl:Int,maxEl:Int,overflow:Boolean) extends CacheStorage {
  private val cache = new Ehcache(cacheName,ttl,maxEl,overflow,60*60)

  val maxMemoryMB = Runtime.getRuntime.maxMemory / 1024 /1024
  println("Cache with "+maxEl+" in memory")
  println("File cache size: "+maxMemoryMB+"m")
  
  if (overflow) {
    cache.getCache.getCacheConfiguration.setOverflowToDisk(true)
    cache.getCache.getCacheConfiguration.setDiskSpoolBufferSizeMB(maxMemoryMB.toInt)
    cache.getCache.getCacheConfiguration.setMaxElementsOnDisk(maxEl * 2)
  }

  override def put(el:MemElement):Unit = cache.put(el.key, el, el.expire)
  override def get(key:String):MemElement = cache.get(key).asInstanceOf[MemElement]
  override def remove(key:String):Unit = cache.remove(key)
  override def size:Long = cache.getKeys.size
  override def evictions:Long = cache.getCacheEvictions
  override def keys:List[String] = List.fromArray(cache.getKeys.toArray).asInstanceOf[List[String]]
  override def clear:Unit = cache.clear
}
