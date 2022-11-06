package GramTree.Element.cond;

import GramTree.*;
import gccBin.MidCode.original.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
相等性表达式  EqExp → RelExp | EqExp ('==' | '!=') RelExp
 */
public class EqExp extends TreeFatherNode {
    private EqExp eqExp;
    private Word word;
    private RelExp relExp;

    private String midCode;

    private boolean signalExp;

    public EqExp() {
        super();
        super.setLabel(Label.EqExp);
        this.signalExp = false;
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter, param);
        if (eqExp != null) {
            String t1 = eqExp.getMidCode();
            String t2 = relExp.getMidCode();
            this.midCode = IRGenerate.getInstance().condJump(t1, word, t2);
        } else {
            this.midCode = relExp.getMidCode();
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof EqExp) {
            this.eqExp = (EqExp) treeElement;
        } else if (treeElement instanceof Word) {
            this.word = (Word) treeElement;
        } else if (treeElement instanceof RelExp) {
            this.relExp = (RelExp) treeElement;
        }
    }

    public String getMidCode() {
        return midCode;
    }

    public boolean getSignalExp() {
        return signalExp;
    }

    public void setSignalExp(boolean signalExp) {
        this.signalExp = signalExp;
    }
}