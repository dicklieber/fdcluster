
package org.wa9nnn.fdlog

import akka.actor.{ActorRef, ActorSystem, Props}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import org.wa9nnn.fdlog.model.{CurrentStationProvider, CurrentStationProviderImpl}
import org.wa9nnn.fdlog.store.{NodeInfo, NodeInfoImpl, Store, StoreActor, StoreMapImpl}
import com.google.inject.Singleton

class Module extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[ActorSystem].toInstance(ActorSystem())
    bind[Store].to[StoreMapImpl].asEagerSingleton()
    bind[CurrentStationProvider].to[CurrentStationProviderImpl].asEagerSingleton()
    bind[NodeInfo].to[NodeInfoImpl]


  }
  @Provides
  @Singleton
  @Named("store")
  def getMyActor(actorSystem: ActorSystem, store: Store): ActorRef = {
    actorSystem.actorOf(Props(classOf[StoreActor],store))
//    actorSystem.actorOf(Props(new StoreActor(store)))
  }
}
