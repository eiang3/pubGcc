package GramTree.Element.UnaryExp;

import GramTree.Element.Exp;
import GramTree.Element.FuncRParams;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import SymbolTableBin.APIErrorSymTable;
import SymbolTableBin.TypeTable;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.MidCode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
一元表达式   UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // c d e j
        | UnaryOp UnaryExp
 */
public class FuncUnaryExp extends UnaryExp { //不会有函数调用？
    private Word ident;
    private FuncRParams funcRParams;

    public FuncUnaryExp() {
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter, param);
        if (funcRParams != null) {
            super.setMidCode(MidCode.getInstance().funcCall(ident.getToken(),
                    funcRParams.getExps()));
        } else {
            super.setMidCode(MidCode.getInstance().funcCall(ident.getToken()));
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word &&
                ((Word) treeElement).getSym() == Symbol.IDENFR) {
            this.ident = (Word) treeElement;

            if (APIErrorSymTable.getInstance().
                    getFuncDef(ident.getToken()) != null) {
                TypeTable myType = APIErrorSymTable.getInstance().
                        getFuncDef(ident.getToken()).getReturnType();
                super.setDimension(myType == TypeTable.INT ? 0 : -1);
            }
        } else if (treeElement instanceof FuncRParams) {
            this.funcRParams = (FuncRParams) treeElement;
        }
    }

    public ArrayList<Exp> getFuncRParams() {
        if (this.funcRParams != null) return funcRParams.getExps();
        else return new ArrayList<>();
    }

    public Word getFuncWord() {
        return ident;
    }

    public String getFuncName(){
        return ident.getToken();
    }
}
