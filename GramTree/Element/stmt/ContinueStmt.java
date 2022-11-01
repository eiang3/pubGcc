package GramTree.Element.stmt;

import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
'continue' ';'
 */
public class ContinueStmt extends Stmt{
    private Word myContinue;

    public ContinueStmt(){
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        IRGenerate.getInstance().annotate(this.toString());
        IRGenerate.getInstance().jump(param.getCondLabel());
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof Word
                && ((Word) treeElement).getSym() == Symbol.CONTINUETK){
            this.myContinue = (Word) treeElement;
        }
    }

}
