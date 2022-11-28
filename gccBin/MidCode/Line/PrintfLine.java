package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Judge;

import java.util.HashMap;

/**
 * printf exp
 * printf str_
 */
//
public class PrintfLine extends Line {
    private String t;

    public PrintfLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        t = ele[1];
        super.addUseTemp_Zero(t);
    }

    public String getT() {
        return t;
    }

    @Override
    public void copyPropagation(HashMap<String, String> copy) {
        if(Judge.isTemp(t)) {
            if (copy.containsKey(t)) {
                super.decreaseUse(t);
                t = copy.get(t);
                super.increaseUse(t);
            }
            super.clearTwoUseSet();
            addUseTemp_Zero(t);
        }
    }

    @Override
    public String getMidCodeLine() {
        return "&printf " + t;
    }

}
