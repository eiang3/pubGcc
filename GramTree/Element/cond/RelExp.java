package GramTree.Element.cond;

import GramTree.*;
import GramTree.Element.AddExp;
import gccBin.MidCode.AoriginalProcess.IRGenerate;

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
        if(relExp != null){ //错误的话
            String t1 = relExp.getMidCode();
            String t2 = addExp.getMidCode();
            midCode = IRGenerate.getInstance().condJump(t1,word,t2);
        } else {
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