package GramTree.Element.cond;

import GramTree.*;
import GramTree.Element.AddExp;
import gccBin.MidCode.MidCode;

import java.io.FileWriter;
import java.io.IOException;

/*
关系表达式   RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 */

public class RelExp extends TreeFatherNode {
    private RelExp relExp;
    private Word word;
    private AddExp addExp;

    private String midCode;

    public RelExp(){
        super();
        super.setLabel(Label.RelExp);
        this.midCode = " error ";
    }

    @Override
    public void midCodeGen(FileWriter fileWriter,Param param) throws IOException {
        super.ergodicMidCode(fileWriter,param);
        if(relExp != null){
            String t1 = relExp.getMidCode();
            String t2 = addExp.getMidCode();
            MidCode.getInstance().condJump(t1,word,t2, param.getWhileOrIfEndLabel());
        } else {
            if(super.getFather() instanceof EqExp){
                ((EqExp)super.getFather()).setSignalExp(true);
            }
            this.midCode = addExp.getMidCode();
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof RelExp){
            this.relExp = (RelExp) treeElement;
        } else if (treeElement instanceof Word) {
            this.word = (Word) treeElement;
        } else if (treeElement instanceof AddExp) {
            this.addExp = (AddExp) treeElement;
        }
    }

    public String getMidCode() {
        return midCode;
    }
}