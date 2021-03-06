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

package com.base.protocol
import org.apache.mina.core.service.IoHandlerAdapter

import com.base.storage._
import org.apache.mina.core.session.IoSession
import com.base.lang._
import scala.collection.mutable.{Map => MMap}
import java.util.concurrent.atomic.AtomicLong
import org.apache.commons.io.output.ByteArrayOutputStream
import com.base._

class MemcacheHandler(storage:CacheStorage,app:SMemcached) extends IoHandlerAdapter {
  private val stats = MMap[String,AtomicLong]("cmd_get"->new AtomicLong(0),
                                        "cmd_set"->new AtomicLong(0),
                                        "cmd_flush"->new AtomicLong(0),
                                        "total_cmd_set"->new AtomicLong(0),
                                        "get_hits"->new AtomicLong(0),
                                        "bytes_read"->new AtomicLong(0),
                                        "bytes_written"->new AtomicLong(0),
                                        "get_misses"->new AtomicLong(0),
                                        "total_connections"->new AtomicLong(0),
                                        "curr_connections"->new AtomicLong(0),
                                        "uptime"->new AtomicLong(Sys.timeSecs))

  @throws(classOf[Exception])
  override def sessionOpened(sess:IoSession):Unit = {
    super.sessionOpened(sess);
    stats("total_connections").addAndGet(1) ; stats("curr_connections").addAndGet(1)
  }

  @throws(classOf[Exception])
  override def sessionClosed(sess:IoSession):Unit = {
    super.sessionClosed(sess); 
    stats("curr_connections").decrementAndGet
  }

  @throws(classOf[Exception])
  override def exceptionCaught(sess:IoSession,cause:Throwable):Unit = cause.printStackTrace()

  @throws(classOf[Exception])
  override def messageReceived(sess:IoSession,msg:Object):Unit = {
    val mData = msg.asInstanceOf[MemElement]
    val resp = new ResponseMessage()
    mData.command match {
      case "ERROR" => resp.write("ERROR")
      case "STATS" => stats(resp)
      case "VERSION" => resp.write("VERSION "+SMemcached.version)
      case "FLUSH_ALL" => flush(resp)
      case "SET" | "REPLACE" | "PREPEND" | "APPEND" | "ADD" => set(mData,resp)
      case "INCR"|"DECR" => incOrDec(mData,resp)
      case "GETS"|"GET" => get(mData,resp)
      case "DELETE" => delete(mData,resp)
      case "QUIT" => sess.close
      case _ => {}
    }
    sess.write(resp)
  }

  protected def flush(resp:ResponseMessage):Unit = {
    stats("cmd_flush").addAndGet(1)
    storage.clear;resp.write("OK")
  }

  protected def set(cmd:MemElement,resp:ResponseMessage):Unit = {
    stats("cmd_set").addAndGet(1)
    cmd.command match {
      case c @ ("REPLACE" | "PREPEND" | "APPEND") => {
          storage.get(cmd.key) match {
            case null => resp.write("NOT_STORED")
            case el:MemElement => {
                val bos = new ByteArrayOutputStream()
                c match {
                  case "PREPEND" => { bos.write(cmd.data);bos.write(el.data) }
                  case "APPEND" => { bos.write(el.data);bos.write(cmd.data) }
                  case "REPLACE" => bos.write(cmd.data)
                }
                bos.close
                el.data=bos.toByteArray
                el.length = el.data.length
                storage.put(el)
                stats("total_cmd_set").addAndGet(1);stats("bytes_written").addAndGet(el.length)
                resp.write("STORED")
              }
          }
        }
      case "ADD" => {
          storage.get(cmd.key) match {
            case el:MemElement => resp.write("NOT_STORED")
            case null => {
                storage.put(cmd)
                stats("total_cmd_set").addAndGet(1);stats("bytes_written").addAndGet(cmd.length)
                resp.write("STORED")
              }
          }
        }
      case "SET" => {
          storage.put(cmd)
          stats("total_cmd_set").addAndGet(1);stats("bytes_written").addAndGet(cmd.length)
          resp.write("STORED")
        }
      case _ => {}
    }
  }

  protected def get(cmd:MemElement,resp:ResponseMessage):Unit = {
    stats("cmd_get").addAndGet(1)
    cmd.metaData.foreach({case k =>
          storage.get(k) match {
            case el:MemElement => {
                stats("get_hits").addAndGet(1);stats("bytes_read").addAndGet(el.length)
                val data = el.data
                val casStr = if (cmd.command == "GETS") " " +el.length else ""
                resp.write("VALUE "+el.key+" "+el.flags+" "+el.length+casStr)
                resp.write(data)
              }
            case null => stats("get_misses").addAndGet(1)
          }
          resp.write("END")
      })
  }

  protected def delete(cmd:MemElement,resp:ResponseMessage):Unit = {
    cmd.metaData.foreach({case k =>
          storage.get(k) match {
            case el:MemElement => {
                storage.remove(k)
                resp.write("DELETED")
              }
            case null => resp.write("NOT_FOUND")
          }
      })
  }

  protected def incOrDec(cmd:MemElement,resp:ResponseMessage):Unit = {
    def int(arr:Array[Byte]) = new String(arr).toInt
    implicit def int2arr(i:Int) = i.toString.getBytes
    storage.get(cmd.key) match {
      case el:MemElement => {
          cmd.command match {
            case "DECR" => el.data = { val r = int(el.data) - int(cmd.data); if(r >= 0) r else 0 }
            case "INCR" => el.data = int(el.data) + int(cmd.data)
          }
          storage.put(el)
          resp.write(new String(el.data))
      }
      case null => resp.write("NOT_FOUND")
    }
  }

  protected def stats(resp:ResponseMessage):Unit = {
    resp.stat("pid",Sys.pid)
    resp.stat("uptime", Sys.timeSecs-stats("uptime").get)
    resp.stat("time", Sys.timeSecs)
    resp.stat("pointer_size", if(Sys.is64bit) 64 else 32)
    resp.stat("accepting_conns",1)
    resp.stat("listen_disabled_num",0)
    resp.stat("threads", app.threads)
    resp.stat("version", SMemcached.version)
    resp.stat("curr_items", storage.size)
    resp.stat("evictions", storage.evictions)
    storage.stats(resp);
    stats.foreach({case (k,v) => if(k != "uptime") resp.stat(k,v )})
    resp.write("END")
  }
}
