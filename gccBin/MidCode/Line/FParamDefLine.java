package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * para int a
 * para int b []
 */
public class FParamDefLine extends Line{
    private final String name;
    private final boolean isArray;

    public FParamDefLine(String s, int line,TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        name = ele[2];
        this.isArray = ele.length != 3;
    }

    public String getName() {
        return name;
    }

    public boolean isArray() {
        return isArray;
    }
}
