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

import java.lang.management.ManagementFactory

object Sys {
  def cpus:Int = Runtime.getRuntime().availableProcessors
  def timeMillis:Long = System.currentTimeMillis
  def timeSecs:Long = System.currentTimeMillis/1000
  def is64bit:Boolean = System.getProperty("java.vm.name").contains("64-Bit")
  def pid:Int = {
    val str = ManagementFactory.getRuntimeMXBean().getName
    str.substring(0,str.indexOf("@")).toInt
  }
}
