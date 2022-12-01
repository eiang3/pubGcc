package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * cmp exp exp
 */

public class CmpLine extends Line {
    private String t1;
    private String t2;

    public CmpLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        parse(ele);
    }

    private void parse(String[] ele) {
        t1 = ele[1];
        t2 = ele[2];
        super.addUse_Zero_firstTime(t1);
        super.addUse_Zero_firstTime(t2);
    }

    public String getT1() {
        return t1;
    }

    public String getT2() {
        return t2;
    }

    @Override
    public String getMidCodeLine() {
        return "&cmp " + t1 + " " + t2;
    }

}
