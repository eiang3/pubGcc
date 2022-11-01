package GramTree.Element;

import GramTree.*;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
加减表达式   AddExp → MulExp | AddExp ('+' | '−') MulExp
 */
public class AddExp extends TreeFatherNode {
    private int dimension;
    //for midCode
    private int value; //ConstExp
    private String midCode; //

    private AddExp addExp;
    private Word word;
    private MulExp mulExp;

    public AddExp() {
        super();
        super.setLabel(Label.AddExp);

        this.dimension = 0;
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter,param);
        if (param.getExpKind() == InheritProp.ConstExp) { //AddExp - MulExp
            if (this.addExp == null) {
                this.value = this.mulExp.getValue();
            } else { //AddExp - AddExP +|- MulExp
                this.value = (word.getSym() == Symbol.PLUS) ?
                        this.addExp.getValue() + this.mulExp.getValue() :
                        this.addExp.getValue() - this.mulExp.getValue();
            }
        } else  {
            if (this.addExp != null) {
                this.midCode = IRGenerate.getInstance().addExpTwo(
                        this.addExp.getMidCode(), this.mulExp.getMidCode(),
                        word);
            } else {
                this.midCode = this.mulExp.getMidCode();
            }
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof MulExp) {
            this.dimension = ((MulExp) treeElement).getDimension();
            this.mulExp = (MulExp) treeElement;
        } else if (treeElement instanceof Word) {
            this.word = (Word) treeElement;
        } else if (treeElement instanceof AddExp) {
            this.addExp = (AddExp) treeElement;
        }
    }

    public int getValue() {
        return value;
    }

    public int getDimension() {
        return dimension;
    }

    public String getMidCode() {
        return midCode;
    }
}