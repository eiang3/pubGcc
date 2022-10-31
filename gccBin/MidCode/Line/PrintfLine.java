package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * printf exp
 * printf str_
 */
public class PrintfLine extends Line{
    private String t;
    private boolean tIsUse;

    public PrintfLine(String s, int line,TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        t = ele[1];
        tIsUse = super.addUse(t);
    }

    @Override
    public void renameUse(String old,String name){
        if(tIsUse && t.equals(old)){
            t = name;
        }
    }

    public String getT() {
        return t;
    }
}
