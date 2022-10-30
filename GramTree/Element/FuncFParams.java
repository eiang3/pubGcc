package GramTree.Element;

import GramTree.Label;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
/*
函数形参表   FuncFParams → FuncFParam { ',' FuncFParam }
 */
public class FuncFParams extends TreeFatherNode {
    private final ArrayList<FuncFParam> params ;

    public FuncFParams(){
        super();
        super.setLabel(Label.FuncFParams);
        this.params = new ArrayList<>();
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof FuncFParam){
            this.params.add((FuncFParam) treeElement);
        }
    }

    public ArrayList<FuncFParam> getParams() {
        return params;
    }
}