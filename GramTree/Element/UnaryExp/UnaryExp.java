package GramTree.Element.UnaryExp;

import GramTree.*;
/*
一元表达式   UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // c d e j
        | UnaryOp UnaryExp
 */
public class UnaryExp extends TreeFatherNode {
    private int value; //ConstExp
    private String midCode; //Exp

    private int coe;
    private int negative;

    private int dimension;
    public UnaryExp() {
        super();
        super.setLabel(Label.UnaryExp);
        this.coe = 1;
        this.negative = 1;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getMidCode() {
        return midCode;
    }

    public void setMidCode(String midCode) {
        this.midCode = midCode;
    }

    public int getCoe() {
        return coe;
    }

    public void setCoe(int coe) {
        this.coe = coe;
    }

    public int getNegative() {
        return negative;
    }

    public void setNegative(int negative) {
        this.negative = negative;
    }
}