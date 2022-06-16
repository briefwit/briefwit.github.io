import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TextDocumentReaderWriter {
    private final static String WRITE_FS_FILEPATH = "../DevCraft/";

    private final static String TITLE_PATTERN = "###### ";
    private final static String H1_PATTERN = "##### ";
    private final static String H2_PATTERN = "#### ";
    private final static String H3_PATTERN = "### ";

    private static int h1_Count = -1;
    // this is initialised at -1 so that the count startsfrom 0 when creating IDs.
    private static int h2_Count = 0;
    private static int h3_Count = 0;
    private static int image_Count = 0;
    private static int footnote = 0;

    private final static String ENDCONTENTSUMMARY_PATTERN = "##c";
    private final static String TITLE_META = "<meta property=\"og:title\" content=";
    private final static String HEAD_HTML_1 = "<!DOCTYPE html><html lang=\"en\"><head><meta name=\"viewport\" content=\"width=device-width\"><meta charset=\"utf-8\">";
    private final static String HEAD_HTML_2 = "<meta charset=\"utf-8\"/><link rel=\"stylesheet\" href=\"../Style.css\"></head>";
    private final static String CENTRE_MAIN = "<div id=\"main\" class=\"centre main\">";
    private final static int INDENT_PX = 20;

    private static ArrayList<String> listOfHeaderLinks = new ArrayList<String>();

    public static void main(String[] args) throws Exception {
        //args[0] = name of file
        BufferedReader textSourceReader = new BufferedReader(new InputStreamReader(new FileInputStream("../TextSource/"+args[0]+".txt"), StandardCharsets.UTF_8));
        File writeToHTML = new File(WRITE_FS_FILEPATH + args[0] + ".html");
        writeToHTML.delete();
        BufferedWriter essayHTMLWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(WRITE_FS_FILEPATH+args[0]+".html"), StandardCharsets.UTF_8));
        String currentLine = textSourceReader.readLine();
        while (currentLine != null) {
            writeNextTagsAndOrContent(textSourceReader, essayHTMLWriter, currentLine);
            currentLine = textSourceReader.readLine();
        }
        essayHTMLWriter.write("</div>");
        essayHTMLWriter.write("</body>");
        essayHTMLWriter.write("</html>");
        textSourceReader.close();
        essayHTMLWriter.close();
        System.out.println("Done.");
    }

    private static void writeNextTagsAndOrContent(BufferedReader reader, BufferedWriter writer, String currentLine) throws Exception {
        if (currentLine.contains(TITLE_PATTERN)) {
            writeTitleAndMetaTags(writer, currentLine);
        } else if (currentLine.contains(H1_PATTERN)) {
            h1_Count++;
            h2_Count = 0;
            h3_Count = 0;
            writer.write("<h1" + produceIDs(currentLine, H1_PATTERN) + h1H2H3LinksBegin(currentLine, H1_PATTERN) + "</h1>");
            writeContentsOfTitles(reader, writer, currentLine);
            writer.newLine();
        } else if (currentLine.contains(H2_PATTERN) && !(currentLine.contains(TITLE_PATTERN) && currentLine.contains(H1_PATTERN))) {
            h2_Count++;
            h3_Count = 0;
            writer.write("<h2" + produceIDs(currentLine, H2_PATTERN) + h1H2H3LinksBegin(currentLine, H2_PATTERN) + "</h2>");
            writeContentsOfTitles(reader, writer, currentLine);
            writer.newLine();
        } else if (currentLine.contains(H3_PATTERN) && !(currentLine.contains(TITLE_PATTERN) && currentLine.contains(H1_PATTERN) && currentLine.contains(H2_PATTERN))) {
            h3_Count++;
            writer.write("<h3" + produceIDs(currentLine, H3_PATTERN) + h1H2H3LinksBegin(currentLine, H3_PATTERN) + "</h3>");
            writeContentsOfTitles(reader, writer, currentLine);
            writer.newLine();
        }
    }

    private static void writeTitleAndMetaTags(BufferedWriter writer, String currentLine) throws Exception {
        writer.write(HEAD_HTML_1);
        writer.write(TITLE_META + "\""+ currentLine.split(TITLE_PATTERN)[1] + "\">");
        writer.write("<title>" + currentLine.split(TITLE_PATTERN)[1] + "</title>");
        writer.write(HEAD_HTML_2);
        writer.write("<body>");
        writer.write(CENTRE_MAIN);
        writer.newLine();
        writer.write("<a class= \"alink\" href=\"../index.html\">Click here to return to post history</a>");
        writer.newLine();
    }

    private static void writeContentsOfTitles(BufferedReader reader, BufferedWriter writer, String currentLine) throws Exception{
        currentLine = reader.readLine();
        image_Count = 0;
        footnote=0;
        while (!(currentLine.contains(ENDCONTENTSUMMARY_PATTERN))) {
            if (currentLine.equals("")) {
                //Nothing is done, ignore empty lines.
            } else if (currentLine.charAt(0) == '+') {
                writer.write("<ul><li>" + currentLine.split("\\+ ")[1] + "<a target=\"_blank\" href=\"https://encyclopediac.github.io/Encyclopediac's%20Totally%20Impartial%20Factsheet%20-%20Table%20of%20Contents.html\" style=\"color:blue;text-decoration:underline\">Table of Contents</a>" + "</li></ul>");
                writer.newLine();
            } else if (currentLine.contains("- LINK: ")) {
                writer.write("<ul><li>" + currentLine.split("- ")[1].split("http")[0] + "<a target=\"_blank\" href=\"http"+ currentLine.split("- ")[1].split("http")[1] + "\" style=\"color:blue;text-decoration:underline\">http" + currentLine.split("- ")[1].split("http")[1] + "</a>"+ "</li></ul>");
                writer.newLine();
            } else if (currentLine.charAt(0) == '-') {
                writer.write("<ul><li>" + currentLine.split("- ")[1] + "</li></ul>");
                writer.newLine();
            } else if (currentLine.charAt(0) == '<') {
                writer.write("<h4>" + currentLine + "</h4>");
                writer.newLine();
            } else if (currentLine.charAt(0) == '*') {
                int numberOfIndents = 1;
                for (int counter = 1; counter < currentLine.length(); counter++) {
                    if (currentLine.charAt(counter) == '*') {
                        numberOfIndents++;
                    } else {
                        break;
                    }
                }
                writer.write("<ul style=\"margin-left:" + INDENT_PX*numberOfIndents + "px\"" + "><li>" + currentLine.split("\\* ")[1] + "</li></ul>");
                writer.newLine();
            } else if (currentLine.contains("image")) {
                    String imagePath = h1_Count+ "-" + h2_Count + "-" + h3_Count + "-image-" + image_Count + ".png";
                    writer.write("<div id=\"" + imagePath.split(".png")[0] + "-main\" class=\"keyquotes images\">");
                    writer.newLine();
                    writer.write("<img src=\"images/" + imagePath+ "\" alt=\"" + currentLine.split("image ")[1] + "\">");
                    writer.newLine();
                    writer.write("</div>");
                    writer.newLine();
                    image_Count++;
            } else if (currentLine.charAt(0) == '/') {
                while(currentLine.contains("//")) {
                    String firsthalf = currentLine.split("//", 2)[0];
                    String secondHalf = currentLine.split("//", 2)[1].split("//", 2)[1];
                    String footnoteNum = currentLine.split("//", 2)[1].split("//", 2)[0];
                    String between = "<sup>[<a class= \"alink\" href=\"#footnote-" + footnoteNum + "\" id=\"footnote-" + footnoteNum + "-ret\">" + footnoteNum + "</a>]</sup>";
                    currentLine = firsthalf + between + secondHalf;
                }
                writer.write("<p>&emsp;" + currentLine.split("/", 2)[1] + "</p>");
                writer.newLine();
            } else if (currentLine.charAt(0) == '[') {
                writer.write("<p id=\"footnote-" + footnote + "\">[<a class= \"alink\" href=\"#footnote-" + footnote + "-ret\">" + footnote + "</a>]&ensp;" + currentLine.split("]", 2)[1] + "</p>");
                footnote++;
                writer.newLine();
            }
            currentLine = reader.readLine();
        }
    }

    private static String h1H2H3LinksBegin (String currentLine, String pattern) throws Exception {
        String linkText = "<a href=\"#" + h1_Count+ "-" + h2_Count + "-" + h3_Count + "_" + currentLine.split(pattern)[1].toLowerCase().replace(" ", "_").replace("&", "&amp;").replace("'", "_") + "\">" + currentLine.split(H3_PATTERN)[1] + "</a>";
        listOfHeaderLinks.add(linkText);
        return linkText;
    }

    private static String produceIDs(String currentLine, String pattern) {
        String linkID = " id=" + h1_Count+ "-" + h2_Count + "-" + h3_Count + "_" + currentLine.split(pattern)[1].toLowerCase().replace(" ", "_").replace("&", "&amp;").replace("'", "_") + ">";
        return linkID;
    }
}