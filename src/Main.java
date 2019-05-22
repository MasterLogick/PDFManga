import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static String name_url = /*"11221-.hackg.u..html";*/ "11874-accel-world.html";
    static int amount = Integer.MAX_VALUE;
    static int threadCount = 10;

    public static void main(String[] args) {
        PDDocument doc = new PDDocument();
        String[] select = body();
        ArrayList<String> urls = urls(select);
        System.out.println(urls.size() + " chapters");
        ArrayList<String> imgURLs = imgURLs(urls);
        System.out.println(imgURLs.size() + " images");
        PDPage[] buffer = new PDPage[imgURLs.size()];
        int size = imgURLs.size() / threadCount;
        AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < threadCount - 1; i++) {
            int finalI = i;
            new Thread(() -> {
                int a = finalI;
                for (int j = 0; j < size; j++) {
                    try {
                        BufferedImage bi = ImageIO.read(new URL(imgURLs.get(size * a + j)));
                        PDPage page = new PDPage();
                        page.setCropBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                        page.setMediaBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                        PDImageXObject pdImage = LosslessFactory.createFromImage(doc, bi);
                        PDPageContentStream contents = new PDPageContentStream(doc, page);
                        contents.drawImage(pdImage, 0, 0);
                        contents.close();
                        buffer[size * a + j] = page;
                        System.out.println("Page " + (size * a + j) + " copied. " + (count.incrementAndGet()) + " of " + imgURLs.size());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        new Thread(() -> {
            int a = threadCount - 1;
            for (int j = 0; j < size + imgURLs.size() % threadCount; j++) {
                try {
                    BufferedImage bi = ImageIO.read(new URL(imgURLs.get(size * a + j)));
                    PDPage page = new PDPage();
                    page.setCropBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                    page.setMediaBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                    PDImageXObject pdImage = LosslessFactory.createFromImage(doc, bi);
                    PDPageContentStream contents = new PDPageContentStream(doc, page);
                    contents.drawImage(pdImage, 0, 0);
                    contents.close();
                    buffer[size * a + j] = page;
                    System.out.println("Page " + (size * a + j) + " copied. " + (count.incrementAndGet()) + " of " + imgURLs.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        while (count.get() != imgURLs.size()) {
        }
        for (PDPage bi :
                buffer) {
            if (bi == null) System.out.println("aaa");
        }
        System.out.println("completed");
        int a = 0;
        for (PDPage bi :
                buffer) {
            doc.addPage(bi);
            System.out.println(++a + " page has been drew.");
        }
        try {
            File f = new File("/home/user/" + name_url.substring(0, name_url.length() - ".html".length()) + ".pdf");
            f.createNewFile();
            f.delete();
            f.createNewFile();
            doc.save(f);
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*int bufferSize = 1024;
        BufferedImage[] buffer = new BufferedImage[bufferSize];
        int part = 0;
        int i = 0;
        int j = 0;
        while (i < imgURLs.size()) {
            for (j = 0; j < bufferSize && j < imgURLs.size(); j++, i++) {
                try {
                    buffer[j] = ImageIO.read(new URL(imgURLs.get(i)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(imgURLs.get(i) + " buffered. " + (((float) i) / imgURLs.size() * 100) + "%. " + i + " from " + imgURLs.size());
            }
            PDDocument doc = new PDDocument();
            for (int k = 0; k < j; k++) {
                BufferedImage bi = buffer[k];
                try {
                    PDPage page = new PDPage();
                    page.setCropBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                    page.setMediaBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                    doc.addPage(page);
                    PDImageXObject pdImage = null;
                    pdImage = LosslessFactory.createFromImage(doc, bi);
                    PDPageContentStream contents = null;
                    contents = new PDPageContentStream(doc, page);
                    contents.drawImage(pdImage, 0, 0);
                    contents.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                buffer[k]=null;
            }
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            PDDocument doc1 = new PDDocument();
            try {
                int pageCounter = 0;
                for (PDPage page1 : doc.getPages()) {
                    // note that the page number parameter is zero based
                    BufferedImage bi = pdfRenderer.renderImageWithDPI(pageCounter, 144, ImageType.RGB);
                    pageCounter++;
                    PDPage page = new PDPage();
                    page.setCropBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                    page.setMediaBox(new PDRectangle(bi.getWidth(), bi.getHeight()));
                    doc1.addPage(page);
                    PDImageXObject pdImage = null;
                    pdImage = LosslessFactory.createFromImage(doc1, bi);
                    PDPageContentStream contents = null;
                    contents = new PDPageContentStream(doc1, page);
                    contents.drawImage(pdImage, 0, 0);
                    contents.close();
                    System.out.println("Page: " + pageCounter + " converted");
                }

                doc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                File f = new File("/home/user/" + name_url.substring(0, name_url.length() - ".html".length()) + "-part-" + part + ".pdf");
                f.createNewFile();
                f.delete();
                f.createNewFile();
                doc1.save(f);
                doc1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.gc();
        }*/
    }

    public static ArrayList<String> imgURLs(ArrayList<String> urls) {
        ArrayList<String> ret = new ArrayList<>();
        for (String url :
                urls) {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://mangachan.me" + url)).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String sub = response.body().substring(response.body().indexOf("\"fullimg\":[\"") + "\"fullimg\":[\"".length());
            sub = sub.substring(0, sub.indexOf("\",]"));
            String[] uris = sub.split("\",\"");
            for (String s :
                    uris) {
                ret.add(s);
            }
            System.out.println("Page: " + url + " parsed");
        }
        return ret;
    }

    public static String[] body() {
        HttpClient cl = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest rq = HttpRequest.newBuilder().uri(URI.create("https://mangachan.me/" + name_url)).build();
        HttpResponse<String> rsp = null;
        try {
            rsp = cl.send(rq, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rsp.body().split("\n");
    }

    public static ArrayList<String> urls(String[] select) {
        ArrayList<String> r = new ArrayList<>();
        for (String s :
                select) {
            if (s.contains("zaliv")) r.add(s);
        }
        amount = Math.max(0, Math.min(amount, r.size()));
        String[] selectItems = new String[amount];
        for (int i = 0; i < amount; i++) {
            selectItems[i] = r.get(r.size() - 1 - i);
        }
        ArrayList<String> urls = new ArrayList<>();
        for (String s :
                selectItems) {
            String splitLeft = "href=\'";
            int a = s.indexOf(splitLeft);
            if (a == -1) continue;
            else {
                s = s.substring(a + splitLeft.length());
                urls.add(s.substring(0, s.indexOf("\'")));
            }
        }
        return urls;
    }
}
