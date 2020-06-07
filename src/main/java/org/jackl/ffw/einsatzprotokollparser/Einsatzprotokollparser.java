package org.jackl.ffw.einsatzprotokollparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jackl.ffw.einsatzprotokollparser.objects.Einsatzdaten;
import org.jackl.ffw.einsatzprotokollparser.objects.Einsatzmittel;
import org.jackl.ffw.einsatzprotokollparser.objects.Einsatzprotokoll;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@Log4j2
public class Einsatzprotokollparser {
    private static final Set<String> keywords = new HashSet<>(Arrays.asList("Einsatzdaten", "Einsatzmittel", "Meldungen"));
    private static final HashMap<String, String> einsatzdatenKeywords = new HashMap<>(9);
    static {
        einsatzdatenKeywords.put("Einsatznummer:", "setEinsatznummer");
        einsatzdatenKeywords.put("Datum:", "setDatum");
        einsatzdatenKeywords.put("Meldender:", "setMeldender");
        einsatzdatenKeywords.put("Einsatzstichwort:", "setEinsatzstichwort");
        einsatzdatenKeywords.put("Ort:", "setOrt");
        einsatzdatenKeywords.put("Ortsteil:", "setOrtsteil");
        einsatzdatenKeywords.put("Stra\u00dfe:", "setStrasse");
        einsatzdatenKeywords.put("Objekt:", "setObjekt");
        einsatzdatenKeywords.put("Gefahrenmeldeanlage:", "setGefahrenmeldeanlage");
    }

    @SuppressWarnings("UnstableApiUsage")
    @SneakyThrows
    public static void main(String[] args) {
        if (args.length == 1) {
            File inputfile = new File(args[0]);
            if (inputfile.exists() && inputfile.canRead() && Files.getFileExtension(inputfile.getName()).equals("pdf")) {
                Einsatzprotokoll einsatzprotokoll = new Einsatzprotokollparser(inputfile).getEinsatzprotokoll();
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                File outputfile = new File(Files.getNameWithoutExtension(inputfile.getName()) + ".json");
                mapper.writeValue(outputfile, einsatzprotokoll);
                System.out.println(String.format("The PDF File has been successfully parsed, the output is saved as %s", outputfile.getAbsolutePath()));
                return;
            }
        }
        System.err.println("Please specify a readable PDF as Input Parameter!");
    }

    private BufferedReader reader;
    private String line;
    private String firstWord;
    private String lastWords;
    private Iterator<String> lineIterator;
    private final StringBuilder unrecognized = new StringBuilder();
    private final Einsatzprotokoll einsatzprotokoll = new Einsatzprotokoll();

    public Einsatzprotokollparser(File file) {
        log.debug("Creating an Einsatzauftragsparser with a file as Input");
        parseFile(file);
    }

    public Einsatzprotokollparser(InputStream is) {
        log.debug("Creating an Einsatzauftragsparser with an InputStream as Input");
        parseInputStream(is);
    }

    public Einsatzprotokoll getEinsatzprotokoll() {
        return einsatzprotokoll;
    }

    private void parseInputStream(InputStream inputStream) {
        try {
            @Cleanup RandomAccessBuffer buffer = new RandomAccessBuffer(inputStream);
            parse(buffer);
        } catch (IOException e) {
            log.throwing(e);
        }
    }

    private void parseFile(File file) {
        try {
            @Cleanup RandomAccessBufferedFileInputStream inputStream = new RandomAccessBufferedFileInputStream(file);
            parse(inputStream);
        } catch (IOException e) {
            log.throwing(e);
        }
    }

    private void parse(RandomAccessRead inputStream) {
        try {
            val parser = new PDFParser(inputStream);
            parser.parse();
            @Cleanup PDDocument pdDocument = parser.getPDDocument();
            String parsedText = new PDFTextStripper().getText(pdDocument);
            lineIterator = Splitter.onPattern("\\R").omitEmptyStrings().trimResults().splitToList(parsedText).iterator();
            parseString(parsedText);
        } catch (IOException e) {
            log.throwing(e);
        }
    }

    private void parseString(String input) {
        nextLine();
        while (line != null && !line.equals("EOF")) {
            switch (firstWord) {
                case "Einsatzdaten":
                    log.debug("Found keyword Einsatzdaten");
                    einsatzprotokoll.setEinsatzdaten(parseEinsatzdaten());
                    break;
                case "Einsatzmittel":
                    log.debug("Found keyword Einsatzmittel");
                    einsatzprotokoll.setEinsatzmittel(parseEinsatzmittel());
                    break;
                case "Meldungen":
                    log.debug("Found keyword Meldungen");
                    nextLine();
                    break;
                default:
                    log.debug("Line unrecognized: \"{}\"", line);
                    unrecognized.append("\n").append(line);
                    nextLine();
                    break;
            }
        }
        einsatzprotokoll.setUnrecognized(unrecognized.toString());
    }

    private Einsatzdaten parseEinsatzdaten() {
        nextLine();
        Einsatzdaten einsatzdaten = new Einsatzdaten();
        Class<? extends Einsatzdaten> e = einsatzdaten.getClass();
        String currentToken = "unerkannt";
        try {
            while (breakCondition() && line != null) {
                String v = einsatzdatenKeywords.get(firstWord);
                if (v != null) {
                    currentToken = v;
                    e.getDeclaredMethod(v, String.class).invoke(einsatzdaten, lastWords);
                } else {
                    if (firstWord.contains(":")) {
                        currentToken = "unerkannt";
                    }
                    String o = (String) e.getDeclaredMethod(currentToken.replace("set", "get")).invoke(einsatzdaten);
                    e.getDeclaredMethod(currentToken, String.class).invoke(einsatzdaten, o + " " + line);
                }
                nextLine();
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return einsatzdaten;
    }


    private boolean breakCondition() {
        return !keywords.contains(firstWord);
    }

    private List<Einsatzmittel> parseEinsatzmittel() {
        nextLine();
        ArrayList<Einsatzmittel> einsatzmittelList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<einheit>.+?)(?<staerke>(?<zf>\\d+)/(?<gf>\\d+)/(?<unr>\\d+),\\s(?<agt>\\d+)\\sAGT)\\s(?<alarm>\\d{1,2}:\\d{2}:\\d{2}\\S*|--:--:--)\\s(?<s3>\\d{1,2}:\\d{2}:\\d{2}\\S*|--:--:--)\\s(?<s4>\\d{1,2}:\\d{2}:\\d{2}\\S*|--:--:--)\\s(?<s1>\\d{1,2}:\\d{2}:\\d{2}\\S*|--:--:--)\\s(?<ende>\\d{1,2}:\\d{2}:\\d{2}\\S*|--:--:--)");
        while (breakCondition()) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                Einsatzmittel einsatzmittel = new Einsatzmittel(m.group("einheit"), m.group("staerke"), Integer.parseInt(m.group("zf")), Integer.parseInt(m.group("gf")), Integer.parseInt(m.group("unr")), Integer.parseInt(m.group("agt")), m.group("alarm"), m.group("s3"), m.group("s4"), m.group("s1"), m.group("ende"));
                einsatzmittelList.add(einsatzmittel);
                log.debug("Parsed: {}", einsatzmittel);
            }
            nextLine();
        }
        return einsatzmittelList;
    }

    private void nextLine() {
        if (lineIterator.hasNext()) {
            line = lineIterator.next();
            String[] split = line.split("\\s", 2);
            firstWord = split[0].trim();
            lastWords = (split.length > 1) ? split[1].trim() : null;
        } else {
            line = "EOF";
        }
    }
}
