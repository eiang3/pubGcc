package GramTree.Element;

import GramTree.*;

import java.io.FileWriter;
import java.io.IOException;

/*
 PrimaryExp → '(' Exp ')' | LVal | Number
 */
public class PrimaryExp extends TreeFatherNode {
    private int dimension;
    //for MidCodeGen

    private int value; //ConstExp
    private String midCode; //Exp
    //
    private Exp exp;
    private LVal lVal;
    private MyNumber number;

    public PrimaryExp() {
        super();
        super.setLabel(Label.PrimaryExp);
        this.dimension = 0;
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter,param);
        if (param.getExpKind() == InheritProp.ConstExp) {
            if (this.number != null) {
                this.value = this.number.getValue();
            } else if (this.lVal != null) {
                this.value = this.lVal.getValue();
            } else if (this.exp != null) {
                this.value = this.exp.getValue();
            }
        } else {
            if (this.number != null) {
                this.midCode = this.number.getMidCode();
            } else if (this.lVal != null) {
                this.midCode = this.lVal.getMidCode();
            } else if (this.exp != null) {
                this.midCode = this.exp.getMidCode();
            }
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Exp) {
            this.dimension = ((Exp) treeElement).getDimension();
            exp = ((Exp) treeElement);
        } else if (treeElement instanceof LVal) {
            this.lVal = (LVal) treeElement;
            this.dimension = ((LVal) treeElement).getDimension();
        } else if (treeElement instanceof MyNumber) {
            this.number = (MyNumber) treeElement;
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