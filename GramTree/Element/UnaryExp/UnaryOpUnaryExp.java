package GramTree.Element.UnaryExp;

import GramTree.Element.UnaryOp;
import GramTree.InheritProp;
import GramTree.Param;
import GramTree.TreeElement;
import gccBin.Lex.Symbol;

import java.io.FileWriter;
import java.io.IOException;
/*
由于unaryExp 有系数，比较特殊，所以其中间代码要到Mul阶段才可以判断；
 */
public class UnaryOpUnaryExp extends UnaryExp{
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;

    public UnaryOpUnaryExp(){
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        this.unaryExp.midCodeGen(fileWriter,param);
        if(param.getExpKind() == InheritProp.ConstExp){
            if (this.unaryOp.getSymbol() == Symbol.MINU) {
                super.setValue(-1*this.unaryExp.getValue());
            } else {
                super.setValue(this.unaryExp.getValue());
            }
        } else {
            super.setMidCode(this.unaryExp.getMidCode());
            if(this.unaryOp.getSymbol() == Symbol.MINU) {
                super.setCoe(-1*super.getCoe());
            } else if (this.unaryOp.getSymbol() == Symbol.NOT) {
                super.setNegative(-1*super.getNegative());
            }
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof UnaryOp){
            this.unaryOp = (UnaryOp) treeElement;
        } else if (treeElement instanceof UnaryExp) {
            this.unaryExp = (UnaryExp) treeElement;
            super.setDimension(this.unaryExp.getDimension());
        }
    }
}
