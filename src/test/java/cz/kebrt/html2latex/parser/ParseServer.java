package cz.kebrt.html2latex.parser;

import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseServer implements Callable<String> {
    private String source;
    private ExecutorService executor;
//    static Pattern p = Pattern.compile("http://[w+|\\-][\\.\\w]+/[\\w+|\\/|\\.]*[\\?\\w=\\w[\\&\\w=\\w]*]+",Pattern.CASE_INSENSITIVE );
    static Pattern p = Pattern.compile("http://[\\w|-]+[\\.\\w]+/[\\w+|\\/|\\.]*[\\?\\w=\\w[\\&\\w=\\w]*]+",Pattern.CASE_INSENSITIVE );
    private Map<String,Future<String>> futureMap = new ConcurrentHashMap<String, Future<String>>();
    private String basePath;
    public ParseServer(String source, ExecutorService executor,String basePath) {
        this.source = source;
        this.executor = executor;
        this.basePath = basePath;
    }

    public String call() throws Exception {
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            String group = matcher.group();
            Future<String> submit = executor.submit(new WebLoader(group,basePath));
            futureMap.put(group,submit);
        }
        for(Map.Entry<String,Future<String>> e:futureMap.entrySet()){
            String key = e.getKey();
            Future<String> value = e.getValue();
            source=source.replace(key,value.get());
        }
        source=source.replace("\\[","$").replace("\\]","$");//.replace("\r\n","\r\n&");
        Parser parser = new Parser(source.trim());
        parser.parse();
        return parser.getParserHandler().getConverter().getWriter().getOutput();
    }

    public static void main(String[] args) {
        String m = "asdasd\\\\includegraphics{http://www-aa.aixuexi.com/IMP20170426/1241504f68774b128a8fc4ad86951872/content/content.files/image1.png?123=2&sdfsd=2}asdasdasdasdasd\\\\includegraphics{http://www.aixuexi.com/IMP20170426/1241504f68774b128a8fc4ad86951872/content/content.files/image1.png?adfadf=1}asdasdasd";
        Matcher matcher = p.matcher(m);
        while (matcher.find()) {
            String group = matcher.group();
            System.out.println(group);
        }

    }
}
