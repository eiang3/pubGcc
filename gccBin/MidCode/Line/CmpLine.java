package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Judge;

import java.util.HashMap;

/**
 * cmp exp exp
 */
//
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
        super.addUseTemp_Zero(t1);
        super.addUseTemp_Zero(t2);
    }

    public String getT1() {
        return t1;
    }

    public String getT2() {
        return t2;
    }

    @Override
    public void copyPropagation(HashMap<String, String> copy) {
        if(Judge.isTemp(t1)) {
            if (copy.containsKey(t1)) {
                super.exchangeTempUseZero(copy.get(t1),t1);
                t1 = copy.get(t1);
            }
        }
        if(Judge.isTemp(t2)) {
            if (copy.containsKey(t2)) {
                super.exchangeTempUseZero(copy.get(t2),t2);
                t2 = copy.get(t2);
            }
        }
    }

    @Override
    public String getMidCodeLine() {
        return "&cmp " + t1 + " " + t2;
    }
}
