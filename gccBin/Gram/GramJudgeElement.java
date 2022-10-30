package gccBin.Gram;

import gccBin.Lex.LexStream;
import gccBin.Lex.Symbol;

import java.io.IOException;

public class GramJudgeElement {

    private static Symbol symbol;

    public static void reNew(){
        symbol = Gram.getInstance().getSymbol();
    }
    //*
    public static boolean isDecl () throws IOException {
        return isConstDecl() || isVarDecl();
    }
    //*
    public static boolean isConstDecl() throws IOException {
        reNew();
        return symbol == Symbol.CONSTTK;
    }
    //*
    public static boolean isVarDecl() throws IOException {
        reNew();
        Symbol peekOne = LexStream.getInstance().peek(1).getSym();
        Symbol peekTwo = LexStream.getInstance().peek(2).getSym();
        return  symbol == Symbol.INTTK &&
                peekOne == Symbol.IDENFR &&
                (peekTwo == Symbol.LBRACK ||
                        peekTwo == Symbol.ASSIGN ||
                        peekTwo == Symbol.SEMICN||
                        peekTwo == Symbol.COMMA);
    }

    //*
    // 'void' | 'int' Ident '('
    public static boolean isFuncDef() throws IOException {
        reNew();
        Symbol peekOne = LexStream.getInstance().peek(1).getSym();
        Symbol peekTwo = LexStream.getInstance().peek(2).getSym();
        return (symbol == Symbol.INTTK || symbol == Symbol.VOIDTK) &&
                peekOne == Symbol.IDENFR &&
                peekTwo  == Symbol.LPARENT;
    }
    //*
    public static boolean isStmtLVal() throws IOException {
        reNew();
        if (symbol != Symbol.IDENFR) return false;
        int index = 1;
        while (LexStream.getInstance().peek(index).getSym() != Symbol.SEMICN) {
            if (LexStream.getInstance().peek(index++).getSym() == Symbol.ASSIGN)
                return true;
        }
        return false;
    }

    // Block → '{'
    public static boolean isBlock() {
        reNew();
        return symbol == Symbol.LBRACE;
    }
    //*
    public static boolean isUnaryExpFunc() throws IOException {
        reNew();
        return symbol == Symbol.IDENFR && peekSym(1) == Symbol.LPARENT;
    }
    //*
    public static boolean isUnaryOp(){
        reNew();
        return symbol == Symbol.PLUS || symbol == Symbol.MINU ||
                symbol == Symbol.NOT;
    }

    private static Symbol peekSym(int index) throws IOException {
        return LexStream.getInstance().peek(index).getSym();
    }
}
