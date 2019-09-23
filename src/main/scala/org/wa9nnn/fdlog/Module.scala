
package org.wa9nnn.fdlog

import java.net.{Inet4Address, InetAddress, NetworkInterface, URL}
import java.nio.file.{Path, Paths}

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.wa9nnn.fdlog.model.{Contest, CurrentStationProvider, CurrentStationProviderImpl, NodeAddress}
import org.wa9nnn.fdlog.store.{NodeInfo, NodeInfoImpl, StoreActor}
import sun.jvmstat.monitor.{MonitoredHost, MonitoredVmUtil}

import scala.collection.JavaConverters._

class Module extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    try {
      val actorSystem = ActorSystem()
      bind[ActorSystem].toInstance(actorSystem)
      bind[Config].toInstance(actorSystem.settings.config)
      bind[CurrentStationProvider].to[CurrentStationProviderImpl].asEagerSingleton()

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
                  currentStationProvider: CurrentStationProvider,
                  @Named("ourInetAddresss") inetAddress: InetAddress,
                  config: Config): NodeInfo = {
    val chttp = config.getConfig("fdlog.http")
    val hostName = inetAddress.getCanonicalHostName
    val port = chttp.getInt("port")
    val url = new URL(s"http://$hostName:${port}")
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
                 @Named("journalPath") journalPath: Path): ActorRef = {
    actorSystem.actorOf(StoreActor.props(nodeInfo, currentStationProvider, inetAddress, config, journalPath))
  }

  @Provides
  @Singleton
  @Named("journalPath")
  def getJournalPath(config: Config): Path = {
    Paths.get(config.getString("fdlog.journalPath"))
  }


  /**
   *
   * @return this 1st inet V4 address that is not the loopback address.
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
  def nodeAddress(@Named("ourInetAddresss") inetAddress: InetAddress): NodeAddress = {
    val mh = MonitoredHost.getMonitoredHost(null.asInstanceOf[String])
    val ids = mh.activeVms().asScala
    val mainClasses = ids.toList.map { vmId ⇒
      val vmidString = "//" + vmId + "?mode=r"
      val aVmId = new sun.jvmstat.monitor.VmIdentifier(vmidString)
      val vm = mh.getMonitoredVm(aVmId)
      //      val vmIdentifier = vm.getVmIdentifier
      MonitoredVmUtil.mainClass(vm, false)
    }
    val fdLogCount = mainClasses.count(mainClass ⇒
      mainClass == "FdLog")
    NodeAddress(fdLogCount, inetAddress.getCanonicalHostName)

  }
}
