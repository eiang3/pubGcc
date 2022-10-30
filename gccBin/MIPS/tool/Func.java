package gccBin.MIPS.tool;

import GramTree.Element.FuncFParam;
import SymbolTableBin.APIMidCodeSymTable;
import SymbolTableBin.TableSymbol;
import gccBin.MIPS.MIPS;
import gccBin.MIPS.MM;
import gccBin.MIPS.Reg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Func {
    private static Func func;

    private Func() {

    }

    public static Func get() {
        if (func == null) {
            func = new Func();
        }
        return func;
    }

}
