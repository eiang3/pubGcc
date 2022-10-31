package GramTree.Element.stmt;

import GramTree.Element.Exp;
import GramTree.Param;
import GramTree.InheritProp;
import GramTree.TreeElement;
import GramTree.Word;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.MidCode;

import java.io.FileWriter;
import java.io.IOException;

/*
'return' [Exp] ';'
 */
public class ReturnStmt extends Stmt {
    private Word returnWord;
    private Exp exp;

    public ReturnStmt() {
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        MidCode.getInstance().annotate(this.toString());
        if (param.getFunc() == InheritProp.Call && exp != null) {
            super.ergodicMidCode(fileWriter, param);
            MidCode.getInstance().returnStmt(exp.getMidCode());
        } else if (param.getFunc() == InheritProp.Call) {
            MidCode.getInstance().returnStmt();
        } else {
            MidCode.getInstance().mainRetStmt();
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word) {
            Word word = (Word) treeElement;
            if (word.getSym() == Symbol.RETURNTK) {
                this.returnWord = ((Word) treeElement);
            }
        } else if (treeElement instanceof Exp) {
            this.exp = ((Exp) treeElement);
        }
    }

    public Word getReturnWord() {
        return returnWord;
    }

    public boolean hasReturnValue() {
        return exp != null && returnWord != null;
    }
}
