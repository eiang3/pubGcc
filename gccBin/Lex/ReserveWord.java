package gccBin.Lex;

import java.util.HashSet;

// Keyword list
public class ReserveWord {
    private static ReserveWord instance;

    private final HashSet<String> reserve = new HashSet<>();

    private ReserveWord(){
        reserve.add("const");
        reserve.add("int");
        reserve.add("void");
        reserve.add("main");
        reserve.add("if");
        reserve.add("else");
        reserve.add("while");
        reserve.add("break");
        reserve.add("continue");
        reserve.add("return");
        reserve.add("getint");
        reserve.add("printf");
    }

    public static ReserveWord getInstance(){
        if(instance == null )
            instance = new ReserveWord();
        return instance;
    }

    public boolean isReserve(String str){
        return this.reserve.contains(str);
    }

    public Symbol getReserveSym(String str){
        Symbol sym ;
        switch(str){
            case "main":
                sym = Symbol.MAINTK;
                break;
            case "const":
                sym = Symbol.CONSTTK;
                break;
            case "int":
                sym = Symbol.INTTK;
                break;
            case "break":
                sym = Symbol.BREAKTK;
                break;
            case "continue":
                sym = Symbol.CONTINUETK;
                break;
            case "if":
                sym = Symbol.IFTK;
                break;
            case "else":
                sym = Symbol.ELSETK;
                break;
            case "while":
                sym = Symbol.WHILETK;
                break;
            case "getint":
                sym = Symbol.GETINTTK;
                break;
            case "printf":
                sym = Symbol.PRINTFTK;
                break;
            case "return":
                sym = Symbol.RETURNTK;
                break;
            case "void":
                sym = Symbol.VOIDTK;
                break;
            default:
                sym = Symbol.NOTRESERVE;
        }
        return sym;
    }
}
