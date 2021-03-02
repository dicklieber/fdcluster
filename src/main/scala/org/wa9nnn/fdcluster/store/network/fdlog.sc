import java.time.{Instant, ZoneId}
val instant = Instant.now()
val msHour = 1000 * 60 * 60
val epochhours1 = instant.toEpochMilli / msHour
Thread.sleep(1000)
val epochhours2 = instant.toEpochMilli / msHour
val nextHour = instant.plus(40, java.time.temporal.ChronoUnit.MINUTES)
val epochhours3 = nextHour.toEpochMilli / msHour

val msReversed = epochhours3 * msHour
val iReversed = Instant.ofEpochMilli(msReversed)

val zoneId = ZoneId.of("UTC")
val tt = zoneId.toTemporal()

