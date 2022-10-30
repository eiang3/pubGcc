package GramTree.Element.stmt;

import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import gccBin.Lex.Symbol;
import gccBin.MidCode.MidCode;

import java.io.FileWriter;
import java.io.IOException;

/*
'break' ';'
 */
public class BreakStmt extends Stmt{

    private Word myBreak;

    public BreakStmt(){
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        MidCode.getInstance().annotate(this.toString());
        MidCode.getInstance().jump(param.getWhileEndLabel());
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof Word
                && ((Word) treeElement).getSym() == Symbol.BREAKTK){
            this.myBreak = (Word) treeElement;
        }
    }
}
