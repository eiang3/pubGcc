import SymbolTableBin.APIIRSymTable;
import gccBin.ERROR.ErrorHandle;
import gccBin.Gram.Gram;
import gccBin.Lex.Lex;
import gccBin.MIPS.MIPS;
import gccBin.MidCode.AfirstProcess.IRFirst;
import gccBin.MidCode.AoriginalProcess.IRGenerate;
import gccBin.MidCode.AzeroProcess.IRZero;

public class Compiler {
    public static void main(String[] args) {

        try {
            //基础：语法树构建+错误处理//
            Lex.getInstance().open();
            Gram.getInstance().open();
            ErrorHandle.getInstance().open();

            Gram.getInstance().start();

            Gram.getInstance().close();
            //Lex.close在LexStream里
            ErrorHandle.getInstance().close();
            //语法树构建完毕//


            //中间代码生成开始//
            IRGenerate.getInstance().open();
            IRGenerate.getInstance().begin();
            IRGenerate.getInstance().close();


            //刷新一次符号表
            APIIRSymTable.getInstance().refreshTable();


            //第一次处理IR，得到全局寄存器分配，对符号表和一些变量进行重命名//
            IRFirst.getInstance().open();
            IRFirst.getInstance().begin();
            IRFirst.getInstance().close();


            //第二次处理IR，进行公共子表达式删除，死代码删除
            IRZero.getInstance().open();
            IRZero.getInstance().begin();
            IRZero.getInstance().close();


            //对IR进行翻译//
            MIPS.getInstance().open();
            MIPS.getInstance().begin();
            MIPS.getInstance().close();
            //finish//
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
