import org.wa9nnn.fdcluster.javafx.ValueName

val clazz = classOf[ValueName]
val name = clazz.getName
val canonicalName = clazz.getCanonicalName
val strings = canonicalName.split("""\.""")
strings.last
