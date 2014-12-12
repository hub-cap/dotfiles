/**
*  Copyright (c) 2010, Aemon Cannon
*  All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of ENSIME nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
*  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL Aemon Cannon BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.ensime.sbt
package util
import scala.util.parsing.combinator._
import scala.util.parsing.input

abstract class SExp {
  def toPPReadableString: String = toReadableString
  def toReadableString: String = toString
  def toScala: Any = toString
}

case class SExpList(items: Iterable[SExp]) extends SExp with Iterable[SExp] {

  override def iterator = items.iterator

  override def toString = "(" + items.mkString(" ") + ")"

  override def toPPReadableString = {
    def indent(s: String): String = {
      val indentation = "  "
      s.split("\n").map(indentation + _).mkString("\n")
    }
    def group(items: List[SExp]): List[String] = items match {
      case KeywordAtom(kw) :: value :: tl =>
        val item = indent(kw) + " " + indent(value.toPPReadableString).replaceAll("^\\s+", "") // trimLeft
        item :: group(tl)
      case hd :: tl =>
        indent(hd.toPPReadableString) :: group(tl)
      case Nil =>
        Nil
    }
    "(\n" + group(items.toList).mkString("\n") + "\n)"
  }

  override def toReadableString = {
    "(" + items.map { _.toReadableString }.mkString(" ") + ")"
  }

  def toKeywordMap(): KeyMap = {
    var m = KeyMap()
    items.toList.sliding(2, 2).foreach {
      case (key: KeywordAtom) :: (sexp: SExp) :: rest => {
        m += (key -> sexp)
      }
      case other => {}
    }
    m
  }

  def toSymbolMap(): SymMap = {
    var m = SymMap()
    items.sliding(2, 2).foreach {
      case SymbolAtom(key) ::(sexp: SExp) :: rest => {
        m += (Symbol(key) -> sexp.toScala)
      }
      case _ => {}
    }
    m
  }
}


object BooleanAtom {

  def unapply(z: SExp): Option[Boolean] = z match {
    case TruthAtom() => Some(true)
    case NilAtom() => Some(false)
    case _ => None
  }

}

abstract class BooleanAtom extends SExp {
  def toBool: Boolean
  override def toScala = toBool
}

case class NilAtom() extends BooleanAtom {
  override def toString = "nil"
  override def toBool: Boolean = false

}
case class TruthAtom() extends BooleanAtom {
  override def toString = "t"
  override def toBool: Boolean = true
  override def toScala: Boolean = true
}
case class StringAtom(value: String) extends SExp {
  override def toString = value
  override def toReadableString = {
    val printable = value.replace("\\", "\\\\").replace("\"", "\\\"");
    "\"" + printable + "\""
  }
}
case class IntAtom(value: Int) extends SExp {
  override def toString = String.valueOf(value)
  override def toScala = value
}
case class SymbolAtom(value: String) extends SExp {
  override def toString = value
}
case class KeywordAtom(value: String) extends SExp {
  override def toString = value
}

object SExp extends RegexParsers {

  import scala.util.matching.Regex

  private lazy val string = regexGroups("""\"((?:[^\"\\]|\\.)*)\"""".r) ^^ { m =>
    StringAtom(m.group(1).replace("\\\\", "\\"))
  }
  private lazy val symbol = regex("[a-zA-Z][a-zA-Z0-9-:]*".r) ^^ { s =>
    if(s == "nil") NilAtom()
    else if(s == "t") TruthAtom()
    else SymbolAtom(s)
  }
  private lazy val keyword = regex(":[a-zA-Z][a-zA-Z0-9-:]*".r) ^^ KeywordAtom
  private lazy val number = regex("-?[0-9]+".r) ^^ { s => IntAtom(s.toInt) }
  private lazy val list = literal("(") ~> rep(expr) <~ literal(")") ^^ SExpList.apply
  private lazy val expr: Parser[SExp] = list | keyword | string | number | symbol

  def read(r: input.Reader[Char]): SExp = {
    val result: ParseResult[SExp] = expr(r)
    result match {
      case Success(value, next) => value
      case Failure(errMsg, next) => {
        println(errMsg)
        NilAtom()
      }
      case Error(errMsg, next) => {
        println(errMsg)
        NilAtom()
      }
    }
  }

  /** A parser that matches a regex string and returns the match groups */
  private def regexGroups(r: Regex): Parser[Regex.Match] = new Parser[Regex.Match] {
    def apply(in: Input) = {
      val source = in.source
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      (r findPrefixMatchOf (source.subSequence(start, source.length))) match {
        case Some(matched) => Success(matched, in.drop(start + matched.end - offset))
        case None =>
        Failure("string matching regex `" + r +
          "' expected but `" +
          in.first + "' found", in.drop(start - offset))
      }
    }
  }

  def apply(s: String): StringAtom = strToSExp(s)

  def apply(i: Int): IntAtom = intToSExp(i)

  def apply(b: Boolean): BooleanAtom = boolToSExp(b)

  def apply(items: SExp*): SExpList = sexp(items)

  def apply(items: Iterable[SExp]): SExpList = sexp(items)

  def apply(map: KeyMap): SExpList = {
    val buf = scala.collection.mutable.ListBuffer[SExp]()
    map.map{ pair =>
      buf += pair._1
      buf += pair._2
    }
    SExpList(buf.toList)
  }

  // Helpers for common case of key,val prop-list.
  // Omit keys for nil values.
  def propList(items: (String, SExp)*): SExpList = {
    propList(items)
  }
  def propList(items: Iterable[(String, SExp)]): SExpList = {
    val nonNil = items.filter {
      case (s, NilAtom()) => false
      case (s, SExpList(items)) if items.isEmpty => false
      case _ => true
    }
    SExpList(nonNil.flatMap(ea => List(key(ea._1), ea._2)))
  }

  implicit def strToSExp(str: String): StringAtom = {
    StringAtom(str)
  }

  def key(str: String): KeywordAtom = {
    KeywordAtom(str)
  }

  def sym(str: String): SymbolAtom = {
    SymbolAtom(str)
  }

  def sexp(items: Iterable[SExp]): SExpList = {
    SExpList(items)
  }

  def sexp(items: SExp*): SExpList = {
    SExpList(items)
  }

  implicit def intToSExp(value: Int): IntAtom = {
    IntAtom(value)
  }

  implicit def boolToSExp(value: Boolean): BooleanAtom = {
    if (value) {
      TruthAtom()
    } else {
      NilAtom()
    }
  }

  implicit def symbolToSExp(value: Symbol): SExp = {
    if (value == 'nil) {
      NilAtom()
    } else {
      SymbolAtom(value.toString.drop(1))
    }
  }

  implicit def nilToSExpList(nil: NilAtom): SExp = {
    SExpList(List())
  }

  implicit def toSExp(o: SExpable): SExp = {
    o.toSExp
  }

  implicit def toSExpable(o: SExp): SExpable = new SExpable {
    def toSExp = o
  }

  implicit def listToSExpable(o: Iterable[SExpable]): SExpable = new Iterable[SExpable] with SExpable {
    override def iterator = o.iterator
    override def toSExp = SExp(o.map { _.toSExp })
  }


  def main(args:Array[String]){
    def readStr(s:String) = {
      val chars = new Array[Char](s.length)
      s.getChars(0, s.length, chars, 0)
      val r = new input.CharArrayReader(chars)
      SExp.read(r)
    }
    def check(s:String, r:String) {
      assert(readStr(s).toString() == r, "Failed at: " + s)
    }
    check("()", "()")
    check("(nil)", "(nil)")
    check("(t)", "(t)")
    check("(a b c d)", "(a b c d)")
    check("(a b c () nil)", "(a b c () nil)")
    check("(a b c () trait)", "(a b c () trait)")
    check("(a b c () t())", "(a b c () t ())")
    check("(a b c\n() nil(nil\n t))", "(a b c () nil (nil t))")
    check("(nildude)", "(nildude)")


    assert(readStr("t\n").toScala == true, "t should be true!")
    assert(readStr("t\n\t").toScala == true, "t should be true!")
    assert(readStr("t\n\t").toScala == true, "t should be true!")
    assert(readStr("t ").toScala == true, "t should be true!")
    assert(readStr("t").toScala == true, "t should be true!")


    assert(readStr("nil\n").toScala == false, "nil should be false!")
    assert(readStr("nil\n\t").toScala == false, "nil should be false!")
    assert(readStr("nil\n\t").toScala == false, "nil should be false!")
    assert(readStr("nil ").toScala == false, "nil should be false!")
    assert(readStr("nil").toScala == false, "nil should be false!")

    val map = readStr("(:use-sbt t :dude 1212)") match{
      case ls:SExpList => ls.toKeywordMap()
    }
    map.get(key(":use-sbt")) match{
      case Some(v) => assert(v.toScala == true)
      case None => assert(false)
    }

  }

}

abstract trait SExpable {
  implicit def toSExp(): SExp
}

