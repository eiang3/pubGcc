package GramTree.Element.stmt;

import GramTree.Element.cond.Cond;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.IRGenerate;
import gccBin.MidCode.original.IRTagManage;

import java.io.FileWriter;
import java.io.IOException;

/*
 Stmt →
 'while' '(' Cond ')' Stmt
 */
public class WhileStmt extends Stmt {
    private Word myWhile;
    private Cond cond;
    private Stmt stmt;

    public WhileStmt() {
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        String end_while = IRTagManage.getInstance().newLabel();
        String cond_again = IRTagManage.getInstance().newLabel();

        Param p = new Param(param);
        p.setWhileOrIfEndLabel(end_while);
        p.setWhileEndLabel(end_while);
        p.setCondBeginLabel(cond_again);

        IRGenerate.getInstance().annotate("while");
        IRGenerate.getInstance().localLabel(cond_again); //    cond_again:
        this.cond.midCodeGen(fileWriter, p); //cond;

        IRGenerate.getInstance().annotate("cond "+cond.toString());

        stmt.midCodeGen(fileWriter, p); //stmt;
        IRGenerate.getInstance().b_label(cond_again);     // jump cond_again
        IRGenerate.getInstance().localLabel(end_while); //end_while:
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word
                && ((Word) treeElement).getSym() == Symbol.WHILETK) {
            this.myWhile = (Word) treeElement;
        } else if (treeElement instanceof Cond) {
            this.cond = (Cond) treeElement;
        } else if (treeElement instanceof Stmt) {
            this.stmt = (Stmt) treeElement;
        }
    }
}
