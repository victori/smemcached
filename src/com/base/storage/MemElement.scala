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

import java.io.Serializable

class MemElement(key:String,flags:Int,expire:Int,length:Int) extends Serializable {
  var data:Array[Byte] = _
  var flags:Int = _
  var expire:Int = _
  var length:Int = _
  var cas:Long = _
  var key:String = _
  var waitData:Boolean = false
  var command:String = _
  var metaData:List[String] = _

  def this() = this(null,0,0,0)
  def this(key:String) = this(key,0,0,0)
}
