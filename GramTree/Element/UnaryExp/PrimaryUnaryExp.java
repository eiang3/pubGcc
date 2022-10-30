package GramTree.Element.UnaryExp;

import GramTree.Element.LVal;
import GramTree.Element.PrimaryExp;
import GramTree.InheritProp;
import GramTree.Param;
import GramTree.TreeElement;

import java.io.FileWriter;
import java.io.IOException;

/*
一元表达式   UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // c d e j
        | UnaryOp UnaryExp
 */
public class PrimaryUnaryExp extends UnaryExp {
    private PrimaryExp primaryExp;

    public PrimaryUnaryExp() {
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        this.primaryExp.midCodeGen(fileWriter, param);
        if (param.getExpKind() == InheritProp.ConstExp) {
            super.setValue(this.primaryExp.getValue());
        } else  {
            super.setMidCode(this.primaryExp.getMidCode());
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof PrimaryExp) {
            this.primaryExp = (PrimaryExp) treeElement;
            super.setDimension(this.primaryExp.getDimension());
        }
    }

}
