package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Judge;

import java.util.HashMap;

/**
 * push exp
 */
//
public class PushLine extends Line {
    private String exp;

    public PushLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        exp = ele[1];
        super.addUseTemp_Zero(exp);
    }

    public String getExp() {
        return exp;
    }

    @Override
    public void copyPropagation(HashMap<String, String> copy) {
        if(Judge.isTemp(exp)) {
            if (copy.containsKey(exp)) {
                super.exchangeTempUseZero(copy.get(exp),exp);
                exp = copy.get(exp);
            }
        }
    }

    @Override
    public String getMidCodeLine() {
        return "&push " + exp;
    }
}
