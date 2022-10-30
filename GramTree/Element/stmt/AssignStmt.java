package GramTree.Element.stmt;

import GramTree.InheritProp;
import GramTree.Element.Exp;
import GramTree.Element.LVal;
import GramTree.Param;
import GramTree.TreeElement;
import gccBin.MidCode.MidCode;

import java.io.FileWriter;
import java.io.IOException;

/*
Stmt →

LVal '=' Exp ';'     |
LVal '=' 'getint' '(' ')' ';'
 */
public class AssignStmt extends Stmt {
    private LVal lVal;
    private Exp exp;

    public AssignStmt() {
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        MidCode.getInstance().annotate(this.toString());
        Param p = new Param(param);
        p.setExpKind(InheritProp.LValAssign);
        lVal.midCodeGen(fileWriter,p);
        if (exp != null) {
            exp.midCodeGen(fileWriter,param);
            MidCode.getInstance().assignStmtExp(lVal.getMidCode(), exp.getMidCode());
        } else {
            MidCode.getInstance().assignStmtScanf(lVal.getMidCode());
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof LVal) {
            this.lVal = (LVal) treeElement;
        } else if (treeElement instanceof Exp) {
            this.exp = (Exp) treeElement;
        }
    }
}
