package GramTree.Element;

import GramTree.*;
import SymbolTableBin.APIErrorSymTable;
import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementTable;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
LVal → Ident {'[' Exp ']'} // c k
 */
public class LVal extends TreeFatherNode {
    private int dimension;
    //for midCode
    private int value;

    private Word ident;
    private String name;
    private Exp exp1;
    private Exp exp2;
    //
    private String midCode;

    public LVal() {
        super();
        super.setLabel(Label.LVal);
        this.dimension = 0;
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        if (param.getExpKind() == InheritProp.ConstExp) { //常量直接计算
            constOp(fileWriter, param);
        } else if (param.getFuncRParams() == InheritProp.FuncRParam) { //实参，注意数组指针
            funcRParamOp(fileWriter, param);
        } else {
            elseOp(fileWriter, param);
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word) {
            Word word = (Word) treeElement;
            if (word.getSym() == Symbol.IDENFR) {
                this.ident = word;
                this.name = word.getToken();
                ElementTable tableElement =
                        APIErrorSymTable.getInstance().getElement(word.getToken());
                if (tableElement != null) {
                    this.dimension = tableElement.getDimension(); //陈 负数？
                }
            }
        } else if (treeElement instanceof Exp) {
            if (this.exp1 == null) exp1 = (Exp) treeElement;
            else exp2 = (Exp) treeElement;
            this.dimension--;
        }
    }

    private void constOp(FileWriter fileWriter, Param param) throws IOException {
        if (exp1 == null && exp2 == null) {
            this.value = APIIRSymTable.getInstance()
                    .findValue(super.getTableSymbol(), name);
        } else if (exp1 != null && exp2 == null) {
            exp1.midCodeGen(fileWriter, param);
            this.value = APIIRSymTable.getInstance()
                    .findValue(super.getTableSymbol(), name, exp1.getValue());
        } else if (exp1 != null && exp2 != null) {
            exp1.midCodeGen(fileWriter, param);
            exp2.midCodeGen(fileWriter, param);
            this.value = APIIRSymTable.getInstance()
                    .findValue(super.getTableSymbol(), name, exp1.getValue(), exp2.getValue());
        }
    }

    private void funcRParamOp(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter, param); //如果是函数的话，那下标里一定是数值了。
        if (APIIRSymTable.getInstance().isConstNum(super.getTableSymbol(), name)) {
            this.midCode = String.valueOf(APIIRSymTable.getInstance().getConstNum
                    (super.getTableSymbol(), name));
        } else if (exp1 == null && exp2 == null) {
            this.midCode = IRGenerate.getInstance().lValRParam
                    (super.getTableSymbol(), name);
        } else if (exp1 != null && exp2 == null) { //一维的数组
            this.midCode = IRGenerate.getInstance().lValRParam
                    (super.getTableSymbol(), name, exp1.getMidCode());
        } else if (exp1 != null && exp2 != null) {
            this.midCode = IRGenerate.getInstance().lValRParam(
                    super.getTableSymbol(), name,
                    exp1.getMidCode(), exp2.getMidCode());
        }
    }

    private void elseOp(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter, param);
        if (APIIRSymTable.getInstance().isConstNum(super.getTableSymbol(), name)) {
            this.midCode = String.valueOf(APIIRSymTable.getInstance().getConstNum
                    (super.getTableSymbol(), name));
        } else if (exp1 == null && exp2 == null) {
            this.midCode = IRGenerate.getInstance().lValNormal(super.getTableSymbol(),name, param);
        } else if (exp1 != null && exp2 == null) { //一维的数组
            this.midCode = IRGenerate.getInstance().lValNormal(name, exp1.getMidCode(), param);
        } else if (exp1 != null && exp2 != null) {
            int len = APIIRSymTable.getInstance().findTwoDimArrayLen(
                    super.getTableSymbol(), this.name);
            this.midCode = IRGenerate.getInstance().lValNormal(name,
                    exp1.getMidCode(), exp2.getMidCode(), len, param);
        }
    }

    public int getValue() {
        return value;
    }

    public int getDimension() {
        return dimension;
    }

    public String getName() {
        return name;
    }

    public String getMidCode() {
        return midCode;
    }

    public Word getIdent() {
        return ident;
    }

}