package main;

import db.DBBroker;
import domain.Code;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Lazar Vujadinovic
 */
public class Main {

    private static List<String> reservedTerms;
    private static Map<String, String> glossary;
    private static List<Code> codesToTranslate;
    private static DBBroker dbb;

    public static void main(String[] args) {
        try {
            reservedTerms = getReservedTerms();
            glossary = getGlossaryMap();

            dbb = DBBroker.getINSTANCE();
            dbb.loadDriver();
            dbb.openConnection();
            codesToTranslate = dbb.getCodesToTranslate();

            for (Code c : codesToTranslate) {
                translate(c);
            }
            System.out.println("All codes translated.");

            dbb.saveEnglishCodes(codesToTranslate);
            dbb.closeConnection();

            saveNotTranslated();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static List<String> getReservedTerms() {
        return new ArrayList<>(Arrays.asList("#import", "extern", "path", "object",
                "data", "int", "float", "date", "bool", "loop",
                "this", "break", "if", "else", "exists", "exec"));
    }

    private static Map<String, String> getGlossaryMap() throws Exception {
        InputStream glossaryFile = new FileInputStream(new File("Translation-Glossary.xlsx"));
        XSSFWorkbook wb = new XSSFWorkbook(glossaryFile);

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            XSSFSheet sheet = wb.getSheetAt(i);
            XSSFRow row;

            Iterator rows = sheet.rowIterator();
            while (rows.hasNext()) {
                row = (XSSFRow) rows.next();
                if (row == null || row.getCell(0) == null || row.getCell(0).getStringCellValue().equals("German")) {
                    continue;
                }
                map.put(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue());
            }
        }
        System.out.println("Translation Glossary loaded.");
        return map;
    }

    private static void translate(Code c) {
        String newCode = c.getCode();
        for (Map.Entry<String, String> entrySet : glossary.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            newCode = newCode.replaceAll(key, value);
        }
        c.setCode(newCode);
        System.out.println("Code: " + c.getObjectID() + ", " + c.getDomainID() + " translated.");
    }

    private static void saveNotTranslated() throws FileNotFoundException {
        //load all codes
        StringBuilder sb = new StringBuilder();
        for (Code c : codesToTranslate) {
            sb.append(c.getCode());
        }
        //split on terms and remove characters like ,{}[]()!= and space
        String[] terms = sb.toString().split("[,|\\s+|{|}|[|]|(|)|\"|!=|=]");
        List<String> notTranslated = new ArrayList<>();
        for (String t : terms) {
            //if it is a comment, continue
            if (t.startsWith("/") || t.endsWith("/")) {
                continue;
            }
            notTranslated.add(t);
        }
        //remove all terms from dictionary, as well as reserved terms
        notTranslated.removeAll(reservedTerms);
        notTranslated.removeAll(glossary.keySet());
        notTranslated.removeAll(glossary.values());

        //remove duplicates
        Collections.sort(notTranslated);
        for (int i = notTranslated.size() - 1; i > 0; i--) {
            if (notTranslated.get(i).isEmpty() || notTranslated.get(i).equals(notTranslated.get(i - 1))) {
                notTranslated.remove(i);
            }
        }
        System.out.println("Not translated terms: " + notTranslated.size());
        //save not translated terms in file
        PrintWriter writer = new PrintWriter("notTranslated.xml");
        writer.println("<NotTranslatedTerms size=\"" + notTranslated.size() + "\">");
        for (String ntt : notTranslated) {
            writer.println("\t<NotTranslatedTerm>");
            writer.println("\t\t" + ntt);
            writer.println("\t</NotTranslatedTerm>");
        }
        writer.println("</NotTranslatedTerms>");
        writer.close();
        System.out.println("NotTranslatedTerms file saved.");
    }
}
