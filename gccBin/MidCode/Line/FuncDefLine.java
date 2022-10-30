package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import SymbolTableBin.TypeTable;

/**
 * int foo ()
 */
public class FuncDefLine extends Line{
    private TypeTable returnType;
    private String name;
    public FuncDefLine(String str , int line,TableSymbol tableSymbol,String[] ele){
        super(str,line,tableSymbol);
        if(ele[0].equals("void")) this.returnType = TypeTable.VOID;
        else if(ele[0].equals("int")) this.returnType = TypeTable.INT;
        this.name = ele[1];
    }

    public TypeTable getReturnType() {
        return returnType;
    }

    public String getName() {
        return name;
    }
}
