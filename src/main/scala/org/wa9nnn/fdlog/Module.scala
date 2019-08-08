
package org.wa9nnn.fdlog

import akka.actor.{ActorRef, ActorSystem, Props}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import org.wa9nnn.fdlog.model.{CurrentStationProvider, CurrentStationProviderImpl}
import org.wa9nnn.fdlog.store.{NodeInfo, NodeInfoImpl, StoreActor}

class Module extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[ActorSystem].toInstance(ActorSystem())
    bind[CurrentStationProvider].to[CurrentStationProviderImpl].asEagerSingleton()
    bind[NodeInfo].to[NodeInfoImpl]


  }
  @Provides
  @Singleton
  @Named("store")
  def getMyActor(actorSystem: ActorSystem, nodeInfo: NodeInfo,  currentStationProvider: CurrentStationProvider): ActorRef = {
    actorSystem.actorOf(Props(classOf[StoreActor],nodeInfo, currentStationProvider))
  }
}
