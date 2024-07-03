import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReaderExample {

    public static void main(String[] args) {
        try (PDDocument document = Loader.loadPDF(new File("article.pdf"));
             FileOutputStream stream = new FileOutputStream(Path.of("output.txt").toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
//            System.out.println(text);
//            stream.write(text.getBytes());
            int pre = 0;
            List<Float> his = new ArrayList<>();
            String regex = "χ2";
            while (true) {
                int i = text.indexOf(regex, pre + 2);
                if (i == -1) break;
                pre = i;
                i = text.indexOf(") = ", i);
                if (i > pre + 100) continue;
                int count = -1;
                int size = 0;
                List<Integer> value = new ArrayList<>();
                for (int j = i + 4; j < i + 14; j++) {
                    char maybeInt = text.charAt(j);
                    if (maybeInt == ',') {
                        if (size == 0) size = count;
                        break;
                    }
                    if (maybeInt == '.') {
                        size = count;
                    }
                    if (Character.isDigit(maybeInt)) {
                        count++;
                        value.add(Character.getNumericValue(maybeInt));
                    }
                }
                float hi = 0;
                for (int j = 0; j < value.size(); j++) {
                    int x = value.get(j);
                    hi += x * Math.pow(10, size - j);
                }
                if (hi != 0) his.add(hi);
            }
            for (Float hi : his) {
                System.out.println("χ2 = " + hi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
