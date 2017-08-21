package cz.kebrt.html2latex.parser;

import cz.kebrt.html2latex.exception.FatalErrorException;
import org.junit.Test;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserIntegrationTest {
    //找到相对应的绝对路径。启动记事本文件
    private String PATH = "G:\\Program Files\\MiKTeX 2.9\\miktex\\bin\\x64/pdflatex.exe";
    private static final String NL = "\n";
    private static final String TAB = "\t";
    ExecutorService executor = Executors.newFixedThreadPool(5);
    ExecutorService web = Executors.newFixedThreadPool(5);
    private List<Future<String>> futures = new ArrayList<Future<String>>();
    private String basePath = "H://tex";

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
    public void loadNet() throws IOException, SQLException, FatalErrorException, ExecutionException, InterruptedException {

        Connection connection = DriverManager.getConnection("jdbc:mysql://mysql.aixuexi.com:3306/tiku", "root", "root123");
        PreparedStatement preparedStatement = connection.prepareStatement("select* from topic_content  limit 100,100");
        ResultSet resultSet = preparedStatement.executeQuery();
        String temp = basePath + "/" + System.currentTimeMillis();
        File file = new File(temp, "result.tex");
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
        bufferedWriter.write("\\documentclass[UTF8]{ctexart}");
        bufferedWriter.newLine();
        bufferedWriter.write("\\usepackage{graphicx}");
        bufferedWriter.newLine();
        bufferedWriter.write("\\begin{document}");
        bufferedWriter.newLine();
        while (resultSet.next()) {
            String topic_content_html = resultSet.getString("topic_content_html");
            Future<String> submit = executor.submit(new ParseServer(topic_content_html, web, temp));
            futures.add(submit);
        }
        for (Future<String> feature : futures) {
            bufferedWriter.write(feature.get());
            bufferedWriter.newLine();
        }

        bufferedWriter.write("\\end{document}");
        bufferedWriter.newLine();
        bufferedWriter.close();

        List<String> commend = new ArrayList<String>();
        commend.add(PATH);
        commend.add("-synctex=1");

        commend.add("-shell-escape");
        commend.add("-interaction=nonstopmode");
        commend.add(file.getAbsolutePath());
        commend.add("-output-directory");
        commend.add(parentFile.getAbsolutePath());
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commend);
            Process start = builder.start();
            InputStream inputStream = start.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line=br.readLine())!=null){
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
