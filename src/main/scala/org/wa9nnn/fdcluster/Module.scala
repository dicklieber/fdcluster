
package org.wa9nnn.fdcluster

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.racc.tscg.TypesafeConfigModule
import com.google.inject.{AbstractModule, Injector, Provides}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfoConsumer, RunningTaskPane}
import org.wa9nnn.fdcluster.javafx.sync.{ProgressStep, SyncSteps}
import org.wa9nnn.fdcluster.metrics.Reporter
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store._
import org.wa9nnn.util.{CommandLine, CommandLineScalaFxImpl}
import scalafx.application.JFXApp.Parameters
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import java.net.{Inet4Address, InetAddress, NetworkInterface, URL}
import java.nio.file.{Path, Paths}
import javax.inject.{Named, Singleton}
import scala.jdk.CollectionConverters._

/**
 *
 * @param args command line arg
 */
class Module(parameters: Parameters) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    try {
      bind[CommandLine].toInstance(new CommandLineScalaFxImpl(parameters))

      val actorSystem = ActorSystem()
      val runningTaskPane = new RunningTaskPane
      bind[RunningTaskPane].toInstance(runningTaskPane)
      bind[RunningTaskInfoConsumer].toInstance(runningTaskPane)
      bind[ActorSystem].toInstance(actorSystem)
      bind[Config].toInstance(actorSystem.settings.config)
      install(TypesafeConfigModule.fromConfigWithPackage(actorSystem.settings.config, "org.wa9nnn"))
      bind[OurStationStore].asEagerSingleton()
      bind[Reporter].asEagerSingleton()
      bind[Store].to[StoreMapImpl]
      bind[JournalLoader].to[JournalLoaderImpl].in[Singleton]
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
    val url = new URL(s"https://$hostName:$port")
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
                 journalLoader: JournalLoader
                ): ActorRef = {
    actorSystem.actorOf(Props(new StoreActor(injector, nodeInfo, inetAddress, config, syncSteps, storeMapImpl, journalLoader)))
  }

  @Provides
  @Singleton
  @Named("journalPath")
  def getJournalPath(config: Config): Path = {
    Paths.get(config.getString("fdcluster.journalPath"))
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
