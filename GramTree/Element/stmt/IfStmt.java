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

'if' '(' Cond ')' Stmt [ 'else' Stmt ]

 */
public class IfStmt extends Stmt {
    private Word myIf;
    private Cond cond;
    private Stmt ifStmt;
    private Word myElse;
    private Stmt elseStmt;

    public IfStmt() {
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        IRGenerate.getInstance().annotate("if");
        if (elseStmt != null) {
            String start_else = IRTagManage.getInstance().newLabel();
            String end_if = IRTagManage.getInstance().newLabel();

            Param p = new Param(param);
            p.setWhileOrIfEndLabel(start_else);

            cond.midCodeGen(fileWriter, p); //失败,跳到start_else
            IRGenerate.getInstance().annotate("cond "+cond.toString());

            ifStmt.midCodeGen(fileWriter, p); //if stmt;
            IRGenerate.getInstance().jump(end_if); //jump end_if

            IRGenerate.getInstance().localLabel(start_else); // start_else:
            IRGenerate.getInstance().annotate("else");
            elseStmt.midCodeGen(fileWriter, p);  //else if;
            IRGenerate.getInstance().localLabel(end_if); //end_if:
        } else {
            String end_if = IRTagManage.getInstance().newLabel();
            Param p = new Param(param);
            p.setWhileOrIfEndLabel(end_if);

            cond.midCodeGen(fileWriter, p); //失败，end_if
            ifStmt.midCodeGen(fileWriter, p); //if stmt;
            IRGenerate.getInstance().localLabel(end_if); //end_if
        }

    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word) {
            Word word = (Word) treeElement;
            if (word.getSym() == Symbol.IFTK) {
                this.myIf = word;
            } else if (word.getSym() == Symbol.ELSETK) {
                this.myElse = word;
            }
        } else if (treeElement instanceof Cond) {
            this.cond = (Cond) treeElement;
        } else if (treeElement instanceof Stmt) {
            if (this.ifStmt == null) {
                this.ifStmt = (Stmt) treeElement;
            } else {
                this.elseStmt = (Stmt) treeElement;
            }
        }
    }
}
