package org.wa9nnn.fdcluster.javafx

import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.javafx.NamedCellProvider.omitFieldNames
import org.wa9nnn.util.ClassName
import scalafx.scene.control.Label

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}


abstract class NamedCellProvider[T: ClassTag] extends Product {
  def collectNamedValues(namedValueCollector: NamedValueCollector): Unit = {
    val clazz: Class[_] = classTag[T].runtimeClass
    for {
      arity <- 0 until productArity
      str = productElementName(arity)
      if !omitFieldNames.contains(str)
    } yield {
      namedValueCollector(NamedValue(ValueName(clazz, str), productElement(arity)))
    }
  }
}

object NamedCellProvider {
  val omitFieldNames = Set("stamp", "v")
}

case class NamedValue(name: ValueName, value: Any)

case class ValueName(className: String, name: String) extends Ordered[ValueName] {
  override def compare(that: ValueName): Int = {
    var ret = className.compareTo(that.className)
    if (ret == 0)
      ret = name.compareTo(that.name)
    ret
  }
}

object ValueName {
  def apply(clazz: Class[_], name: String): ValueName = {
    new ValueName(ClassName.last(clazz), name)
  }
}

/**
 * A Label that can be updated
 *
 * @param name         of field.
 * @param value        starting.
 */
case class CellProperty(name: ValueName, value: Any) extends Label with Ordered[CellProperty] {
  //  text = Cell(s"${name.clazz}\t${name.name}\t$initialValue").value
  text = Cell(value).value

  def value(newVal: Any): Unit = {
    text = Cell(newVal).value
  }

  override def compare(that: CellProperty): Int = {
    this.name.compare(that.name)
  }

}


case class NamedValueCollector(initial: NamedValue*) {
  private val builder: mutable.Builder[NamedValue, List[NamedValue]] = List.newBuilder[NamedValue]
  initial.foreach {
    builder += _
  }

  def apply(namedValue: NamedValue): Unit = {
    namedValue.value match {
      case ncp: NamedCellProvider[_] =>
        ncp.collectNamedValues(this)
      case _ =>
        builder += namedValue
    }
  }

  def result: List[NamedValue] = builder.result()
}

object CellProperty {
  def apply(namedValue: NamedValue): CellProperty = {
    new CellProperty(namedValue.name, namedValue.value)
  }

  type NamedValues = List[NamedValue]
}