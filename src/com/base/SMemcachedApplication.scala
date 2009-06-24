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

import org.apache.commons.cli._
import com.base.storage._
import com.base.lang._

object SMemcachedApplication {
  
	def main(args : Array[String]) : Unit = {
    var defs = Map[String,Any]("address"->null,"port"->11211,"threads"->(Sys.cpus + 1),
                               "maxElements"->10000,"timetoLive"->3600,"overflow"->true,"cacheName"->"defaultcache")

    val options = new Options()
    options.addOption("h", "help", false, "print this screen")
    options.addOption("v", "version", false, "show version")
    options.addOption("p", "port", true, "set port to listen on")
    options.addOption("l", "listen", true, "set address to listen on")
    options.addOption("t", "threads", true, "number of NIO threads, default " + defs("threads"))
    options.addOption("e", "elements", true, "max number of cached elements to hold in memory, default " + defs("maxElements"))
    options.addOption("d", "disk",true,"overflow to disk if max elements reached, default true")
    options.addOption("ttl","timetolive",true,"default time to live for the cache in seconds.")
    options.addOption("c", "cache", true, "cache name")

    val parser = new PosixParser()
    val cmdline = parser.parse(options,args)

    cmdline.getOptions.foreach({case x:Option =>
          x.getOpt match {
            case v @ ("p" | "port") => defs += ("port"->cmdline.getOptionValue(v))
            case v @ ("l" | "listen") => defs += ("address"->cmdline.getOptionValue(v))
            case v @ ("e" | "elements") => defs += ("maxElements"->cmdline.getOptionValue(v))
            case v @ ("d" | "disk") => defs += ("overflow"->cmdline.getOptionValue(v))
            case "v" | "version" => println("SMemcached Version "+SMemcached.version);exit
            case "h" | "help" | _  => OptsHelper.println("java -jar smemcached.jar",options);exit
          }
      })

    val sMemcached = new SMemcached(
      defs("address").asInstanceOf[String],
        defs("port").toString.toInt,
        defs("threads").toString.toInt,
        new HashMapStorage())

    println("Listening on "+ (if (defs("address") != null) defs("address")+":" else "") + defs("port"))
    sMemcached.startBlocking
	}
}

object OptsHelper {
  def println(str:String,opt:Options):Unit = new HelpFormatter().printHelp(str, opt)
}