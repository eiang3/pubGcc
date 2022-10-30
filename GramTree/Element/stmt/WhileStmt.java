package GramTree.Element.stmt;

import GramTree.Element.cond.Cond;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import gccBin.Lex.Symbol;
import gccBin.MidCode.MidCode;
import gccBin.MidCode.MidTagManage;

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
        String end_while = MidTagManage.getInstance().newLabel();
        String cond_again = MidTagManage.getInstance().newLabel();

        Param p = new Param(param);
        p.setWhileOrIfEndLabel(end_while);
        p.setWhileEndLabel(end_while);
        p.setCondLabel(cond_again);

        MidCode.getInstance().write("# while ( ");
        MidCode.getInstance().localLabel(cond_again); //    cond_again:
        this.cond.midCodeGen(fileWriter, p); //cond;

        MidCode.getInstance().write(cond.toString()+")\n");

        stmt.midCodeGen(fileWriter, p); //stmt;
        MidCode.getInstance().jump(cond_again);     // jump cond_again
        MidCode.getInstance().localLabel(end_while); //end_while:
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
