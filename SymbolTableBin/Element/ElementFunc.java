package SymbolTableBin.Element;

import GramTree.Element.FuncFParam;
import SymbolTableBin.TypeTable;

import java.util.ArrayList;
/*
函数符号表 ok
 */
public class ElementFunc extends ElementTable {
    private final ArrayList<FuncFParam> params; //参数
    private TypeTable returnType;   //返回值

    public ElementFunc(String name, TypeTable type, TypeTable decl,
                       int dimension){
        super(name,type,decl,dimension);
        this.params = new ArrayList<>();
    }

    public void addParameters(ArrayList<FuncFParam> params){
        this.params.addAll(params);
    }

    public void setReturnType(TypeTable returnType) {
        this.returnType = returnType;
    }

    public TypeTable getReturnType() {
        return returnType;
    }

    public ArrayList<FuncFParam> getParams() {
        return params;
    }
}
