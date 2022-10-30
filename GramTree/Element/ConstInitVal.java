package GramTree.Element;

import GramTree.Label;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
/*
常量初值    ConstInitVal → ConstExp
    | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'

0维 ConstExp
1维 { , , , }
2维 {{,},{,},{,}}
 */
public class ConstInitVal extends TreeFatherNode {
    private int value; //0维
    private final ArrayList<ArrayList<Integer>> array; //一、二维

    public ConstInitVal() {
        super();
        super.setLabel(Label.ConstInitVal);
        this.array = new ArrayList<>();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        int dimension = param.getDimension(); //得知这个constInitVal的维数
        for (TreeElement treeElement : super.getChildren()) {
            param.setDimension(Math.max((dimension - 1), 0));
            treeElement.midCodeGen(fileWriter, param);
        }
        if (dimension == 0) {
            ConstExp constExp = (ConstExp) super.getChildren().get(0);
            this.value = constExp.getValue();
        } else if (dimension == 1) {
            ArrayList<Integer> a = new ArrayList<>();
            for (TreeElement treeElement : super.getChildren()) {
                if (treeElement instanceof ConstInitVal) {
                    ConstInitVal constInitVal = (ConstInitVal) treeElement;
                    a.add(constInitVal.getValue());
                }
            }
            this.array.add(a);
        } else {
            for (TreeElement treeElement : super.getChildren()) {
                if (treeElement instanceof ConstInitVal) {
                    ConstInitVal constInitVal = (ConstInitVal) treeElement;
                    this.array.add(constInitVal.getOneDimArray());
                }
            }
        }
    }

    public ArrayList<ArrayList<Integer>> getArray() {
        return array;
    }

    public ArrayList<Integer> getOneDimArray() {
        return this.array.get(0);
    }

    public int getValue() {
        return value;
    }
}