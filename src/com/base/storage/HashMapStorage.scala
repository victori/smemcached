/*
 * Copyright 2009 Victor Igumnov <victori@fabulously40.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.base.storage

import com.base.lang.RichList.toRichList

import java.util.concurrent.ConcurrentHashMap
import com.base.lang.RichConcurrentMap.toRichConcurrentMap

class HashMapStorage extends CacheStorage {
  private var _map:ConcurrentHashMap[String,MemElement] = new ConcurrentHashMap()

  override def put(el:MemElement):Unit = _map.put(el.key,el)
  override def get(key:String):MemElement = _map.get(key)
  override def remove(key:String):Unit = _map.remove(key)
  override def size:Long = _map.size
  override def evictions:Long = 0
  override def keys:List[String] = _map.keyList
  override def clear:Unit = { _map = new ConcurrentHashMap() }
}
