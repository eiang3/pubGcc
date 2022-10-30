package GramTree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
抽象类，所有的语法树元素父类 ok
 */

public class TreeElement {
    private TreeFatherNode father;
    private ArrayList<TreeElement> children;

    public TreeElement(){
        this.children = new ArrayList<>();
    }

    public void travel(FileWriter fileWriter) throws IOException{
    }

    public void midCodeGen(FileWriter fileWriter,Param param) throws IOException{
    }

    public ArrayList<TreeElement> getChildren() {
        return children;
    }

    public int getChildrenSize(){
        return this.children.size();
    }

    public TreeFatherNode getFather() {
        return father;
    }

    public void setFather(TreeFatherNode father) {
        this.father = father;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(TreeElement treeElement : children){
            sb.append(treeElement).append(' ');
        }
        return sb.toString();
    }
}
