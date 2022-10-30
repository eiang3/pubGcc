package gccBin.Lex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackInputStream;
/*
词法分析 + 注释处理
 */
public class Lex {  //单例模式的词法分析器
    private static Lex instance;
    //主要变量
    private char character; //存放当前读进的字符
    private String token; //存放单词的字符串
    private Integer num; //存放当前读入的整型数值
    private Symbol symbol; //当前所识别单词的类型
    //辅助变量
    private PushbackInputStream inputStream;
    private boolean end = false; //记录是否读到文件末尾
    private int row = 1; //记录行号

    private Lex() {
    }

    public static Lex getInstance() {
        if (instance == null) {
            instance = new Lex();
        }
        return instance;
    }

    public void initialLexInput(PushbackInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void lexAnalysisStart() { //单用于词法分析作业，输出单词
        try {
            File file = new File("output.txt");
            FileWriter fileWriter = new FileWriter(file.getName());
            int ret = 0;
            do {
                ret = getSym();
                if (ret == 0) {
                    fileWriter.write(symbol + " " + token + "\n");
                    System.out.println(symbol + " " + token);
                }
            } while (ret != -2);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
       读文件，当读到末尾时，end=true;
     */
    private void getChar() throws IOException {
        int value = inputStream.read();
        if (value != -1) {
            character = (char) value;
            this.row = (character == '\n') ? this.row + 1 : this.row;
        } else {
            end = true;
            character = ' ';
        }
    }

    /*
    读符号
    -2 :end
    -1 :comments
     0 :success
     */
    public int getSym() throws IOException {
        do { //先读进一个字符
            if (!end) { //getChar前不能end
                getChar();
            }
            if (end) { //getChar后可能end
                return -2;
            }
        } while (isSpace() || isNewLine() || isTab());
        clearToken();

        if (isLetterOrLine()) {   //标识符+关键字
            while ((isLetterOrLine() || isDigit()) && notEnd()) {
                catToken();
                getChar();
            }
            if (!(isLetterOrLine() || isDigit())) retract();
            if (!isReserve()) symbol = Symbol.IDENFR;
            else symbol = ReserveWord.getInstance().getReserveSym(this.token);
        } else if (isDigit()) { //十进制数
            while (isDigit() && notEnd()) {
                catToken();
                getChar();
            }
            if (!isDigit()) retract();
            symbol = Symbol.INTCON;
            this.num = transNum();
        } else if (isQuotation()) {
            do {
                catToken();
                getChar();
            } while (!isQuotation() && notEnd());
            catToken();
            symbol = (isQuotation()) ? Symbol.STRCON : Symbol.NOTSYM;
        } else if (isLess()) {
            preJudgeModule('=', Symbol.LSS, Symbol.LEQ);
        } else if (isGreater()) {
            preJudgeModule('=', Symbol.GRE, Symbol.GEQ);
        } else if (isEqu()) {
            preJudgeModule('=', Symbol.ASSIGN, Symbol.EQL);
        } else if (isExclamation()) {
            preJudgeModule('=', Symbol.NOT, Symbol.NEQ);
        } else if (isAnd()) {
            preJudgeModule('&', Symbol.NOTSYM, Symbol.AND);
        } else if (isOr()) {
            preJudgeModule('|', Symbol.NOTSYM, Symbol.OR);
        } else if (isDivi()) {
            catToken();
            getChar();
            if (isStar()) { //开始找下一个’*/‘组合
                getChar(); //必定是注释,最少也是个'*/'
                char preChar;
                do {
                    preChar = this.character;
                    getChar();
                } while (!(preChar == '*' && this.character == '/') && notEnd());
                return -1;
            } else if (isDivi()) {
                do {
                    getChar();
                } while (!isNewLine() && notEnd());
                return -1;
            }
            retract();
            symbol = Symbol.DIV;
        } else {
            catToken();
            if (isComma()) symbol = Symbol.COMMA;
            else if (isSemi()) symbol = Symbol.SEMICN;
            else if (isPlus()) symbol = Symbol.PLUS;
            else if (isMinus()) symbol = Symbol.MINU;
            else if (isLBrack()) symbol = Symbol.LBRACK;
            else if (isRBrack()) symbol = Symbol.RBRACK;
            else if (isLBrace()) symbol = Symbol.LBRACE;
            else if (isRBrace()) symbol = Symbol.RBRACE;
            else if (isLParent()) symbol = Symbol.LPARENT;
            else if (isRParent()) symbol = Symbol.RPARENT;
            else if (isMod()) symbol = Symbol.MOD;
            else if (isStar()) symbol = Symbol.MULT;
        }
        return 0;
    }

    /*
    模板
    向下读一个字符，如果是nextChar,那么就是moreSym,否则是lessSym(最小识别)
     */
    private void preJudgeModule(char nextChar, Symbol lessSym, Symbol moreSym) throws IOException {
        catToken();
        getChar();
        if (character == nextChar) {
            catToken();
            symbol = moreSym;
        } else {
            retract();
            symbol = lessSym;
        }
    }

    private boolean isSpace() {
        return (character == ' ');
    }

    private boolean isNewLine() {
        return (character == '\r' || character == '\n');
    }

    private boolean isTab() {
        return (character == '\t');
    }

    private boolean isLetter() {
        return (character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z');
    }

    private boolean isLetterOrLine() {
        return isLetter() || (character == '_');
    }

    private boolean isDigit() {
        return (character >= '0' && character <= '9');
    }


    private boolean isComma() {
        return (character == ',');
    }

    private boolean isSemi() {
        return (character == ';');
    }

    private boolean isPlus() {
        return (character == '+');
    }

    private boolean isEqu() {
        return (character == '=');
    }

    private boolean isMinus() {
        return (character == '-');
    }

    private boolean isDivi() {
        return (character == '/');
    }

    private boolean isStar() {
        return (character == '*');
    }

    private boolean isQuotation() {
        return (character == '"');
    }

    private boolean isExclamation() {
        return (character == '!');
    }

    private boolean isMod() {
        return (character == '%');
    }

    private boolean isLess() {
        return (character == '<');
    }

    private boolean isGreater() {
        return (character == '>');
    }

    private boolean isAnd() {
        return (character == '&');
    }

    private boolean isOr() {
        return (character == '|');
    }

    private boolean isLParent() {
        return (character == '(');
    }

    private boolean isRParent() {
        return (character == ')');
    }

    private boolean isLBrack() {
        return (character == '[');
    }

    private boolean isRBrack() {
        return (character == ']');
    }

    private boolean isLBrace() {
        return (character == '{');
    }

    private boolean isRBrace() {
        return (character == '}');
    }

    private void clearToken() {
        token = "";
    }

    /*
    将character和token连接
     */
    private void catToken() {
        token = token.concat(String.valueOf(character));
    }

    /*
    读入字符流回退一个字符
     */
    private void retract() throws IOException {
        this.row = (character == '\n') ? this.row - 1 : this.row;
        inputStream.unread(character);
    }

    private boolean notEnd() {
        return (!end);
    }

    private boolean isReserve() {
        return ReserveWord.getInstance().isReserve(token);
    }

    private Integer transNum() { //读入数字，翻译数字
        return Integer.parseInt(token);
    }

    public Symbol getSymbol() {
        return this.symbol;
    }

    public String getToken() {
        return token;
    }

    public int getRow() {
        return row;
    }

    public void closeInputFile() throws IOException {
        this.inputStream.close();
    }
}
