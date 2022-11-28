package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Judge;

import java.util.HashMap;

/**
 * scanf $t1
 */
//
public class ScanfLine extends Line {
    private String exp;

    public ScanfLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        exp = ele[1];
        super.addGenTemp_Zero(exp);
    }

    public String getExp() {
        return exp;
    }

    @Override
    public String getMidCodeLine() {
        return "&scanf " + exp;
    }
}
