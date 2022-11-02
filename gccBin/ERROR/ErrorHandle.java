package gccBin.ERROR;

import GramTree.Element.*;
import GramTree.Element.UnaryExp.FuncUnaryExp;
import GramTree.Element.stmt.PrintfStmt;
import GramTree.Element.stmt.ReturnStmt;
import GramTree.Word;
import SymbolTableBin.*;
import SymbolTableBin.Element.ElementTable;
import gccBin.Gram.Gram;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ErrorHandle {
    private static ErrorHandle instance;
    private FileWriter fileWriter;
    private static final boolean closeError = false;

    private ErrorHandle() {
    }


    public static ErrorHandle getInstance() {
        if (instance == null) {
            instance = new ErrorHandle();
        }
        return instance;
    }

    public void open() throws IOException {
        File file = new File("error.txt");
        fileWriter = new FileWriter(file.getName());
    }

    public void close() throws IOException {
        fileWriter.close();
    }

    private void printError(char errorType) throws IOException {
        String str = Gram.getInstance().getRow() + " " + errorType;
        System.out.println(str);
        fileWriter.write(str + '\n');
    }

    private void printError(char errorType, int row) throws IOException {
        String str = row + " " + errorType;
        System.out.println(str);
        fileWriter.write(str + '\n');
    }


    // 非法符号 a <FormatString> 错误
    public boolean AIllegalSym(PrintfStmt printfStmt) throws IOException {
        if (closeError) return false;

        Word formatString = printfStmt.getFormatString();
        if (formatString != null) {
            String str = formatString.getToken();
            int len = str.length() - 1;
            for (int i = 1; i < len; i++) {
                char c = str.charAt(i);
                if (!(c == '%' && (i + 1) < len && str.charAt(i + 1) == 'd') &&
                        !(c == '\\' && (i + 1) < len && str.charAt(i + 1) == 'n') &&
                        !(c == 32 || c == 33 || c >= 40 && c <= 126 && c != 92)) {
                    printError('a', formatString.getRow());
                    return true;
                }
            }
        }
        return false;
    }

    //名字重定义 b
    public void BNameRedefinition(String name) throws IOException {
        if (closeError) return;
        if (APIErrorSymTable.getInstance().isNameReDefined(name)) {
            printError('b');
        }
    }

    public void BNameRedefinition() throws IOException {
        printError('b');
    }

    //未定义的名字 c
    public void CUndefinedName(String name) throws IOException {
        if (closeError) return;
        if (APIErrorSymTable.getInstance().isNameUnDefined(name)) {
            printError('c');
        }
    }

    //函数参数个数不匹配 d + 函数参数类型不匹配 e
    public void DEFuncParamNotMatch(FuncUnaryExp unaryExp) throws IOException {
        if (closeError) return;
        Word func = unaryExp.getFuncWord(); //基本的
        ArrayList<Exp> RParams = unaryExp.getFuncRParams();

        if (func != null && APIErrorSymTable.getInstance().
                getFuncDef(func.getToken()) != null) {

            ArrayList<FuncFParam> funcFParams =
                    APIErrorSymTable.getInstance().getFuncDefFParams(func.getToken()); //形参列表

            if (funcFParams.size() != RParams.size()) {
                printError('d', func.getRow());
            } else {
                for (int i = 0; i < RParams.size(); i++) {
                    if (funcFParams.get(i).getDimension() != RParams.get(i).getDimension()) {
                        printError('e', func.getRow());
                    }
                }
            }
        }
    }

    //无返回值的函数存在不匹配的return语句 f
    private FuncDef funcDef;

    public void entryFunc(FuncDef funcDef) {
        this.funcDef = funcDef;
    }

    public void leftFuncDef() {
        this.funcDef = null;
    }

    public void FVoidFuncWithReturn(ReturnStmt returnStmt) throws IOException {
        if (closeError) return;
        Word returnWord = returnStmt.getReturnWord();
        if (funcDef != null && returnWord != null) {
            if (funcDef.getReturnType() == TypeTable.VOID && returnStmt.hasReturnValue()) {
                printError('f', returnWord.getRow());
            }
        }
    }

    //有返回值的函数缺少return语句 g
    //空语句块
    //最后一句非返回值
    //最后一句是返回值但返回空值
    //那就判断正确的情况吧
    public boolean GIntFuncWithoutReturn(TypeTable myType, Block block) throws IOException {
        if (closeError) return false;
        if (myType == TypeTable.INT) {
            return intFuncWithoutReturn(block);
        }
        return false;
    }

    //有返回值的函数缺少return语句 g for mainDef
    private boolean intFuncWithoutReturn(Block block) throws IOException {
        BlockItem lastBlockItem = block.getLastBlockItem();
        Word rbrace = block.getRBrace();
        if (lastBlockItem != null && lastBlockItem.getStmt() instanceof ReturnStmt
                && rbrace != null) {
            ReturnStmt returnStmt = ((ReturnStmt) lastBlockItem.getStmt());
            if (returnStmt.hasReturnValue()) {
                return false;
            }
        }
        if (rbrace != null) printError('g', rbrace.getRow());
        return true;
    }

    //不能改变常量的值 h
    public void HChangeConst(LVal lVal) throws IOException {
        if (closeError) return;
        Word ident = lVal.getIdent();
        if (ident != null) {
            ElementTable tableElement = APIErrorSymTable.getInstance().
                    getElement(ident.getToken());
            if (tableElement != null) {
                if (tableElement.getDecl() == TypeTable.CONST) {
                    printError('h', ident.getRow());
                }
            }
        }
    }

    //缺少分号 i
    public void ILackSemicn(int row) throws IOException {
        if (closeError) return;
        printError('i', row);
    }

    //缺少右小括号’)’ j
    public void JLackRParent(int row) throws IOException {
        if (closeError) return;
        printError('j', row);
    }

    //缺少右中括号’]’ k
    public void KLackRbrack(int row) throws IOException {
        if (closeError) return;
        printError('k', row);
    }

    //printf中格式字符与表达式个数不匹配
    public void LPrintfNotMatch(PrintfStmt printfStmt) throws IOException {
        if (closeError) return;
        Word formatString = printfStmt.getFormatString();
        int expCount = printfStmt.getExpSize();
        Word printf = printfStmt.getPrintf();
        if (formatString != null && printf != null) {
            int formatCount = formatString.getToken().split("%").length - 1;
            if (expCount != formatCount) {
                printError('l', printf.getRow());
            }
        }
    }


    //在非循环块中使用break和continue语句 m
    public int inLoop = 0;

    public void initialLoop() {
        inLoop++;
    }

    public void leaveLoop() {
        inLoop--;
    }

    public boolean MBreakAndContinue(int row) throws IOException {
        if (closeError) return false;
        if (inLoop <= 0) {
            printError('m', row);
            return true;
        }
        return false;
    }
}
