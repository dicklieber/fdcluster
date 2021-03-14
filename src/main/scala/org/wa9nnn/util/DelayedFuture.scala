/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.util

object DelayedFuture {
  import java.util.{Date, Timer, TimerTask}
  import scala.concurrent._
  import scala.concurrent.duration.FiniteDuration
  import scala.util.Try

  private val timer = new Timer(true)

  private def makeTask[T]( body: => T )( schedule: TimerTask => Unit )(implicit ctx: ExecutionContext): Future[T] = {
    val prom = Promise[T]()
    schedule(
      new TimerTask{
        def run() {
          // IMPORTANT: The timer task just starts the execution on the passed
          // ExecutionContext and is thus almost instantaneous (making it
          // practical to use a single  Timer - hence a single background thread).
          ctx.execute(
            new Runnable {
              def run() {
                prom.complete(Try(body))
              }
            }
          )
        }
      }
    )
    prom.future
  }
  def apply[T]( delay: Long )( body: => T )(implicit ctx: ExecutionContext): Future[T] = {
    makeTask( body )( timer.schedule( _, delay ) )
  }
  def apply[T]( date: Date )( body: => T )(implicit ctx: ExecutionContext): Future[T] = {
    makeTask( body )( timer.schedule( _, date ) )
  }
  def apply[T]( delay: FiniteDuration )( body: => T )(implicit ctx: ExecutionContext): Future[T] = {
    makeTask( body )( timer.schedule( _, delay.toMillis ) )
  }
}