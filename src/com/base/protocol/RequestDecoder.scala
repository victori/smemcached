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
        val mData = MemcacheProtocolDecoder.decode(baos.toString)
        if(mData.waitData) sess.setAttribute(sessId, mData) else out.write(mData)
        return MessageDecoderResult.OK
      }
    } while (buf.hasRemaining)

    buf.reset
    return MessageDecoderResult.NEED_DATA
  }
}
