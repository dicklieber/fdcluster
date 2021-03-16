
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

package org.wa9nnn.fdcluster

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.racc.tscg.TypesafeConfigModule
import com.google.inject.{AbstractModule, Injector, Provides}
import com.typesafe.config.{Config, ConfigFactory}
import net.codingwell.scalaguice.ScalaModule
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfoConsumer, RunningTaskPane}
import org.wa9nnn.fdcluster.javafx.sync.{ProgressStep, SyncSteps}
import org.wa9nnn.fdcluster.metrics.Reporter
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store._
import org.wa9nnn.fdcluster.tools.RandomQsoGenerator
import org.wa9nnn.util.{CommandLine, CommandLineScalaFxImpl}
import scalafx.application.JFXApp.Parameters
import scalafx.collections.ObservableBuffer

import java.net.{Inet4Address, InetAddress, NetworkInterface, URL}
import javax.inject.{Named, Singleton}
import scala.jdk.CollectionConverters._

/**
 * This is where dependemnch injection (Guice) is managed.
 * Note not all objects are specifically configured here. Many, (most) are simply annotated with @Inject() and
 * scala guice magic does automaticaly addsa them as required.
 *
 * @param parameters command line args
 */
class Module(parameters: Parameters) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    try {
      val commandLine = new CommandLineScalaFxImpl(parameters)
      bind[CommandLine].toInstance(commandLine)
      val contest = Contest(commandLine)
      bind[Contest].toInstance(contest)
      val config = ConfigFactory.load
      val fileManager = new FileManagerConfig(config, contest)
      // FileManager sets up log file so load early before logging start.
      bind[FileManager].toInstance(fileManager)
      val actorSystem = ActorSystem("default", config)

      val runningTaskPane = new RunningTaskPane
      bind[RunningTaskPane].toInstance(runningTaskPane)
      bind[RunningTaskInfoConsumer].toInstance(runningTaskPane)
      bind[ActorSystem].toInstance(actorSystem)
      bind[Config].toInstance(actorSystem.settings.config)
      install(TypesafeConfigModule.fromConfigWithPackage(config, "org.wa9nnn"))
      bind[OurStationStore].asEagerSingleton()
      bind[Reporter].asEagerSingleton()
      bind[Store].to[StoreMapImpl]
    }

    catch {
      case e: Throwable ⇒
        e.printStackTrace()
    }

  }

  @Provides
  @Singleton
  def getNodeInfo(contest: Contest,
                  nodeAddress: NodeAddress,
                  @Named("ourInetAddresss") inetAddress: InetAddress,
                  config: Config): NodeInfo = {
    val chttp = config.getConfig("fdcluster.http")
    val hostName = inetAddress.getCanonicalHostName
    val port = chttp.getInt("port")
    val url = new URL(s"http://$hostName:$port")
    val ret = new NodeInfoImpl(contest, nodeAddress, url)
    ret
  }

  @Provides
  @Singleton
  @Named("store")
  def getMyActor(actorSystem: ActorSystem,
                 injector: Injector,
                 nodeInfo: NodeInfo,
                 @Named("ourInetAddresss") inetAddress: InetAddress,
                 config: Config,
                 syncSteps: SyncSteps,
                 storeMapImpl: StoreMapImpl,
                 journalLoader: JournalLoader,
                 randomQsoGenerator: RandomQsoGenerator
                ): ActorRef = {
    actorSystem.actorOf(Props(new StoreActor(injector, nodeInfo, inetAddress, config, syncSteps, storeMapImpl, journalLoader, randomQsoGenerator)))
  }

  /**
   *
   * @return this 1st  V4 address that is not the loopback address.
   */
  @Provides
  @Singleton
  @Named("ourInetAddresss")
  def determineIp(): InetAddress = {
    (for {
      networkInterface <- NetworkInterface.getNetworkInterfaces.asScala.toList
      inetAddresses <- networkInterface.getInetAddresses.asScala
      if !inetAddresses.isLoopbackAddress && inetAddresses.isInstanceOf[Inet4Address]
    } yield {
      inetAddresses
    })
      .headOption.getOrElse(throw new IllegalStateException("No IP address!"))

  }

  @Provides
  @Singleton
  def nodeAddress(@Named("ourInetAddresss") inetAddress: InetAddress, config: Config): NodeAddress = {
    val instance = config.getInt("instance")
    NodeAddress(instance, inetAddress.getCanonicalHostName)
  }

  @Provides
  @Singleton
  @Named("stepsData")
  def stepsData(): ObservableBuffer[ProgressStep] = {
    ObservableBuffer[ProgressStep](Seq.empty)
  }

  @Provides
  @Singleton
  @Named("allQsos")
  def qsoBufferData(): ObservableBuffer[QsoRecord] = {
    ObservableBuffer[QsoRecord](Seq.empty)
  }

}
