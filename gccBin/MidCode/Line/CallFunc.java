package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * call tar
 */
public class CallFunc extends Line{
    private String funcName;
    public CallFunc(String s,int line, TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        funcName = ele[1];
    }

    public String getFuncName() {
        return funcName;
    }
}
