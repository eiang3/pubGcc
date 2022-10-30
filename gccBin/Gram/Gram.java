package gccBin.Gram;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import GramTree.Element.*;
import GramTree.Element.UnaryExp.FuncUnaryExp;
import GramTree.Element.UnaryExp.PrimaryUnaryExp;
import GramTree.Element.UnaryExp.UnaryExp;
import GramTree.Element.UnaryExp.UnaryOpUnaryExp;
import GramTree.Element.cond.*;
import GramTree.Element.stmt.*;
import GramTree.TreeFatherNode;
import GramTree.Word;
import SymbolTableBin.APIGramSymTable;
import SymbolTableBin.APIMidCodeSymTable;
import SymbolTableBin.TypeTable;
import gccBin.ERROR.ErrorHandle;
import gccBin.Lex.LexStream;
import gccBin.Lex.Symbol;
import gccBin.MidCode.MidCode;

/*
递归下降词法分析
&&
错误处理
 */
public class Gram {
    private static Gram instance;

    private Word word;
    private Symbol symbol;
    private String token;
    private int row;

    private int beforeRow; //ForErrorHandle: previous row
    private FileWriter fileWriter;

    public static Gram getInstance() {
        if (instance == null)
            instance = new Gram();
        return instance;
    }

    public void initialGramOutFile() throws IOException {
        File file = new File("output.txt");
        fileWriter = new FileWriter(file.getName());
    }

    public void closeGramOutFile() throws IOException {
        fileWriter.close();
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public int getRow() {
        return row;
    }

    public void getSym() throws IOException {
        LexStream.getInstance().getNextWord();
        this.symbol = LexStream.getInstance().getSym();
        this.token = LexStream.getInstance().getToken();
        this.beforeRow = this.row;
        this.row = LexStream.getInstance().getRow();
        this.word = LexStream.getInstance().getWord();
    }


    public void gramStart() throws IOException {
        getSym();
        CompUnit compUnit = compUnit();
        MidCode.getInstance().setRoot(compUnit);
        //compUnit.travel(fileWriter);
    }

    //递归下降
    private CompUnit compUnit() throws IOException {
        APIGramSymTable.getInstance().buildTable();

        CompUnit compUnit = new CompUnit();
        while (GramJudgeElement.isDecl()) {
            compUnit.addChild(decl());
        }
        while (GramJudgeElement.isFuncDef()) {
            compUnit.addChild(funcDef());
        }
        compUnit.addChild(mainFuncDef());

        APIGramSymTable.getInstance().finishTable();
        return compUnit;
    }

    private Decl decl() throws IOException {
        Decl decl = new Decl();

        if (GramJudgeElement.isConstDecl()) {
            decl.addChild(constDecl());
        } else {
            decl.addChild(varDecl());
        }
        return decl;
    }

    private ConstDecl constDecl() throws IOException {
        ConstDecl constDecl = new ConstDecl();

        assertSymGetSym(constDecl, "constDecl e1", Symbol.CONSTTK);
        constDecl.addChild(bType());
        ConstDef constDef = constDef();
        constDecl.addChild(constDef);

        APIGramSymTable.getInstance().addConstDef(constDef);
        while (hopeSymGetSym(constDecl, Symbol.COMMA)) {
            constDef = constDef();
            constDecl.addChild(constDef);
            APIGramSymTable.getInstance().addConstDef(constDef);
        }
        assertSemicnGetSym(constDecl);
        return constDecl;
    }

    private BType bType() throws IOException {
        BType btype = new BType();

        assertSymGetSym(btype, "constDecl e2", Symbol.INTTK);
        return btype;
    }

    private ConstDef constDef() throws IOException {
        ConstDef constDef = new ConstDef();

        assertSymGetSym(constDef, "constDef e1 ", Symbol.IDENFR);
        while (hopeSymGetSym(constDef, Symbol.LBRACK)) {
            constDef.addChild(constExp());
            assertRBrackAndGetSym(constDef); // ]
        }
        assertSymGetSym(constDef, "constDef e2", Symbol.ASSIGN);
        constDef.addChild(constInitVal());
        return constDef;
    }

    private ConstInitVal constInitVal() throws IOException {
        ConstInitVal constInitVal = new ConstInitVal();

        if (hopeSymGetSym(constInitVal, Symbol.LBRACE)) {
            if (symbol != Symbol.RBRACE) {
                constInitVal.addChild(constInitVal());
                while (hopeSymGetSym(constInitVal, Symbol.COMMA)) {
                    constInitVal.addChild(constInitVal());
                }
            }
            assertSymGetSym(constInitVal, "ConstInitVal e1", Symbol.RBRACE);
        } else {
            constInitVal.addChild(constExp());
        }
        return constInitVal;
    }

    private VarDecl varDecl() throws IOException {
        VarDecl varDecl = new VarDecl();
        varDecl.addChild(bType());
        VarDef varDef = varDef();
        varDecl.addChild(varDef);

        APIGramSymTable.getInstance().addVarDef(varDef);
        while (hopeSymGetSym(varDecl, Symbol.COMMA)) {
            varDef = varDef();
            varDecl.addChild(varDef);
            APIGramSymTable.getInstance().addVarDef(varDef);
        }
        assertSemicnGetSym(varDecl);
        return varDecl;
    }

    private VarDef varDef() throws IOException {
        VarDef varDef = new VarDef();
        assertSymGetSym(varDef, "varDef e1", Symbol.IDENFR);

        while (hopeSymGetSym(varDef, Symbol.LBRACK)) {
            varDef.addChild(constExp());
            assertRBrackAndGetSym(varDef); // ]
        }
        if (hopeSymGetSym(varDef, Symbol.ASSIGN)) {
            varDef.addChild(initVal());
        }
        return varDef;
    }

    private InitVal initVal() throws IOException {
        InitVal initVal = new InitVal();

        if (hopeSymGetSym(initVal, Symbol.LBRACE)) {
            if (symbol != Symbol.RBRACE) {
                initVal.addChild(initVal());
                while (hopeSymGetSym(initVal, Symbol.COMMA)) {
                    initVal.addChild(initVal());
                }
            }
            assertSymGetSym(initVal, "InitVal e1", Symbol.RBRACE);
        } else {
            initVal.addChild(exp());
        }
        return initVal;
    }

    private FuncDef funcDef() throws IOException {
        FuncDef funcDef = new FuncDef();
        funcDef.addChild(funcType());
        String name = this.token;
        assertSymGetSym(funcDef, "FuncDef e1", Symbol.IDENFR);
        assertSymGetSym(funcDef, "FuncDef e2", Symbol.LPARENT);
        //为错误处理新添的接口，如果改成符号集判断会不会好一点？ 陈

        APIGramSymTable.getInstance().buildTable();
        APIMidCodeSymTable.getInstance().addFunTable(name,
                APIGramSymTable.getInstance().getNowTable());

        ErrorHandle.getInstance().entryFunc(funcDef); //void error
        if (symbol != Symbol.RPARENT && symbol != Symbol.LBRACE) {
            funcDef.addChild(funcFParams());
        }
        assertRParentGetSym(funcDef);
        APIGramSymTable.getInstance().addFuncDef(funcDef);
        Block block = block();
        funcDef.addChild(block);

        ErrorHandle.getInstance().GIntFuncWithoutReturn(funcDef.getReturnType(), block);
        ErrorHandle.getInstance().leftFuncDef(); //void error
        APIGramSymTable.getInstance().finishTable();
        return funcDef;
    }

    private MainFuncDef mainFuncDef() throws IOException {

        MainFuncDef mainFuncDef = new MainFuncDef();
        APIGramSymTable.getInstance().addMainFuncDef();

        assertSymGetSym(mainFuncDef, "mainFuncDef e1", Symbol.INTTK);
        assertSymGetSym(mainFuncDef, "mainFuncDef e2", Symbol.MAINTK);
        assertSymGetSym(mainFuncDef, "mainFuncDef e3", Symbol.LPARENT);
        assertRParentGetSym(mainFuncDef);

        Block block = block();
        mainFuncDef.addChild(block);

        ErrorHandle.getInstance().GIntFuncWithoutReturn(TypeTable.INT, block);
        return mainFuncDef;
    }

    private FuncType funcType() throws IOException {
        FuncType funcType = new FuncType();
        assertSymGetSym(funcType, "FuncType e1", Symbol.VOIDTK, Symbol.INTTK);
        return funcType;
    }

    private FuncFParams funcFParams() throws IOException {
        FuncFParams funcFParams = new FuncFParams();
        FuncFParam funcFParam = funcFParam();
        funcFParams.addChild(funcFParam);

        APIGramSymTable.getInstance().addFuncFParam(funcFParam);
        while (hopeSymGetSym(funcFParams, Symbol.COMMA)) {
            funcFParam = funcFParam();
            funcFParams.addChild(funcFParam);
            APIGramSymTable.getInstance().addFuncFParam(funcFParam);
        }

        return funcFParams;
    }

    private FuncFParam funcFParam() throws IOException {
        FuncFParam funcFParam = new FuncFParam();

        funcFParam.addChild(bType());
        assertSymGetSym(funcFParam, "FuncFParam e2", Symbol.IDENFR);

        if (hopeSymGetSym(funcFParam, Symbol.LBRACK)) {
            assertRBrackAndGetSym(funcFParam);
            while (hopeSymGetSym(funcFParam, Symbol.LBRACK)) {
                funcFParam.addChild(constExp());
                assertRBrackAndGetSym(funcFParam);
            }
        }

        return funcFParam;
    }

    private Block block() throws IOException {
        APIGramSymTable.getInstance().buildTable();

        Block block = new Block();
        assertSymGetSym(block, "Block e1", Symbol.LBRACE);
        while (symbol != Symbol.RBRACE) {
            block.addChild(blockItem());
        }
        assertSymGetSym(block, "Block e2", Symbol.RBRACE);

        APIGramSymTable.getInstance().finishTable();
        return block;
    }

    private BlockItem blockItem() throws IOException {
        BlockItem blockItem = new BlockItem();

        if (GramJudgeElement.isDecl()) {
            blockItem.addChild(decl());
        } else blockItem.addChild(stmt());
        return blockItem;
    }

    private Stmt stmt() throws IOException {
        if (symbol == Symbol.IFTK) {
            return stmtIf();
        } else if (symbol == Symbol.WHILETK) {
            return stmtWhile();
        } else if (symbolEql(Symbol.BREAKTK)) {
            BreakStmt breakStmt = new BreakStmt();
            hopeSymGetSym(breakStmt, Symbol.BREAKTK);
            assertSemicnGetSym(breakStmt);
            ErrorHandle.getInstance().MBreakAndContinue(this.beforeRow);
            return breakStmt;
        } else if (symbolEql(Symbol.CONTINUETK)) {
            ContinueStmt continueStmt = new ContinueStmt();
            hopeSymGetSym(continueStmt, Symbol.CONTINUETK);
            assertSemicnGetSym(continueStmt);
            ErrorHandle.getInstance().MBreakAndContinue(this.beforeRow);
            return continueStmt;
        } else if (symbol == Symbol.RETURNTK) {
            return stmtReturn();
        } else if (symbol == Symbol.PRINTFTK) {
            return stmtPrintf();
        } else if (GramJudgeElement.isStmtLVal()) {
            AssignStmt assignStmt = new AssignStmt();
            LVal lVal = lVal();
            assignStmt.addChild(lVal);
            ErrorHandle.getInstance().HChangeConst(lVal);
            assertSymGetSym(assignStmt, "Stmt e1", Symbol.ASSIGN);
            if (hopeSymGetSym(assignStmt, Symbol.GETINTTK)) {
                assertSymGetSym(assignStmt, "Stmt e2", Symbol.LPARENT);
                assertRParentGetSym(assignStmt);
            } else {
                assignStmt.addChild(exp());
            }
            assertSemicnGetSym(assignStmt);
            return assignStmt;
        } else if (GramJudgeElement.isBlock()) {
            BlockStmt blockStmt = new BlockStmt();
            blockStmt.addChild(block());
            return blockStmt;
        } else {
            ExpStmt expStmt = new ExpStmt();
            if (symbol != Symbol.SEMICN) {
                expStmt.addChild(exp());
            }
            assertSemicnGetSym(expStmt);
            return expStmt;
        }
    }

    private IfStmt stmtIf() throws IOException {
        IfStmt ifStmt = new IfStmt();
        assertSymGetSym(ifStmt, "if e1", Symbol.IFTK);
        assertSymGetSym(ifStmt, "if e2", Symbol.LPARENT);
        ifStmt.addChild(cond());
        assertRParentGetSym(ifStmt);
        ifStmt.addChild(stmt());
        if (hopeSymGetSym(ifStmt, Symbol.ELSETK)) {
            ifStmt.addChild(stmt());
        }
        return ifStmt;
    }

    private WhileStmt stmtWhile() throws IOException {
        WhileStmt whileStmt = new WhileStmt();
        ErrorHandle.getInstance().initialLoop();
        assertSymGetSym(whileStmt, "while e1", Symbol.WHILETK);
        assertSymGetSym(whileStmt, "while e2", Symbol.LPARENT);
        whileStmt.addChild(cond());
        assertRParentGetSym(whileStmt);
        whileStmt.addChild(stmt());
        ErrorHandle.getInstance().leaveLoop();
        return whileStmt;
    }

    private ReturnStmt stmtReturn() throws IOException {
        ReturnStmt returnStmt = new ReturnStmt();
        assertSymGetSym(returnStmt, "return e1", Symbol.RETURNTK);
        if (symbol != Symbol.SEMICN) {
            returnStmt.addChild(exp());
        }
        assertSemicnGetSym(returnStmt);

        ErrorHandle.getInstance().FVoidFuncWithReturn(returnStmt);
        return returnStmt;
    }

    private PrintfStmt stmtPrintf() throws IOException {
        PrintfStmt printfStmt = new PrintfStmt();
        assertSymGetSym(printfStmt, "printf e1", Symbol.PRINTFTK);
        assertSymGetSym(printfStmt, "printf e2", Symbol.LPARENT);
        assertSymGetSym(printfStmt, "printf e3", Symbol.STRCON);
        boolean undefined = ErrorHandle.getInstance().AIllegalSym(printfStmt);
        while (hopeSymGetSym(printfStmt, Symbol.COMMA)) {
            printfStmt.addChild(exp());
        }
        assertRParentGetSym(printfStmt);
        assertSemicnGetSym(printfStmt);
        if (!undefined) {
            ErrorHandle.getInstance().LPrintfNotMatch(printfStmt);
        }
        return printfStmt;
    }

    private Exp exp() throws IOException {
        Exp exp = new Exp();
        exp.addChild(addExp());
        return exp;
    }

    private Cond cond() throws IOException {
        Cond cond = new Cond();
        cond.addChild(lOrExp());
        return cond;
    }

    private LVal lVal() throws IOException {
        LVal lval = new LVal();
        ErrorHandle.getInstance().CUndefinedName(this.token);
        assertSymGetSym(lval, "LVal e1", Symbol.IDENFR);
        while (hopeSymGetSym(lval, Symbol.LBRACK)) {
            lval.addChild(exp());
            assertRBrackAndGetSym(lval);
        }
        return lval;
    }

    private PrimaryExp primaryExp() throws IOException {
        PrimaryExp primaryExp = new PrimaryExp();

        if (hopeSymGetSym(primaryExp, Symbol.LPARENT)) {
            primaryExp.addChild(exp());
            assertSymGetSym(primaryExp, "primaryExp e1", Symbol.RPARENT);
        } else if (symbol == Symbol.INTCON) {
            primaryExp.addChild(number());
        } else {
            primaryExp.addChild(lVal());
        }
        return primaryExp;
    }

    private MyNumber number() throws IOException {
        MyNumber number = new MyNumber();

        assertSymGetSym(number, "number e1", Symbol.INTCON);
        return number;
    }

    private UnaryExp unaryExp() throws IOException {
        if (GramJudgeElement.isUnaryExpFunc()) {
            FuncUnaryExp funcUnaryExp = new FuncUnaryExp();
            ErrorHandle.getInstance().CUndefinedName(this.token);
            assertSymGetSym(funcUnaryExp, "unaryExp e1", Symbol.IDENFR);
            assertSymGetSym(funcUnaryExp, "unaryExp e2", Symbol.LPARENT);
            //成
            if (symbol != Symbol.RPARENT && symbol != Symbol.SEMICN) {
                funcUnaryExp.addChild(funcRParams());
            }
            assertRParentGetSym(funcUnaryExp);
            ErrorHandle.getInstance().DEFuncParamNotMatch(funcUnaryExp);
            return funcUnaryExp;
        } else if (GramJudgeElement.isUnaryOp()) {
            UnaryOpUnaryExp unaryOpUnaryExp = new UnaryOpUnaryExp();
            unaryOpUnaryExp.addChild(unaryOp());
            unaryOpUnaryExp.addChild(unaryExp());
            return unaryOpUnaryExp;
        } else {
            PrimaryUnaryExp primaryUnaryExp = new PrimaryUnaryExp();
            primaryUnaryExp.addChild(primaryExp());
            return primaryUnaryExp;
        }
    }

    private UnaryOp unaryOp() throws IOException {
        UnaryOp unaryOp = new UnaryOp();

        assertSymGetSym(unaryOp, "UnaryOp e1", Symbol.PLUS, Symbol.MINU, Symbol.NOT);
        return unaryOp;
    }

    private FuncRParams funcRParams() throws IOException {
        FuncRParams funcRParams = new FuncRParams();

        funcRParams.addChild(exp());
        while (hopeSymGetSym(funcRParams, Symbol.COMMA)) {
            funcRParams.addChild(exp());
        }
        return funcRParams;
    }

    private MulExp mulExp() throws IOException {
        MulExp mulExp = new MulExp();
        mulExp.addChild(unaryExp());

        while (symbolEql(Symbol.MULT, Symbol.DIV, Symbol.MOD)) {
            MulExp newMulExp = mulExp;
            mulExp = new MulExp();
            mulExp.addChild(newMulExp, this.word);
            getSym();
            mulExp.addChild(unaryExp());
        }
        return mulExp;
    }

    private AddExp addExp() throws IOException {
        AddExp addExp = new AddExp();
        addExp.addChild(mulExp());

        while (symbolEql(Symbol.PLUS, Symbol.MINU)) {
            AddExp newAddExp = addExp;
            addExp = new AddExp();
            addExp.addChild(newAddExp, this.word);
            getSym();
            addExp.addChild(mulExp());
        }
        return addExp;
    }

    private RelExp relExp() throws IOException {
        RelExp relExp = new RelExp();
        relExp.addChild(addExp());
        while (symbolEql(Symbol.LSS, Symbol.LEQ, Symbol.GRE, Symbol.GEQ)) {
            RelExp newRelExp = relExp;
            relExp = new RelExp();
            relExp.addChild(newRelExp, this.word);
            getSym();
            relExp.addChild(addExp());
        }
        return relExp;
    }

    private EqExp eqExp() throws IOException {
        EqExp eqExp = new EqExp();
        eqExp.addChild(relExp());
        while (symbolEql(Symbol.EQL, Symbol.NEQ)) {
            EqExp newEqExp = eqExp;
            eqExp = new EqExp();
            eqExp.addChild(newEqExp, this.word);
            getSym();
            eqExp.addChild(relExp());
        }
        return eqExp;
    }

    private LAndExp lAndExp() throws IOException {
        LAndExp lAndExp = new LAndExp();
        lAndExp.addChild(eqExp());
        while (symbolEql(Symbol.AND)) {
            LAndExp newLAndExp = lAndExp;
            lAndExp = new LAndExp();
            lAndExp.addChild(newLAndExp, this.word);
            getSym();
            lAndExp.addChild(eqExp());
        }
        return lAndExp;
    }

    private LOrExp lOrExp() throws IOException {
        LOrExp lOrExp = new LOrExp();
        lOrExp.addChild(lAndExp());
        while (symbolEql(Symbol.OR)) {
            LOrExp newLOrExp = lOrExp;
            lOrExp = new LOrExp();
            lOrExp.addChild(newLOrExp, this.word);
            getSym();
            lOrExp.addChild(lAndExp());
        }
        return lOrExp;
    }

    private ConstExp constExp() throws IOException {
        ConstExp constExp = new ConstExp();
        constExp.addChild(addExp());
        return constExp;
    }

    //断言，错误的话进入错误处理: )，正确的话向下读一个并加入语法树
    private void assertRParentGetSym(TreeFatherNode treeFatherNode) throws IOException {
        if (symbol != Symbol.RPARENT) {
            ErrorHandle.getInstance().JLackRParent(this.beforeRow);
        } else {
            treeFatherNode.addChild(this.word);
            getSym();
        }
    }

    //断言，错误的话进入错误处理: ]，正确的话向下读一个并加入语法树
    private void assertRBrackAndGetSym(TreeFatherNode treeFatherNode) throws IOException {
        if (symbol != Symbol.RBRACK) {
            ErrorHandle.getInstance().KLackRbrack(this.beforeRow);
        } else {
            treeFatherNode.addChild(this.word);
            getSym();
        }
    }

    //断言，错误的话进入错误处理: ;，正确的话向下读一个并加入语法树
    private void assertSemicnGetSym(TreeFatherNode treeFatherNode) throws IOException {
        if (symbol != Symbol.SEMICN) {
            ErrorHandle.getInstance().ILackSemicn(this.beforeRow);
        } else {
            treeFatherNode.addChild(this.word);
            getSym();
        }
    }

    private boolean symbolEql(Symbol symbol) {
        return this.symbol == symbol;
    }

    private boolean symbolEql(Symbol... symbols) {
        for (Symbol sym : symbols) {
            if (this.symbol == sym) {
                return true;
            }
        }
        return false;
    }

    /*
    断言某个符号，如果成立，就添加到语法树中，并得将读进下一个word
    否则 do nothing
     */
    private boolean hopeSymGetSym(TreeFatherNode treeFatherNode, Symbol symbol) throws IOException {
        if (this.symbol == symbol) {
            treeFatherNode.addChild(this.word);
            getSym();
            return true;
        }
        return false;
    }

    /*
    断言某个符号，如果成立，就添加到语法树中，并得将读进下一个word
    否则报错
     */
    private boolean assertSymGetSym(TreeFatherNode treeFatherNode, String error, Symbol symbol) throws IOException {
        if (this.symbol == symbol) {
            treeFatherNode.addChild(this.word);
            getSym();
            return true;
        }
        error(error);
        return false;
    }

    /*
    断言某些符号之一成立，如果成立，就添加到语法树中，并得将读进下一个word
    否则报错
     */
    private boolean assertSymGetSym(TreeFatherNode treeFatherNode, String error, Symbol... symbols) throws IOException {
        for (Symbol sym : symbols) {
            if (this.symbol == sym) {
                treeFatherNode.addChild(this.word);
                getSym();
                return true;
            }
        }
        error(error);
        return false;
    }

    /*
    不符合文法输出
     */
    private void error(String str) throws IOException {
        //fileWriter.write(
        //        str + "\nEToken:" + this.token + ",ESym:" + this.symbol + "ERROR");
        System.out.println(
                str + "\nEToken:" + this.token + ",ESym:" + this.symbol + "ERROR");
    }
}
