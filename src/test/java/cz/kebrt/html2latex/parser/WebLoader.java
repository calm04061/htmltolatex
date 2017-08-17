package cz.kebrt.html2latex.parser;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

public class WebLoader implements Callable<String> {
    private String url;
    private String basePath;

    public WebLoader(String url, String basePath) {
        this.url = url;
        this.basePath = basePath;
    }

    public String call() throws Exception {
        URL uri = null;
        FileOutputStream fos = null;
        InputStream inputStream= null;
        try {
            uri = new URL(url);
            String fragment = uri.getPath().substring(1);

            inputStream = uri.openStream();
            File file = new File(basePath, fragment);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            fos = new FileOutputStream(file);
            IOUtils.copy(inputStream, fos);
            return fragment;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos!=null){
                IOUtils.closeQuietly(fos);
            }
            if(inputStream!=null){
                IOUtils.closeQuietly(inputStream);
            }
        }
        return null;
    }
}
