package GramTree.Element.cond;

import GramTree.*;
import gccBin.MidCode.original.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
逻辑与表达式  LAndExp → EqExp | LAndExp '&&' EqExp
 */
public class LAndExp extends TreeFatherNode {
    private LAndExp lAndExp;
    private Word word;
    private EqExp eqExp;

    //private String midCode;
    private boolean signalExp;

    public LAndExp() {
        super();
        super.setLabel(Label.LAndExp);
        this.signalExp = false;
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        if (lAndExp != null) {
            lAndExp.midCodeGen(fileWriter, param);
            eqExp.midCodeGen(fileWriter, param);
            String ret = eqExp.getMidCode();
            IRGenerate.getInstance().b_false(ret,param.getWhileOrIfEndLabel());
        } else {
            eqExp.midCodeGen(fileWriter, param);
            String ret = eqExp.getMidCode();
            IRGenerate.getInstance().b_false(ret,param.getWhileOrIfEndLabel());
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof LAndExp) {
            this.lAndExp = (LAndExp) treeElement;
        } else if (treeElement instanceof Word) {
            this.word = (Word) treeElement;
        } else if (treeElement instanceof EqExp) {
            this.eqExp = (EqExp) treeElement;
        }
    }

    public void setSignalExp(boolean signalExp) {
        this.signalExp = signalExp;
    }
}