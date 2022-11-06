package GramTree.Element;

import GramTree.*;
import SymbolTableBin.TypeTable;
import SymbolTableBin.Value;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
函数形参    FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]  //   b k
 */
public class FuncFParam extends TreeFatherNode {
    private Word ident;
    private TypeTable type;
    private BType bType;
    private String name;
    private int falseRow;
    private ConstExp constExpTwo;
    private int dimension;

    private int index; //函数的第x个参数。
    private Value towDim; //如果由第二维的话

    public FuncFParam() {
        super();
        super.setLabel(Label.FuncFParam);
        this.dimension = 0;
        index = 0;
        towDim = new Value();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter, param);
        IRGenerate.getInstance().funcFParam(type, name, dimension, super.getTableSymbol(), falseRow);
        if (constExpTwo != null) {
            towDim.setNum(constExpTwo.getValue());
        }
    }

    @Override
    public void addChildOperate(TreeElement element) {
        if (element instanceof Word) {
            Word word = (Word) element;
            if (word.getSym() == Symbol.IDENFR) {
                this.name = word.getToken();
                this.falseRow = word.getFalseRow();
            } else if (word.getSym() == Symbol.LBRACK) {
                this.dimension++;
            }
        } else if (element instanceof BType) {
            this.bType = (BType) element;
            this.type = ((BType) element).getType();
        } else if (element instanceof ConstExp) {
            this.constExpTwo = (ConstExp) element;
        }
    }

    public TypeTable getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getDimension() {
        return dimension;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int getFalseRow() {
        return falseRow;
    }

    public Value getTowDim() {
        return towDim;
    }
}