package GramTree.Element.stmt;

import GramTree.Element.Exp;
import GramTree.Param;
import GramTree.TreeElement;
import gccBin.MidCode.AoriginalProcess.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
 Stmt →
 [Exp] ';'
 */
public class ExpStmt extends Stmt{
    private Exp exp;

    public ExpStmt(){

    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        IRGenerate.getInstance().annotate(this.toString());
        super.ergodicMidCode(fileWriter, param);
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Exp) {
            this.exp = (Exp) treeElement;
        }
    }
}
