import java.io.*;

public class Application {

    private static final String extractText="extract";
    private static final String compressText="compress";
    public static void main(String[] args) throws IOException {
        if(args.length<2){
            System.out.println("Use for extract or compress texts from/to xeen.dat\nextractor.jar [XEEN.DAT path] [extract|compress] [xeen.dat.xls path] [out XEEN.DAT path]");
            System.exit(0);
        }
        File dat = new File(args[0]);
        if(extractText.equals(args[1])){
            System.out.println("Extracting");
            DatExtractor ec=new DatExtractor();
            ec.extractText(dat);
        }
        if(compressText.equals(args[1])){
            System.out.println("Compressing");
            DataCompressor dc = new DataCompressor();
            File xls = new File(args[2]);
            String outFilename="." + File.separator + dat.getName() + ".translated.dat";
            if(args.length==4){
                outFilename=args[3];
            }
            dc.compressTexts(dat, xls,outFilename);
        }
    }
}
