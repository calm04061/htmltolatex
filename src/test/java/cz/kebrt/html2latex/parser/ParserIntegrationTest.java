package cz.kebrt.html2latex.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import cz.kebrt.html2latex.exception.FatalErrorException;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;

public class ParserIntegrationTest {
    private static final String NL = "\n";
    private static final String TAB = "\t";

    @Test
    public void shouldNotConvertNonHTMLString() {
        String input = "there is no HTML here!";
        String expectedOutput = input;
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertSpecialCharacters() {
        String input = "# $ % ^ & _ { } ~ \\";
        String expectedOutput = "\\# \\$ \\% \\textasciicircum & \\_ \\{ \\} \\textasciitilde $\\backslash$";
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertBoldTag() {
        String input = "there is a <strong>bold text</strong> tag";
        String expectedOutput = "there is a \\textbf{bold text} tag";
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertUnderlineTag() {
        String input = "there is a <u>underline text</u> tag";
        String expectedOutput = "there is a \\underline{underline text} tag";
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertItalicsTag() {
        String input = "there is a <i>italics text</i> tag";
        String expectedOutput = "there is a \\textit{italics text} tag";
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertUnorderedListTag() {
        String input = "there is a list tag <ul><li>text A</li><li>text B</li><li>text C</li></ul>";
        String expectedOutput = "there is a list tag " + NL + "\\begin{itemize}" + NL + TAB + "\\item text A" + NL + TAB + "\\item text B" + NL + TAB
                + "\\item text C" + NL + "\\end{itemize}";
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertOrderedListTag() {
        String input = "there is a list tag <ol><li>text A</li><li>text B</li><li>text C</li></ol>";
        String expectedOutput = "there is a list tag " + NL + "\\begin{enumerate}" + NL + TAB + "\\item text A" + NL + TAB + "\\item text B" + NL + TAB
                + "\\item text C" + NL + "\\end{enumerate}";
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertMultipleTags() {
        String input = "there is a <strong>bold text</strong> <u>underline text</u> <i>italics text</i> tags";
        String expectedOutput = "there is a \\textbf{bold text}\\underline{underline text}\\textit{italics text} tags";
        parse(input, expectedOutput);
    }

    @Test
    public void shouldConvertNestedTagStructure() {
        String input = "there is a list tag <ul><li><strong><u><i>text A</i></u></strong></li><li>text B</li><li>text C</li></ul>";
        String expectedOutput = "there is a list tag " + NL + "\\begin{itemize}" + NL + TAB + "\\item \\textbf{\\underline{\\textit{text A}}}" + NL + TAB
                + "\\item text B" + NL + TAB + "\\item text C" + NL + "\\end{itemize}";
        parse(input, expectedOutput);
    }

    @Test
    public void loadNet() throws IOException, SQLException, FatalErrorException {

//        Connection connection = DriverManager.getConnection("jdbc:mysql://mysql.aixuexi.com:3306/tiku", "root", "root123");
//        PreparedStatement preparedStatement = connection.prepareStatement("select* from topic_content");
//        ResultSet resultSet = preparedStatement.executeQuery();
//
//        FileOutputStream fos=new FileOutputStream("result.txt");
//        BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(fos));
//        while (resultSet.next()) {
//            String topic_content_html = resultSet.getString("topic_content_html");
//            String parse = parse(topic_content_html);
////            bufferedWriter.write(topic_content_html);
//            bufferedWriter.write(parse);
//            bufferedWriter.write("\t\n");
//        }
        String parse = parse("<div class=\"axx_piece\">\n" +
                "      <p>电梯上升18米记作\\[18\\]米，那么\\[-6\\]米表示__________．</p>\n" +
                "    </div>");
        System.out.println(parse);
    }

    private void parse(String input, String expectedOutput) {
        try {
            Parser parser = new Parser(input);
            parser.parse();
            String output = parser.getParserHandler().getConverter().getWriter().getOutput();
            assertEquals(expectedOutput, output);
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    private String parse(String input) throws FatalErrorException {
        Parser parser = new Parser(input);
        parser.parse();
        return parser.getParserHandler().getConverter().getWriter().getOutput();

    }
}
