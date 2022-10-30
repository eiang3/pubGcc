package GramTree.Element.stmt;

import GramTree.Element.Block;
import GramTree.Param;
import GramTree.TreeElement;

import java.io.FileWriter;
import java.io.IOException;

/*
Stmt →
Block
 */
public class BlockStmt extends Stmt{
    private Block block;

    public BlockStmt(){
        super();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        block.midCodeGen(fileWriter,param);
    }
    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof Block){
            this.block = (Block) treeElement;
        }
    }
}
