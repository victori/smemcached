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

package com.base

import java.net.InetSocketAddress;
import org.apache.mina.transport.socket._
import org.apache.mina.transport.socket.nio._
import org.apache.mina.core.filterchain._
import com.base.storage._
import org.apache.mina.filter.codec.ProtocolCodecFilter
import com.base.protocol._
import com.base.lang._

object SMemcached { final val version:String = "0.6" }
class SMemcached(address:String,port:Int,threads:Int,storage:CacheStorage) {
  private var acceptor:NioSocketAcceptor = _
  
  def this(port:Int,storage:CacheStorage) = this(null,port,(Sys.cpus+1),storage)
  def this(address:String,port:Int,storage:CacheStorage) = this(address,port,(Sys.cpus+1),storage)

  def startBlocking():Unit = {
    start()
    do { Thread.sleep(3000) } while(true)
  }
  
  def start():Unit = {
    acceptor = new NioSocketAcceptor(threads)
    val chain = acceptor.getFilterChain()

    chain.addFirst("codec", new ProtocolCodecFilter(new MemcacheProtocolFactory()))

    val cfg = acceptor.getSessionConfig()
    cfg.setSendBufferSize(1024000)
    cfg.setReceiveBufferSize(1024000)
    cfg.setTcpNoDelay(true)
    cfg.setReuseAddress(true)
    
    acceptor.setReuseAddress(true)
    // Actor based handler in the works.
    //acceptor.setHandler(new IoActorAdapter(sess => new MemcacheActor(storage,sess)))
    acceptor.setHandler(new MemcacheHandler(storage))
    acceptor.bind(if(address == null) new InetSocketAddress(port) else new InetSocketAddress(address,port))
  }
  
  def stop(): Unit = acceptor.unbind()
}
