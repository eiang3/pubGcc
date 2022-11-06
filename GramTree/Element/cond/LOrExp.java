package GramTree.Element.cond;

import GramTree.*;
import gccBin.MidCode.original.IRGenerate;
import gccBin.MidCode.original.IRTagManage;

import java.io.FileWriter;
import java.io.IOException;

/*
逻辑或表达式  LOrExp → LAndExp | LOrExp '||' LAndExp
 */
public class LOrExp extends TreeFatherNode {
    private LOrExp lOrExp;
    private Word word;
    private LAndExp lAndExp;

    public LOrExp() {
        super();
        super.setLabel(Label.LOrExp);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {

        if(lOrExp != null){
            String nextOr = IRTagManage.getInstance().newLabel();

            Param p = new Param(param);
            p.setWhileOrIfEndLabel(nextOr);
            lOrExp.midCodeGen(fileWriter,p);

            IRGenerate.getInstance().localLabel(nextOr);
            lAndExp.midCodeGen(fileWriter,param);
        } else {
            lAndExp.midCodeGen(fileWriter,param);
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof LOrExp) {
            this.lOrExp = (LOrExp) treeElement;
        } else if (treeElement instanceof Word) {
            this.word = (Word) treeElement;
        } else if (treeElement instanceof LAndExp) {
            this.lAndExp = (LAndExp) treeElement;
        }
    }

}