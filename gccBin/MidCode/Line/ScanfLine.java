package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * scanf $t1
 */
public class ScanfLine extends Line{
    private String t;
    public ScanfLine(String s,int line, TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        t = ele[1];
    }

    public String getT() {
        return t;
    }
}
