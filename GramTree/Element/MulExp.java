package GramTree.Element;

import GramTree.*;
import GramTree.Element.UnaryExp.UnaryExp;
import gccBin.Lex.Symbol;
import gccBin.MidCode.MidCode;

import java.io.FileWriter;
import java.io.IOException;

/*
乘除模表达式  MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 */

public class MulExp extends TreeFatherNode {
    private int dimension;
    //for mid code gen
    private int value; //ConstExp
    private String midCode; //Exp

    private MulExp mulExp;
    private Word word;
    private UnaryExp unaryExp;

    public MulExp() {
        super();
        super.setLabel(Label.MulExp);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter,param);
        if (param.getExpKind() == InheritProp.ConstExp) {
            if (this.mulExp == null) {
                this.value = unaryExp.getValue();
            } else {
                this.value = (word.getSym() == Symbol.MULT) ?
                        mulExp.getValue() * unaryExp.getValue() :
                        (word.getSym() == Symbol.DIV) ?
                                mulExp.getValue() / unaryExp.getValue() :
                                mulExp.getValue() % unaryExp.getValue();
            }
        } else  {
            if(mulExp == null){
                this.midCode = MidCode.getInstance().mulExpUnary(
                        unaryExp.getMidCode(), unaryExp.getCoe(),unaryExp.getNegative());
            } else {
                this.midCode = MidCode.getInstance().mulExpTwo(
                        this.mulExp.getMidCode(),this.unaryExp.getMidCode(),
                        this.unaryExp.getCoe(),this.unaryExp.getNegative(),word);
            }
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof UnaryExp) {
            this.dimension = ((UnaryExp) treeElement).getDimension();
            this.unaryExp = (UnaryExp) treeElement;
        } else if (treeElement instanceof Word) {
            this.word = (Word) treeElement;
        } else if (treeElement instanceof MulExp) {
            this.mulExp = (MulExp) treeElement;
        }
    }

    public int getDimension() {
        return dimension;
    }

    public int getValue() {
        return value;
    }

    public String getMidCode() {
        return midCode;
    }
}