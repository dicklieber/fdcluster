
package org.wa9nnn.fdcluster

import java.net.{Inet4Address, InetAddress, NetworkInterface, URL}
import java.nio.file.{Path, Paths}
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.wa9nnn.fdcluster.javafx.sync.{ProgressStep, SyncSteps}
import org.wa9nnn.fdcluster.metrics.Reporter
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.{NodeInfo, NodeInfoImpl, StoreActor}
import scalafx.collections.ObservableBuffer

import java.util.prefs.Preferences
import scala.jdk.CollectionConverters._

class Module extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    try {
      val actorSystem = ActorSystem()
      bind[ActorSystem].toInstance(actorSystem)
      bind[Config].toInstance(actorSystem.settings.config)
      bind[CurrentStationProvider].to[CurrentStationProviderImpl].asEagerSingleton()
      bind[Reporter].asEagerSingleton()
      bind[Preferences].toInstance(Preferences.userRoot.node("org/wa9nnn/fdcluster"))

    } catch {
      case e: Throwable ⇒
        e.printStackTrace()
    }

    println("Done")
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
                 nodeInfo: NodeInfo,
                 currentStationProvider: CurrentStationProvider,
                 @Named("ourInetAddresss") inetAddress: InetAddress,
                 config: Config,
                 @Named("journalPath") journalPath: Path,
                 syncSteps: SyncSteps,
                 @Named("allQsos") allQsos: ObservableBuffer[QsoRecord]
                ): ActorRef = {
    actorSystem.actorOf(StoreActor.props(nodeInfo, currentStationProvider, inetAddress, config, journalPath, allQsos, syncSteps))
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
