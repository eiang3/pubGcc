package gccBin.MIPS.tool;

import SymbolTableBin.APIMidCodeSymTable;
import SymbolTableBin.ElementTable;
import SymbolTableBin.TableSymbol;
import SymbolTableBin.TypeTable;
import gccBin.MIPS.Operand;
import gccBin.MIPS.Operate;
import gccBin.MIPS.Reg;

import java.io.FileWriter;
import java.io.IOException;

public class Compute {
    private static Compute compute;

    private FileWriter fileWriter;
    private TableSymbol tableSymbol;

    private Compute() {
    }

    public static Compute get() {
        if (compute == null) {
            compute = new Compute();
        }
        return compute;
    }


}
