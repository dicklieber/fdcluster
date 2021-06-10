package about

import com.wa9nnn.util.tableui.{Cell, Header, Table}
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.store.network.multicast.MulticastThing
import org.wa9nnn.fdcluster.{AppInfo, BuildInfo}
import org.wa9nnn.util.HtmlTableBuider
import scalafx.scene.control.TextArea

import java.lang.management.ManagementFactory
import java.net.{Inet4Address, InterfaceAddress, NetworkInterface}
import java.time.{Duration, Instant}
import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
import scala.collection.mutable
import scala.jdk.CollectionConverters.ListHasAsScala

@Singleton
class AboutTable @Inject()(appInfo: AppInfo, nodeAddress: NodeAddress, multicastThing: MulticastThing) {

  def apply(): Table = {
    val tableBuilder = new HtmlTableBuider(Header("FdCuster", "Field", "Value"))

    tableBuilder("Application", BuildInfo.name)
    tableBuilder("Version", BuildInfo.version)
    tableBuilder("UpTime", Duration.between(appInfo.started, Instant.now()))
    tableBuilder("Git Branch", BuildInfo.gitCurrentBranch)
    tableBuilder("Git commit", BuildInfo.gitHeadCommit.getOrElse("--"))
    tableBuilder("Source Code", Cell("https://github.com/dicklieber/fdcluster")
      .withUrl("https://github.com/dicklieber/fdcluster"))

    tableBuilder("Built", BuildInfo.buildInstant)
    tableBuilder("Java Home", Cell(System.getenv("JAVA_HOME")))

    tableBuilder("Java Version", ManagementFactory.getRuntimeMXBean.getVmVersion)
    tableBuilder("JavaFx", System.getProperty("javafx.runtime.version"))
    tableBuilder("Our HTTP", Cell(nodeAddress.display).withUrl(nodeAddress.url))

    val args = ManagementFactory.getRuntimeMXBean.getInputArguments.asScala
    val control = new TextArea(args.mkString("\n")) {
      prefRowCount = args.size + 1
      prefColumnCount = 70
      editable = false
    }
    tableBuilder("Command Line", control)

    val ifList = NetworkInterface.getNetworkInterfaces
      .asScala
      .toList
      //      .filter(_.isUp)
      .flatMap { ni: NetworkInterface =>
        val addresses: mutable.Seq[InterfaceAddress] = ni.getInterfaceAddresses
          .asScala
          .filter {
            _.getAddress.isInstanceOf[Inet4Address]
          }
        addresses.map { address: InterfaceAddress =>
          val host = address.getAddress.getHostAddress
          f"${ni.getName}%-10s\t$host%-10s"

        }
      }

    val netIfControl = new TextArea(ifList.mkString("\n")) {
      prefRowCount = ifList.size
      prefColumnCount = 70
      editable = false
    }
    tableBuilder("Network Interfaces", netIfControl)

    tableBuilder("Multicast Interface", multicastThing.networkInterface.getInetAddresses.asScala.toList.head.getHostAddress)
    tableBuilder.result
  }
}
