package gccBin.MIPS.tool;

import SymbolTableBin.TableSymbol;

import java.io.FileWriter;

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
