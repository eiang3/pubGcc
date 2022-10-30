package SymbolTableBin;

import GramTree.Element.*;

import java.io.IOException;

/*
在语法分析过程中，建立符号表 ok
 */
public class APIGramSymTable {
    private static APIGramSymTable instance;

    private TableSymbol nowTable; //目前分析到的符号表
    private TableSymbol rootTable;

    private static final boolean close = false;

    private APIGramSymTable() {
    }

    public static APIGramSymTable getInstance() {
        if (instance == null) {
            instance = new APIGramSymTable();
        }
        return instance;
    }

    //name,type,decl,dim,value|array
    public void addConstDef(ConstDef constDef) throws IOException {
        if (close) return;
        if (constDef.getDimension() == 0) {
            ElementTable elementTable = new ElementTable(
                    constDef.getName(), constDef.getType(), TypeTable.CONST,
                    constDef.getDimension(), constDef.getValue());
            this.nowTable.addElement(elementTable);
        } else  {
            ElementConstArray elementArray = new ElementConstArray(
                    constDef.getName(), constDef.getType(), TypeTable.CONST,
                    constDef.getDimension(), constDef.getArray());
            this.nowTable.addElement(elementArray);
        }
    }

    //name,type,decl,dim
    public void addVarDef(VarDef varDef) throws IOException {
        if (close) return;
        if(varDef.getDimension() == 0) {
            ElementTable elementTable = new ElementTable(
                    varDef.getName(), TypeTable.INT, TypeTable.VAR, // TypeTable.INT改成get有bug
                    varDef.getDimension());
            this.nowTable.addElement(elementTable);
        } else {
             ElementVarArray elementVarArray = new ElementVarArray(
                     varDef.getName(), varDef.getType(), TypeTable.VAR,
                     varDef.getDimension(), varDef.getOneDim(),
                     varDef.getTwoDim());
             this.nowTable.addElement(elementVarArray);
        }
    }

    //name,type,decl,dim
    public void addFuncFParam(FuncFParam funcFParam) throws IOException {
        if (close) return;
        ElementTable elementTable = new ElementTable(
                funcFParam.getName(), funcFParam.getType(),
                TypeTable.FUNC_F_PARAM, funcFParam.getDimension());
        this.nowTable.addElement(elementTable);
    }

    //name,type,decl,dim
    public void addFuncDef(FuncDef funcDef) throws IOException {
        if (close) return;
        ElementFunc funcElement = new ElementFunc(
                funcDef.getName(), TypeTable.FUNC, TypeTable.FUNC, 0);
        funcElement.setReturnType(funcDef.getReturnType());
        funcElement.addParameters(funcDef.getFParams());
        this.rootTable.addElement(funcElement);
    }

    public void addMainFuncDef() throws IOException {
        if (close) return;
        ElementFunc funcElement = new ElementFunc(
                "main", TypeTable.FUNC, TypeTable.FUNC, 0);
        funcElement.setReturnType(TypeTable.INT);
        this.nowTable.addElement(funcElement);
    }

    //构筑语法树部分，已经可以确定不会轻易更改。
    //设置跟符号表
    public void setRootTable(TableSymbol rootTable) {
        if (close) return;
        this.rootTable = rootTable;
    }

    //构建语法树过程中，添加新的符号表
    public void buildTable() {
        if (close) return;
        this.nowTable = new TableSymbol(this.nowTable);
    }

    //一个符号表构建完毕
    public void finishTable() {
        if (close) return;
        this.nowTable = this.nowTable.getFather();
    }

    //得到目前的符号表
    public TableSymbol getNowTable() {
        if (close) return null;
        return nowTable;
    }
}
