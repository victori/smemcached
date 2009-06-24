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

import org.apache.mina.filter.codec.demux.MessageDecoderAdapter
import org.apache.mina.filter.codec.demux.MessageDecoderResult
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.filter.codec.ProtocolDecoderOutput
import org.apache.commons.io.output.ByteArrayOutputStream
import com.base.storage._
import scala.io.BytePickle


object Commands {
  val supported =
  List("SET","CAS","PREPEND","APPEND","ADD","REPLACE","STATS",
       "INCR","DECR","GET","GETS","QUIT","VERSION","FLUSH_ALL","DELETE","ERROR")
  def bytes2Nat(value:Array[Byte]) = value.toString.replace("Array(", "").replace(")","").toInt
}

object MemcacheElementDecoder {
  implicit def arr2list(arr:Array[String]) = arr.toList
  implicit def str2int(str:String) = str.toInt

  def decode(line:String):MemElement = {
    val cmd = (if(line.contains(" ")) line.substring(0,line.indexOf(" ")) else line).trim.toUpperCase

    val mData = new MemElement()
    if (!Commands.supported.exists(s => s.contains(cmd))) {
      mData.command="ERROR"
      return mData
    }

    // 0 = key, 1 = flags, 2 = expire, 3 = length
    val opts:Array[String] = line.substring(line.indexOf(" ")+1).split(" ").map({case w => w.trim})
    cmd match {
      case c @ ("SET" | "CAS" | "PREPEND" | "APPEND" | "ADD" | "REPLACE") => {
          mData.command=c
          mData.key=opts(0)
          mData.flags=opts(1)
          mData.expire=opts(2)
          mData.length=opts(3)
          mData.waitData=true
        }
      case c @ ("INCR" | "DECR") => { 
          mData.command=c
          mData.key=opts(0)
          mData.data=if(opts.size > 1) opts(1).getBytes else "1".getBytes
        }
      case c @ ("GET" | "GETS" | "DELETE") => {
          mData.command=c
          mData.metaData=opts
        }
      case c @ _ => {
          mData.command=c
          mData.metaData=opts
        }
    }

    return mData
  }
}

class RequestDecoder extends MessageDecoderAdapter {
  private val sessId:String = "session_data"

  def decodable(sess:IoSession,buf:IoBuffer): MessageDecoderResult = {
    val mData = sess.getAttribute(sessId).asInstanceOf[MemElement]
    if(mData != null && buf.remaining <= mData.length) MessageDecoderResult.NEED_DATA else MessageDecoderResult.OK
  }

  @throws(classOf[Exception])
  def decode(sess:IoSession,buf:IoBuffer,out:ProtocolDecoderOutput): MessageDecoderResult = {
    val baos = new ByteArrayOutputStream()

    /* Fetch data */
    val mData = sess.getAttribute(sessId).asInstanceOf[MemElement]
    if(mData != null) {
      if(buf.remaining <= mData.length) return MessageDecoderResult.NEED_DATA
      mData.data = new Array[Byte](mData.length) ; buf.get(mData.data)
      sess.setAttribute(sessId,null)
      out.write(mData)
      return MessageDecoderResult.OK
    }

    /* Decode command */
    buf.mark
    do {
      val c = buf.get.asInstanceOf[char]
      baos.write(c)
      if(c == '\r' && buf.hasRemaining && buf.get == '\n') {
        baos.close
        val mData = MemcacheElementDecoder.decode(baos.toString)
        if(mData.waitData) sess.setAttribute(sessId, mData) else out.write(mData)
        return MessageDecoderResult.OK
      }
    } while (buf.hasRemaining)

    buf.reset
    return MessageDecoderResult.NEED_DATA
  }
}
