package GramTree.Element;

import GramTree.*;
import SymbolTableBin.APIMidCodeSymTable;
import SymbolTableBin.TypeTable;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.MidCode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
函数定义    FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g j
 */

public class FuncDef extends TreeFatherNode {
    private FuncType funcType;
    private String name;
    private FuncFParams funcFParams;
    private ArrayList<FuncFParam> fParams;
    private TypeTable returnType;
    private Block block;

    public FuncDef() {
        super();
        super.setLabel(Label.FuncDef);
        fParams = new ArrayList<>();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        Param p = new Param(param);
        p.setFunc(InheritProp.Call);

        funcType.midCodeGen(fileWriter, p);
        MidCode.getInstance().funcDef(returnType, name);
        MidCode.getInstance().inBlock();

        if (funcFParams != null) funcFParams.midCodeGen(fileWriter, p);
        block.midCodeGen(fileWriter, p);
        if (APIMidCodeSymTable.getInstance().getFuncElement(name).getReturnType()
                == TypeTable.VOID) {
            fileWriter.write("ret\n");
        }   //如果返回类型是void 不论是否有返回语句，最后都要有返回语句
        MidCode.getInstance().leaveBlock();
    }

    @Override
    public void addChildOperate(TreeElement element) {
        if (element instanceof Word) {
            Word word = (Word) element;
            if (word.getSym() == Symbol.IDENFR) {
                this.name = word.getToken();
            }
        } else if (element instanceof FuncType) {
            funcType = (FuncType) element;
            this.returnType = funcType.getType();
        } else if (element instanceof FuncFParams) {
            funcFParams = (FuncFParams) element;
            this.fParams = funcFParams.getParams();
        } else if (element instanceof Block) {
            this.block = (Block) element;
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<FuncFParam> getFParams() {
        return fParams;
    }

    public TypeTable getReturnType() {
        return returnType;
    }
}
