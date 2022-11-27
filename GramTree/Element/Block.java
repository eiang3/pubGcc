package GramTree.Element;

import GramTree.*;
import gccBin.Lex.Symbol;
import gccBin.MidCode.AoriginalProcess.IRGenerate;

import java.io.FileWriter;
import java.io.IOException;

/*
语句块     Block → '{' { BlockItem } '}'
 */
public class  Block extends TreeFatherNode {
    private BlockItem lastBlockItem;
    private Word RBrace;

    public  Block(){
        super();
        super.setLabel(Label. Block);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        IRGenerate.getInstance().inBlock();
        for (TreeElement treeElement : super.getChildren()) {
            treeElement.midCodeGen(fileWriter,param);
        }
        IRGenerate.getInstance().leaveBlock();
    }

    @Override
    public void addChildOperate(TreeElement treeElement){
        if(treeElement instanceof BlockItem){
            this.lastBlockItem = (BlockItem) treeElement;
        } else if(treeElement instanceof Word
                && ((Word)treeElement).getSym() == Symbol.RBRACE){
            this.RBrace = ((Word) treeElement);
        }
    }

    public BlockItem getLastBlockItem() {
        return lastBlockItem;
    }

    public Word getRBrace() {
        return RBrace;
    }
}