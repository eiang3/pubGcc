package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * cmp a 1
 */
public class CmpLine extends Line{
    private String t1;
    private String t2;
    public CmpLine(String s,int line, TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        parse(ele);
    }

    private void parse(String[] ele){
        t1 = ele[1];
        t2 = ele[2];
    }

    public String getT1() {
        return t1;
    }

    public String getT2() {
        return t2;
    }
}
