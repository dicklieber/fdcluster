package org.wa9nnn.fdcluster.model.sync

import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{Journal, NodeAddress, Station}
import org.wa9nnn.fdcluster.store.JsonContainer

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.time.Instant
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.sync.NodeStatusQueueSpec.bigNodeStatus
import play.api.libs.json.Json
class NodeStatusQueueSpec extends Specification {
  val address0 = new NodeAddress()
  val ns0: NodeStatus = NodeStatus(nodeAddress = address0,
    qsoCount = 42,
    qsoHourDigests = List.empty,
    station = Station(),
    contest = Option(Contest()),
    journal = Option(Journal(address0))
  )

  "NodeStatusQueue" >> {
    "Happy" >> {
      val queue = new NodeStatusQueue()
      queue.size must beEqualTo(0)
      queue.take() must beEmpty
      queue.add(ns0)
      queue.take() must beSome(ns0)
      queue.take() must beEmpty

    }
    "latest" >> {
      val queue = new NodeStatusQueue()
      queue.add(ns0)
      val nsLater: NodeStatus = ns0.copy(stamp = Instant.now())
      queue.size must beEqualTo(1)
      queue.add(nsLater)
      queue.size must beEqualTo(1)
      queue.take() must beSome(nsLater)
      queue.take() must beEmpty
      queue.size must beEqualTo(0)
    }

    "java serialization" >> {
//      val ns0: NodeStatus = NodeStatus(nodeAddress = address0,
//        qsoCount = 42,
//        qsoHourDigests = List.empty,
//        station = Station(),
//        contest = Option(Contest()),
//        journal = Option(Journal(address0))
//      )
      val ns0 = bigNodeStatus

      val jsonContainer = JsonContainer(ns0)
      val jsonContainerBytes = jsonContainer.bytes

      val baos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(ns0)
      oos.close()

      val avaSerialiationBytes = baos.toByteArray
      val length = avaSerialiationBytes.length

      val sJson = Json.toJson(ns0).toString()


        pending
    }
  }
}

object NodeStatusQueueSpec {
  val bigNodeStatus: NodeStatus = Json.parse("""{
                                   |  "nodeAddress" : "http://10.37.129.2:8081|1",
                                   |  "qsoCount" : 10000,
                                   |  "qsoHourDigests" : [ {
                                   |    "fdHour" : {
                                   |      "day" : 29,
                                   |      "hour" : 19
                                   |    },
                                   |    "digest" : "qoVP/+4BWnc3KGgAvObH7QNguJwGtQQvBpRtZMs7PCA=",
                                   |    "size" : 435
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 29,
                                   |      "hour" : 20
                                   |    },
                                   |    "digest" : "BxfulljlOf+p8hN/41DToq2jF31Q7WvwKhGCEIV3Wo4=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 29,
                                   |      "hour" : 21
                                   |    },
                                   |    "digest" : "YURD2oegVJpOasaY10iR00wj2pBzWjUIwyidY4ivb9c=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 29,
                                   |      "hour" : 22
                                   |    },
                                   |    "digest" : "KB0vf9hJNlPc8Dx75Rw+myu4/ruQfzE0dc5/scpTiU4=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 29,
                                   |      "hour" : 23
                                   |    },
                                   |    "digest" : "6sHQtKY+8WxKYPT17p5An4j7NEH5OfgYqZy0mRtLg5M=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 0
                                   |    },
                                   |    "digest" : "0znRij5YEbYJBNZU0K1ki9PhVopTFUOCVbdFuZPlNnE=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 1
                                   |    },
                                   |    "digest" : "wtVm4h4yPzz/dWPAEVEE4UCb9t11ELviiZbmVZiA1f4=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 2
                                   |    },
                                   |    "digest" : "2r2z9ZKTrJVkjhIpOg9ENU30jhhZ0lo4spBjfWhbWaw=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 3
                                   |    },
                                   |    "digest" : "OlCDz9NX7ulw5H8hfZ4GFykSM0XswcY3yb6pAf95Wog=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 4
                                   |    },
                                   |    "digest" : "rrWVFZd/88L8GA1KcvrG6AJLClHHSFXG5flvT0p5hPE=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 5
                                   |    },
                                   |    "digest" : "48FmA50a1FLcaP3jrkaXk4Pcn5Dip96sSqgZq8OV84k=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 6
                                   |    },
                                   |    "digest" : "lzXO7oWfL2I4OKOhwF/rICeq50tpiK626m9IH4DFJws=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 7
                                   |    },
                                   |    "digest" : "k9tLHuTALOUzgF1zhEWU/CQ9hE/KUWsvfPkoBjpJ8OY=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 8
                                   |    },
                                   |    "digest" : "EMXSxP1UfoN8eh7N07qTn4/bJAxqNs1Jhu5wxtsQb4o=",
                                   |    "size" : 720
                                   |  }, {
                                   |    "fdHour" : {
                                   |      "day" : 30,
                                   |      "hour" : 9
                                   |    },
                                   |    "digest" : "LXF0Hx/lazA0TiNRMo4JxmR1mRWesQc8td3fMDWX+OQ=",
                                   |    "size" : 205
                                   |  } ],
                                   |  "station" : {
                                   |    "bandName" : "20m",
                                   |    "modeName" : "PH",
                                   |    "operator" : "WA9NNN",
                                   |    "rig" : "",
                                   |    "antenna" : "",
                                   |    "stamp" : "2021-06-30T18:19:57.494911Z"
                                   |  },
                                   |  "contest" : {
                                   |    "callSign" : "WM9W",
                                   |    "ourExchange" : "1A AB",
                                   |    "contestName" : "FieldDay",
                                   |    "nodeAddress" : "http://192.168.0.250:8081|1",
                                   |    "password" : "D/5HsT0sa7Y=",
                                   |    "stamp" : "2021-06-29T05:16:31.842449Z"
                                   |  },
                                   |  "journal" : {
                                   |    "journalFileName" : "FD2021629.18993.json",
                                   |    "nodeAddress" : "http://192.168.0.250:8081|1",
                                   |    "stamp" : "2021-06-29T05:16:33.064791Z"
                                   |  },
                                   |  "sessions" : [ {
                                   |    "sessionKey" : "65721c32fd703392",
                                   |    "station" : {
                                   |      "bandName" : "20m",
                                   |      "modeName" : "PH",
                                   |      "operator" : "WA9NNN",
                                   |      "rig" : "",
                                   |      "antenna" : "",
                                   |      "stamp" : "2021-06-30T18:19:57.494911Z"
                                   |    },
                                   |    "touched" : "2021-06-30T18:19:57.821707Z",
                                   |    "started" : "2021-06-30T18:19:57.821707Z"
                                   |  } ],
                                   |  "osName" : "Mac OS X 10.16",
                                   |  "stamp" : "2021-06-30T18:24:00.204778Z",
                                   |  "ver" : "0.0.9-SNAPSHOT",
                                   |  "sn" : 23
                                   |}""".stripMargin).as[NodeStatus]
}


