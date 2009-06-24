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

import java.io._

class ResponseMessage extends Serializable {
  private val bos:ByteArrayOutputStream = new ByteArrayOutputStream()

  def bytes:Array[Byte] = bos.toByteArray
  def size:Int = bos.size
  def stat(name:String,value:Any): Unit = write("STAT "+name+" "+value)
  
  def write(bytes:Array[Byte]):Unit = {
    bos.write(bytes)
    bos.write("\r\n".getBytes())
  }
  
  def write(str:String):Unit = {
    bos.write(str.getBytes)
    if(!str.contains("\r\n")) bos.write("\r\n".getBytes)
  }
}