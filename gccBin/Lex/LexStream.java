package gccBin.Lex;

import GramTree.Word;

import java.io.IOException;

public class LexStream {
    private static LexStream instance;

    private Symbol sym;
    private String token;
    private int row;
    private Word word;
    private final MyQueue wordTb = new MyQueue();

    private LexStream() {
    }

    public static LexStream getInstance() {
        if (instance == null) {
            instance = new LexStream();
        }
        return instance;
    }

    //错误待处理：当词法分析已经读完，还要提前分析
    /*
    如果队列非空，就从队列中取，
    否则就再从词法分析部分取
    保证执行完
    sym是接下来的符号;
     */
    public void getNextWord() throws IOException {
        if (wordTb.peek(1) != null) {
            this.word = wordTb.poll();
            this.sym = word.getSym();
            this.token = word.getToken();
            this.row = word.getRow();
        } else {
            nextSymLex();
            this.sym = Lex.getInstance().getSymbol();
            this.token = Lex.getInstance().getToken();
            this.row = Lex.getInstance().getRow();
            this.word = new Word(this.sym,this.token,this.row);
        }
    }

    /*
    调用词法分析的getSym程序,跳过注释(ret=-1),
    如果读入成功ret=0
    或者读入失败
     */
    public void nextSymLex() throws IOException {
        int ret = -1;
        while (ret != 0) {
            ret = Lex.getInstance().getSym();
            if (ret == -2) {
                Lex.getInstance().close();
                return;
            }
        }
    }

    /*
    为了分析程序，需要预先偷窥。 陈：会偷窥过火吗？
     */
    public Word peek(int index) throws IOException {
        if (wordTb.peek(index) == null) {
            int times = index - wordTb.size();
            for (int i = 1; i <= times; i++) {
                nextSymLex();
                wordTb.offer(new Word(Lex.getInstance().getSymbol(),
                        Lex.getInstance().getToken(),
                        Lex.getInstance().getRow()));
            }
        }
        return wordTb.peek(index);
    }

    public Symbol getSym() {
        return sym;
    }

    public String getToken() {
        return token;
    }

    public int getRow() {
        return row;
    }

    public Word getWord() {
        return word;
    }
}
