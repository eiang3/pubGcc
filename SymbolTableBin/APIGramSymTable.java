package SymbolTableBin;

import GramTree.Element.*;
import SymbolTableBin.Element.*;

import java.io.IOException;
import java.util.HashMap;

/*
在语法分析过程中，建立符号表 ok
 */
public class APIGramSymTable {
    private static APIGramSymTable instance;

    private TableSymbol nowTable; //目前分析到的符号表
    private TableSymbol rootTable; //根符号表

    private final HashMap<String, Integer> name2Times;//重命名的变量，及其重复次数

    private static final boolean close = false;

    private APIGramSymTable() {
        this.name2Times = new HashMap<>();
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
            ElementConst element = new ElementConst(
                    constDef.getName(), constDef.getType(),
                    constDef.getValue(), constDef.getFalseRow());
            this.nowTable.addElement(element);

            judgeGlobal(element);
            nameClash(element.getName(), element);

        } else {
            ElementConstArray elementArray = new ElementConstArray(
                    constDef.getName(), constDef.getType(), TypeTable.CONST,
                    constDef.getDimension(), constDef.getArray(),
                    constDef.getFalseRow());
            this.nowTable.addElement(elementArray);

            judgeGlobal(elementArray);
            nameClash(elementArray.getName(), elementArray);
        }
    }

    //name,type,decl,dim
    public void addVarDef(VarDef varDef) throws IOException {
        if (close) return;
        if (varDef.getDimension() == 0) {
            ElementVar elementVar = new ElementVar(
                    varDef.getName(), TypeTable.INT, varDef.getFalseRow()); // TypeTable.INT改成get有bug
            this.nowTable.addElement(elementVar);

            nameClash(elementVar.getName(), elementVar);
            judgeGlobal(elementVar);
        } else {
            ElementVarArray elementVarArray = new ElementVarArray(
                    varDef.getName(), varDef.getType(), TypeTable.VAR,
                    varDef.getDimension(), varDef.getOneDim(),
                    varDef.getTwoDim(), varDef.getFalseRow());
            this.nowTable.addElement(elementVarArray);

            nameClash(elementVarArray.getName(), elementVarArray);
            judgeGlobal(elementVarArray);
        }
    }

    //name,type,decl,dim
    public void addFuncFParam(FuncFParam funcFParam) throws IOException {
        if (close) return;
        ElementFParam elementFParam = new ElementFParam(
                funcFParam.getName(), funcFParam.getType(),
                TypeTable.FUNC_F_PARAM, funcFParam.getDimension(),
                funcFParam.getIndex(), funcFParam.getFalseRow());
        this.nowTable.addElement(elementFParam);

        nameClash(elementFParam.getName(), elementFParam);
    }

    //name,type,decl,dim
    public void addFuncDef(FuncDef funcDef) throws IOException {
        if (close) return;
        ElementFunc funcElement = new ElementFunc(
                funcDef.getName(), TypeTable.FUNC, TypeTable.FUNC, 0,
                funcDef.getFalseRow());
        funcElement.setReturnType(funcDef.getReturnType());
        funcElement.addParameters(funcDef.getFParams());
        this.rootTable.addElement(funcElement);

        nameClash(funcElement.getName(), funcElement);
    }

    public void addMainFuncDef() throws IOException {
        if (close) return;
        ElementFunc funcElement = new ElementFunc(
                "main", TypeTable.FUNC, TypeTable.FUNC,
                0, 0);//main 是关键字，不会冲突吧？
        funcElement.setReturnType(TypeTable.INT);
        this.nowTable.addElement(funcElement);

        nameClash("main", funcElement);
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

    public void nameClash(String name, ElementTable elementTable) {
        if (name2Times.containsKey(name)) {
            int times = name2Times.get(name);
            elementTable.setSubScript(times);
            name2Times.put(name, times + 1);
            APIIRSymTable.getInstance().addToRedefineElement(elementTable, nowTable);
        } else {
            name2Times.put(name, 0);
        }
    }

    public void judgeGlobal(ElementTable elementTable) {
        if (nowTable == rootTable) elementTable.setGlobal(true);
    }
}

