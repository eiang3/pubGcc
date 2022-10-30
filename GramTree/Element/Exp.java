package GramTree.Element;

import GramTree.*;
import gccBin.MidCode.original.MidCode;

import java.io.FileWriter;
import java.io.IOException;
/*
 Exp → AddExp
 */
public class Exp extends TreeFatherNode {
    private int dimension;

    private AddExp addExp;

    private String midCode;

    private int value;

    public Exp() {
        super();
        super.setLabel(Label.Exp);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        if(param.getExpKind() == InheritProp.ConstExp){
            this.addExp.midCodeGen(fileWriter, param);
            this.value = this.addExp.getValue();
        }
        if(param.getFuncRParams() == InheritProp.FuncRParam){
            //如果是参数，注意数组指针运算，最后结果根据情况乘4
            this.addExp.midCodeGen(fileWriter, param);
            if(param.getFParamDim() == 0) {
                this.midCode = this.addExp.getMidCode();
            } else {
                this.midCode = MidCode.getInstance().mulFour(this.addExp.getMidCode());
            }
        }
        else  if(param.getInitial() == InheritProp.NULL) {
            //如果是赋初值的，则之后再算,以防止数组过长
            Param p = new Param(param);
            p.setExpKind(InheritProp.VarExp);
            this.addExp.midCodeGen(fileWriter, p);
            this.midCode = this.addExp.getMidCode();
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof AddExp) {
            this.addExp = (AddExp) treeElement;
            this.dimension = ((AddExp) treeElement).getDimension();
        }
    }

    public int getDimension() {
        return dimension;
    }

    public String getMidCode() {
        return midCode;
    }

    public int getValue() {
        return value;
    }
}