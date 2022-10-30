import java.io.File;
import java.io.FileInputStream;
import java.io.PushbackInputStream;

import gccBin.ERROR.ErrorHandle;
import gccBin.Gram.Gram;
import gccBin.Lex.Lex;
import gccBin.MIPS.MM;
import gccBin.MIPS.MIPS;
import gccBin.MidCode.MidCode;

public class Compiler {
    public static void main(String[] args)  {
        File inputFile = new File("testfile.txt");
        try{
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            PushbackInputStream input = new PushbackInputStream(fileInputStream);

            Lex.getInstance().initialLexInput(input);
            Gram.getInstance().initialGramOutFile();
            ErrorHandle.getInstance().open();
            MidCode.getInstance().openMidCodeWriter();
            MM.get().open();
            MIPS.getInstance().openMipsWriter();

            Gram.getInstance().gramStart();
            MidCode.getInstance().beginMidCodeGen();
            MidCode.getInstance().closeMidCodeWriter();
            MIPS.getInstance().beginTransLate();

            MIPS.getInstance().closeMIPSWriter();
            MM.get().close();
            fileInputStream.close();//关闭文件
            Gram.getInstance().closeGramOutFile();
            ErrorHandle.getInstance().close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void testLex(){
        File inputFile = new File("testfile.txt");
        try{
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            PushbackInputStream input = new PushbackInputStream(fileInputStream);
            Lex.getInstance().initialLexInput(input);
            Lex.getInstance().lexAnalysisStart();
            fileInputStream.close();//关闭文件
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void textError(){
        File inputFile = new File("testfile.txt");
        try{
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            PushbackInputStream input = new PushbackInputStream(fileInputStream);

            Lex.getInstance().initialLexInput(input);
            Gram.getInstance().initialGramOutFile();
            ErrorHandle.getInstance().open();

            Gram.getInstance().gramStart();

            fileInputStream.close();//关闭文件
            Gram.getInstance().closeGramOutFile();
            ErrorHandle.getInstance().close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
