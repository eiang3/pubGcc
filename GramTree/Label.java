package GramTree;
/*
语法定义所有的左侧符号 ok
 */
public enum Label {
    CompUnit,
    Decl,
    ConstDecl,
    BType,
    ConstDef,
    ConstInitVal,
    VarDecl,
    VarDef,
    InitVal,
    FuncDef,
    MainFuncDef,
    FuncType,
    FuncFParams,
    FuncFParam,
    Block,
    BlockItem,
    Stmt,
    Exp,
    Cond,
    LVal,
    PrimaryExp,
    Number,
    UnaryExp,
    UnaryOp,
    FuncRParams,
    MulExp,
    AddExp,
    RelExp,
    EqExp,
    LAndExp,
    LOrExp,
    ConstExp
}
