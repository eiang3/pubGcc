
package GramTree.Element;

import GramTree.*;
import gccBin.MidCode.MidCode;

import java.io.FileWriter;
import java.io.IOException;
/*
主函数定义   MainFuncDef → 'int' 'main' '(' ')' Block // g j
 */
public class MainFuncDef extends TreeFatherNode {

    public MainFuncDef() {
        super();
        super.setLabel(Label.MainFuncDef);
    }


    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        fileWriter.write("\nfunc_main: \n");
        for (TreeElement treeElement : super.getChildren()) {
            treeElement.midCodeGen(fileWriter,param);
        }
    }
}