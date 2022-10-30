package GramTree;

import SymbolTableBin.APIGramSymTable;
import SymbolTableBin.TableSymbol;

import java.io.FileWriter;
import java.io.IOException;

/*
非终结符的共同父类
 */

public class TreeFatherNode extends TreeElement {
    private Label label;
    private TableSymbol tableSymbol;

    public TreeFatherNode() {
        this.tableSymbol = APIGramSymTable.getInstance().getNowTable();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter,Param param) throws IOException {
        for(TreeElement treeElement:super.getChildren()){
            treeElement.midCodeGen(fileWriter,param);
        }
    }

    public void ergodicMidCode(FileWriter fileWriter,Param param) throws IOException {
        for(TreeElement treeElement:super.getChildren()){
            treeElement.midCodeGen(fileWriter,param);
        }
    }

    public void addChild(TreeElement... elements) {
        for (TreeElement treeElement:elements) {
            super.getChildren().add(treeElement);
            treeElement.setFather(this);
            addChildOperate(treeElement); //子类自定义添加孩子之后的行为/错误
        }
    }

    public void addChildOperate(TreeElement treeElement) {
    }

    public void travel(FileWriter fileWriter) throws IOException {
        for (TreeElement element : super.getChildren()) {
            element.travel(fileWriter);
        }
        if (this.label != Label.Decl && this.label != Label.BlockItem
                && this.label != Label.BType) {
            fileWriter.write("<" + label + ">" + '\n');
            System.out.println("<" + label + ">");
        }
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

    public TableSymbol getTableSymbol() {
        return tableSymbol;
    }
}
