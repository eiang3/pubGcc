package GramTree.Element;


import GramTree.Label;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;

import java.io.FileWriter;
import java.io.IOException;
/*
CompUnit → {Decl} {FuncDef} MainFuncDef
 */
public class CompUnit extends TreeFatherNode {
    //for midCode
    private boolean j;

    public CompUnit(){
        super();
        j = false;
        super.setLabel(Label.CompUnit);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        for(TreeElement treeElement:super.getChildren()){
            if(treeElement instanceof FuncDef && !j){
                fileWriter.write("\n&call main\n");
                j = true;
            }
            treeElement.midCodeGen(fileWriter,param);
        }
    }
}
