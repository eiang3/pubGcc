package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * call tar
 */
public class CallFuncLine extends Line {
    private final String funcName;

    public CallFuncLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        funcName = ele[1];
    }

    public String getFuncName() {
        return funcName;
    }
}
