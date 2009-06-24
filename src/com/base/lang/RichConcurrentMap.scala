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

package com.base.lang
import java.util.concurrent.ConcurrentMap
import scala.collection.mutable.{Map => MMap}

class RichConcurrentMap[T,V](m: ConcurrentMap[T,V]) {
  
  def keyList:List[T] = {
    val i = m.keySet().iterator
    var k:List[T] = List()
    while(i.hasNext) {
      k = k ::: List(i.next)
    }
    return k
  }

  def toMap:Map[T,V] = {
    val i = m.keySet().iterator
    var mp:Map[T,V] = Map()
    while(i.hasNext) {
      val key:T = i.next
      mp += (key->m.get(key))
    }
    return mp
  }
}

object RichConcurrentMap {
  implicit def toRichConcurrentMap[T,V](m:ConcurrentMap[T,V]) = new RichConcurrentMap(m)
}
