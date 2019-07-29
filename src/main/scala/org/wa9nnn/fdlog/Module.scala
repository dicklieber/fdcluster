
package org.wa9nnn.fdlog

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.wa9nnn.fdlog.model.{CurrentStationProvider, CurrentStationProviderImpl, NodeInfo, NodeInfoImpl}
import org.wa9nnn.fdlog.store.{Store, StoreMapImpl}

class Module extends AbstractModule with ScalaModule {
  override def configure(): Unit = {

    bind[Store].to[StoreMapImpl].asEagerSingleton()
    bind[CurrentStationProvider].to[CurrentStationProviderImpl].asEagerSingleton()
    bind[NodeInfo].to[NodeInfoImpl]
    //    val stringMulti = ScalaMultibinder.newSetBinder[String](binder)
    //    stringMulti.addBinding.toInstance("A")
    //
    //    val annotatedMulti = ScalaMultibinder.newSetBinder[A, Annotation](binder)
    //    annotatedMulti.addBinding.to[A]
    //
    //    val namedMulti = ScalaMultibinder.newSetBinder[ServiceConfiguration](binder, Names.named("backend"))
    //    namedMulti.addBinding.toInstance(config.getAdminServiceConfiguration)
  }
}
