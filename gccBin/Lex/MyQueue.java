package gccBin.Lex;
import GramTree.Word;

import java.util.LinkedList;

public class MyQueue {
    public LinkedList<Word> linkedList = new LinkedList<>();

    public MyQueue(){ }

    //加入元素，默认成功
    public void offer(Word word){
        linkedList.offer(word);
    }

    public int size(){
        return linkedList.size();
    }

    //超前偷看第index个元素的Symbol
    public Word peek(int index){
        if(linkedList.size() < index)
            return null;
        else {
            return linkedList.get(index -1 );
        }
    }

    //删除并返回这个队列队头的元素
    public Word poll(){
        if(linkedList.size() == 0) return null;
        return linkedList.poll();
    }
}
