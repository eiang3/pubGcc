package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * ret t1
 */
public class RetLine extends Line{
    private String exp;

    public RetLine (String s, int line,TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        if(ele.length == 2){
            exp = ele[1];
        }
        super.addUse(exp);
    }

    public String getExp() {
        return exp;
    }
}
