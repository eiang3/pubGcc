package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * cmp a 1
 */
public class CmpLine extends Line{
    private String t1;
    private String t2;

    private boolean t1IsUse;
    private boolean t2IsUse;
    public CmpLine(String s,int line, TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        parse(ele);
    }

    private void parse(String[] ele){
        t1 = ele[1];
        t2 = ele[2];
        t1IsUse = super.addUse(t1);
        t2IsUse = super.addUse(t2);
    }

    public String getT1() {
        return t1;
    }

    public String getT2() {
        return t2;
    }

    @Override
    public void renameUse(String old,String name){
        if(t1IsUse && t1.equals(old)){
            t1 = name;
        }
        if(t2IsUse && t2.equals(old)){
            t2 = name;
        }
    }
}
