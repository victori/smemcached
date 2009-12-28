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
import com.base.protocol._


class EHCacheStorage(val cacheName:String,val ttl:Int,val maxEl:Int,val overflow:Boolean) extends CacheStorage {
  private val cache = new Ehcache(cacheName,ttl,maxEl,overflow,60*60)

  val maxMemoryMB = Runtime.getRuntime.maxMemory / 1024 /1024
  println("Cache with "+maxEl+" elements to be stored in memory.")
  println("File cache size of "+maxMemoryMB+"MB.")
  
  if (overflow) {
    cache.getCache.getCacheConfiguration.setOverflowToDisk(true)
    cache.getCache.getCacheConfiguration.setDiskSpoolBufferSizeMB(maxMemoryMB.toInt)
    cache.getCache.getCacheConfiguration.setMaxElementsOnDisk(maxEl * 2)
  }

  override def stats(resp:ResponseMessage):Unit = {
      resp.stat("max_memory_elements",maxEl)
      resp.stat("overflow_disk",overflow)
  }

  override def put(el:MemElement):Unit = cache.put(el.key, el, el.expire)
  override def get(key:String):MemElement = cache.get(key).asInstanceOf[MemElement]
  override def remove(key:String):Unit = cache.remove(key)
  override def size:Long = cache.getKeys.size
  override def evictions:Long = cache.getCacheEvictions
  override def keys:List[String] = List.fromArray(cache.getKeys.toArray).asInstanceOf[List[String]]
  override def clear:Unit = cache.clear
}
