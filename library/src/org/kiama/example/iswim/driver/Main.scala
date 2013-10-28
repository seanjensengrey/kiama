/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2010-2013 Dominic R B Verity, Macquarie University.
 * Copyright (C) 2011-2013 Anthony M Sloane, Macquarie University.
 *
 * Kiama is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Kiama is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Kiama.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.kiama
package example.iswim.driver

/**
 * The main driver for compiling and executing ISWIM programs from the
 * command line.
 */

import org.kiama.example.iswim.compiler.Syntax.IswimProg
import org.kiama.example.iswim.compiler.{CodeGenerator, Parser, SemanticAnalysis}
import org.kiama.output.PrettyPrinter
import org.kiama.util.{CompilerWithConfig, Config, Emitter}
import scala.collection.immutable.Seq

/**
 * Configuration for the ISWIM compiler.
 */
class ISWIMConfig (args : Seq[String], emitter : Emitter) extends Config (args, emitter) {
    val dumpBytecode = opt[Boolean] ("dumpBytecode", 'b', descr = "Print the generated bytecode")
    val debug = opt[Boolean] ("debug", descr = "Print debug output during execution")
    val execute = opt[Boolean] ("execute", descr = "Execute the generated bytecode")
}

/**
 * Main program for ISWIM.
 */
object Main extends Parser with CodeGenerator with CompilerWithConfig[IswimProg,ISWIMConfig] {

    import org.kiama.example.iswim.secd.SECDBase.CodeSegment
    import org.kiama.output.PrettyPrinter._
    import org.kiama.util.Messaging

    def createConfig (args : Seq[String], emitter : Emitter = new Emitter) : ISWIMConfig =
        new ISWIMConfig (args, emitter)

    /**
     * Process an ISWIM program by checking for semantic rrors, translating to
     * bytecode, optionally dumping the bytecode and optionally executing.
     */
    override def process (filename : String, iswimcode : IswimProg, config : ISWIMConfig) {

        super.process (filename, iswimcode, config)
        val emitter = config.emitter
        val messaging = new Messaging
        val analysis = new SemanticAnalysis (messaging)
        import analysis.isSemanticallyCorrect

        if (iswimcode->isSemanticallyCorrect) {
            val bytecode = iswimcode->code
            if (config.dumpBytecode ()) {
                emitter.emit("Generated bytecode:")
                val d = (bytecode : CodeSegment).toDoc
                emitter.emitln(pretty(nest(line <> d)))
            }
            if (config.execute ()) {
                val machine = new SECD(bytecode, config) {
                    override def debug : Boolean = config.debug ()
                }
                if (config.debug ()) emitter.emitln("Execution trace:")
                machine.run
                emitter.emitln("Returned value:")
                machine.stack.value match {
                    case List(v) => emitter.emitln(v.toString)
                    case _ => emitter.emitln("** stack corrupted **")
                }
            }
        } else
            messaging.report (emitter)

    }

}
