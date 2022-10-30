package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * printf exp
 * printf str_
 */
public class PrintfLine extends Line{
    private String t;
    public PrintfLine(String s, int line,TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        t = ele[1];
        super.addUse(t);
    }

    public String getT() {
        return t;
    }
}
