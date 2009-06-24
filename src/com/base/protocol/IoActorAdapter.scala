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
import org.apache.mina.core.session.IoSession
import scala.actors.Actor
import scala.actors.Actor._
import com.base.storage._

case object SessClosed
case object SessCreated
case object SessOpened
case class SessRecieved(msg:AnyRef)
case class SessSent(msg:AnyRef)

class MemcacheActor(storage:CacheStorage,sess:IoSession) extends Actor {
  def act = {
    loop {
      react {
        case SessRecieved => {}
        case SessCreated => {}
        case SessOpened => {}
        case SessClosed => {}
      }
    }
  }
}

class IoActorAdapter(val fac:(IoSession)=>Actor) extends IoHandlerAdapter {
  private val KEY = "mActor"
  def getActor(sess:IoSession) = sess.getAttribute(KEY).asInstanceOf[Actor]

  override def sessionOpened(sess:IoSession) = getActor(sess) ! SessOpened
  override def messageReceived(sess:IoSession,msg: AnyRef) = getActor(sess) ! SessRecieved(msg)
  override def messageSent(sess:IoSession,msg: AnyRef) = getActor(sess) ! SessSent(msg)

  override def sessionCreated(sess:IoSession) = {
    val actor = fac(sess)
    sess.setAttribute(KEY,actor); actor ! SessCreated
  }
  override def sessionClosed(sess:IoSession) = {
    val actor = getActor(sess)
    sess.removeAttribute(KEY); actor ! SessClosed
  }
}
