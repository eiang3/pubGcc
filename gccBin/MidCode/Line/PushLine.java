package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * push exp
 */
public class PushLine extends Line{
    private String exp;
    private final boolean use;

    private int index;
    public PushLine(String s, int line,TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        exp = ele[1];
        use = super.addUse(exp);
    }

    @Override
    public void renameUse(String old,String name){
        if(use && exp.equals(old)){
            exp = name;
            super.replaceOneUse(old,name);
        }
    }

    public String getExp() {
        return exp;
    }
}
