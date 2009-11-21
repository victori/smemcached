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

import com.base.storage._

object Commands {
  val supported =
  List("SET","CAS","PREPEND","APPEND","ADD","REPLACE","STATS",
       "INCR","DECR","GET","GETS","QUIT","VERSION","FLUSH_ALL","DELETE","ERROR")
}

object MemcacheProtocolDecoder {
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
    val opts:Array[String] = line.substring(line.indexOf(" ")+1).split(" ").map({case w => w.trim}).toArray
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