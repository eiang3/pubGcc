import java.io.File;
import java.io.FileInputStream;
import java.io.PushbackInputStream;

import gccBin.ERROR.ErrorHandle;
import gccBin.Gram.Gram;
import gccBin.Lex.Lex;
import gccBin.MIPS.MIPS;
import gccBin.MidCode.original.IRGenerate;

public class Compiler {
    public static void main(String[] args)  {
        File inputFile = new File("testfile.txt");
        try{
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            PushbackInputStream input = new PushbackInputStream(fileInputStream);

            Lex.getInstance().initialLexInput(input);
            Gram.getInstance().initialGramOutFile();
            ErrorHandle.getInstance().open();
            IRGenerate.getInstance().openMidCodeWriter();
            //MIPS.get().open();
            MIPS.getInstance().openMipsWriter();

            Gram.getInstance().gramStart();
            IRGenerate.getInstance().beginMidCodeGen();
            IRGenerate.getInstance().closeMidCodeWriter();

            MIPS.getInstance().beginTransLate();

            MIPS.getInstance().closeMIPSWriter();
            //MIPS.get().close();
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
