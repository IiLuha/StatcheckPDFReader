import org.apache.commons.math3.special.Gamma;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReaderExample {

    public static void main(String[] args) {
        try (PDDocument document = Loader.loadPDF(new File("article.pdf"));
             FileOutputStream stream = new FileOutputStream(Path.of("output.txt").toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
//            System.out.println(text);
//            stream.write(text.getBytes());
            int pre = 0;
            List<Float> chiSqs = new ArrayList<>();
            List<Integer> dfs = new ArrayList<>();
            List<PairPSymbol> pValuesFromArticle = new ArrayList<>();
            String regex = "χ2";
            while (true) {
                int i = text.indexOf(regex, pre + 2);
                if (i == -1) break;
                pre = i;
                i = text.indexOf("(", i);
                if (!Character.isDigit(text.charAt(i + 1))) continue;
                float digit = findDigit(text, i, 1);
                dfs.add((int) digit);
                i = text.indexOf(") = ", i);
                if (i > pre + 100) continue;
                digit = findDigit(text, i, 4);
                chiSqs.add(digit);
                i = text.indexOf("p ", i);
                char symbol = text.charAt(i + 2);
                if (i > pre + 100) continue;
                digit = findDigit(text, i, 4);
                pValuesFromArticle.add(new PairPSymbol(digit, symbol));
            }
            for (int i = 0; i < chiSqs.size(); i++) {
                float chiSq = chiSqs.get(i);
                int df = dfs.get(i);
                PairPSymbol pair = pValuesFromArticle.get(i);
                char symbol = pair.symbol;
                double pValueFromArticle = pair.pValue;
                double pValue = 1. - Gamma.regularizedGammaP(df * 0.5, chiSq * 0.5);
                System.out.printf("χ2 = %1.2f | p-value = %1.5f", chiSq, pValue);
                switch (symbol) {
                    case '<' -> {
                        System.out.printf(pValue < pValueFromArticle ?
                                (" <  p from article = %1.5f | Consistent\r\n") :
                                (" >= p from article = %1.5f | Inconsistent\r\n"), pValueFromArticle);
                    }
                    case '>' -> {
                        System.out.printf(pValue > pValueFromArticle ?
                                (" >  p from article = %1.5f | Consistent\r\n") :
                                (" <= p from article = %1.5f | Inconsistent\r\n"), pValueFromArticle);
                    }
                    case '=' -> {
                        System.out.printf(pValue == pValueFromArticle ?
                                (" =  p from article = %1.5f | Consistent\r\n") :
                                (" != p from article = %1.5f | Inconsistent\r\n"), pValueFromArticle);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static float findDigit(String text, int i, int offset) {
        int count = -1;
        int size = 0;
        List<Integer> value = new ArrayList<>();
        int j = i + offset;
        char maybeInt = text.charAt(j);
        for (; Character.isDigit(maybeInt) || isValidCharacter(maybeInt); j++, maybeInt = text.charAt(j)) {
            if (isNewLine(maybeInt)) continue;
            if (maybeInt == '.') {
                size = count;
            } else  {
                count++;
                value.add(Character.getNumericValue(maybeInt));
            }
        }
        if (size == 0) size = count;

        float digit = 0;
        for (j = 0; j < value.size(); j++) {
            int x = value.get(j);
            digit += x * Math.pow(10, size - j);
        }
        return digit;
    }

    private static boolean isValidCharacter(char c) {
        return c == '.' || isNewLine(c);
    }

    private static boolean isNewLine(char c) {
        return c == '\r' || c == '\n';
    }

    private static class PairPSymbol {
        double pValue;
        char symbol;

        public PairPSymbol(double pValue, char symbol) {
            this.pValue = pValue;
            this.symbol = symbol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PairPSymbol that = (PairPSymbol) o;
            return Double.compare(that.pValue, pValue) == 0 && symbol == that.symbol;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pValue, symbol);
        }
    }
}
