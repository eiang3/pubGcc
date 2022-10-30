package GramTree.Element;

import GramTree.*;
import SymbolTableBin.TypeTable;
import SymbolTableBin.Value;
import gccBin.Lex.Symbol;
import gccBin.MidCode.MidCode;

import java.io.FileWriter;
import java.io.IOException;

/*
变量定义    VarDef → Ident { '[' ConstExp ']' } // b
    | Ident { '[' ConstExp ']' } '=' InitVal // k
 */

public class VarDef extends TreeFatherNode {
    private TypeTable type;
    private String name;
    private int dimension;
    // for midCode
    private ConstExp constExpOne;
    private ConstExp constExpTwo;
    private InitVal initVal;

    private final Value oneDim;
    private final Value twoDim;

    public VarDef() {
        super();
        super.setLabel(Label.VarDef);
        this.dimension = 0;
        //
        this.oneDim = new Value();
        this.twoDim = new Value();
        //
        TreeFatherNode father = super.getFather();
        if (father instanceof VarDecl) {
            this.type = ((VarDecl) father).getType();
        }
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        Param p = new Param(param);
        p.setInitial(InheritProp.Initial);
        super.ergodicMidCode(fileWriter, p);
        int len;
        if (this.constExpOne != null && this.constExpTwo != null) {
            len = constExpOne.getValue() * constExpTwo.getValue();
            this.oneDim.setNum(constExpOne.getValue());
            this.twoDim.setNum(constExpTwo.getValue());
        } else if (this.constExpOne != null && this.constExpTwo == null) {
            len = constExpOne.getValue();
            this.oneDim.setNum(constExpOne.getValue());
        } else {
            len = -1;
        }
        MidCode.getInstance().varDef(name,len,initVal);
    }


    public Value getOneDim() {
        return oneDim;
    }

    public Value getTwoDim() {
        return twoDim;
    }

    @Override
    public void addChildOperate(TreeElement element) {
        if (element instanceof Word) {
            Word word = (Word) element;
            if (word.getSym() == Symbol.IDENFR) {
                this.name = word.getToken();
            } else if (word.getSym() == Symbol.LBRACK) {
                this.dimension++;
            }
        } else if (element instanceof ConstExp) {
            if (this.constExpOne == null) {
                this.constExpOne = (ConstExp) element;
            } else {
                this.constExpTwo = (ConstExp) element;
            }
        } else if (element instanceof InitVal) {
            this.initVal = ((InitVal) element);
        }
    }

    public String getName() {
        return name;
    }

    public int getDimension() {
        return dimension;
    }

    public TypeTable getType() {
        return type;
    }

}