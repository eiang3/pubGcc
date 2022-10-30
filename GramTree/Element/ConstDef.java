package GramTree.Element;

import GramTree.*;
import SymbolTableBin.TypeTable;
import SymbolTableBin.Value;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.MidCode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
常数定义    ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
 */
public class ConstDef extends TreeFatherNode {
    private TypeTable type;     //int
    private String name;        //常量名addChildOp确定
    private int dimension;      //维数
    //for midCode
    private final Value value;     //0维的时候符号表里的值/为了回填
    private final ArrayList<ArrayList<Integer>> array;  //多维时保存的值
    private ConstExp constExpOne;
    private ConstExp constExpTwo;
    private ConstInitVal constInitVal;

    public ConstDef() {
        super();
        super.setLabel(Label.ConstDef);

        TreeFatherNode father = super.getFather();
        if (father instanceof ConstDecl) {
            this.type = ((ConstDecl) father).getType();
        }

        this.dimension = 0;
        this.value = new Value();
        this.array = new ArrayList<>();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        Param p = new Param(param);
        p.setDimension(this.dimension);
        for (TreeElement treeElement : super.getChildren()) {
            treeElement.midCodeGen(fileWriter, p);
        }
        if (dimension == 0) { //在java里保存值
            this.value.setNum(constInitVal.getValue());
        } else if (dimension == 1) {
            this.array.add(constInitVal.getArray().get(0));
            MidCode.getInstance().constDefArray(name,constExpOne.getValue(),array);
        } else if (dimension == 2) {
            this.array.addAll(constInitVal.getArray());
            MidCode.getInstance().constDefArray(name,
                    constExpOne.getValue()*constExpTwo.getValue(),
                    array);
        }
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
        } else if (element instanceof ConstInitVal) {
            this.constInitVal = (ConstInitVal) element;
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

    public Value getValue() {
        return value;
    }

    public ArrayList<ArrayList<Integer>> getArray() {
        return array;
    }

}