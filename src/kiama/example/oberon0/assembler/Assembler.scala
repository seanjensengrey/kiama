/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2008 Anthony M Sloane, Macquarie University.
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

package kiama.example.oberon0.assembler

/**
 * Simple assembler for RISC machine programs.  Basically adds code
 * emission and symbolic label support to the underlying RISC ISA.
 */
object Assembler {

    import kiama.example.oberon0.machine.RISCISA._
    import scala.collection.mutable.ArrayBuffer
    
    /**
     * The code sequence that is being assembled.
     */
    private val code = new ArrayBuffer[Instr]
    
    /**
     * Emit a RISC instruction.
     */
    def emit (instr : Instr) {
        code += instr
    }

    /**
     * Symbolic labels.
     */
    type Label = Int

    /**
     * The next label to use.
     */
    private var nextlabel = 0

    /**
     * Return a label that hasn't been used before.
     */
    def newlabel : Label = {
        nextlabel += 1
        nextlabel
    }

    /**
     * Map betwen labels and code positions.
     */
    private val labelmap = Map[Label,Int] ()
    
    /**
     * Mark the current code emission position with the given label.
     */
    def mark (label : Label) {
        if ((label <= 0) || (label > nextlabel))
            error ("Assembler.mark: bad label: " + label)
        labelmap (label) = code.length
    }

    /**
     * Return the code sequence that has been emitted.  Symbolic
     * labels are resolved into numeric displacements before the
     * sequence is returned.
     */
    def getcode : Code = {
        for (offset <- 0 until code.length)
            code (offset) match {
                case b : Branch => b.disp = resolve (b.label, offset)
                case _          =>
            }
        code
    }
    
    /**
     * Resolve a symbolic label occurring in an instruction at the
     * given code offset, by returning the equivalent numeric offset.
     */
    private def resolve (label : Label, offset : Int) : Int = {
        if ((label <= 0) || (label > nextlabel))
            error ("Assembler.resolve: bad label: " + label + " at offset " + offset)
        if (! (labelmap contains label))
            error ("Assembler.resolve: unmarked label: " + label)
        labelmap (label) - offset
    }

}
