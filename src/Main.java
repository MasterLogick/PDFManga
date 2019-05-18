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

public class Main {
    public static void main(String[] args) {
        PDDocument doc = new PDDocument();
        String[] urls = new String[]{
                "/online/473888-kenja-no-mago_v13_ch57.html",
                "/online/473887-kenja-no-mago_v13_ch56.html",
                "/online/465148-kenja-no-mago_v13_ch55.html",
                "/online/465147-kenja-no-mago_v13_ch54.html",
                "/online/465146-kenja-no-mago_v13_ch53.html",
                "/online/465145-kenja-no-mago_v13_ch52.html",
                "/online/441765-kenja-no-mago_v13_ch51.html",
                "/online/439597-kenja-no-mago_v13_ch50.html",
                "/online/434938-kenja-no-mago_v13_ch49.html",
                "/online/434527-kenja-no-mago_v13_ch48.html",
                "/online/426993-kenja-no-mago_v13_ch47.5.html",
                "/online/417794-kenja-no-mago_v13_ch47.html",
                "/online/417793-kenja-no-mago_v13_ch46.html",
                "/online/415276-kenja-no-mago_v13_ch45.html",
                "/online/414026-kenja-no-mago_v13_ch44.html",
                "/online/411961-kenja-no-mago_v13_ch43.html",
                "/online/411960-kenja-no-mago_v13_ch42.html",
                "/online/403472-kenja-no-mago_v13_ch41.html",
                "/online/400703-kenja-no-mago_v13_ch40.html",
                "/online/400481-kenja-no-mago_v13_ch39.html",
                "/online/390034-kenja-no-mago_v13_ch38.html",
                "/online/389804-kenja-no-mago_v13_ch37.html",
                "/online/389651-kenja-no-mago_v13_ch36.html",
                "/online/380788-kenja-no-mago_v13_ch35.html",
                "/online/380787-kenja-no-mago_v13_ch34.html",
                "/online/380786-kenja-no-mago_v13_ch33.html",
                "/online/380785-kenja-no-mago_v13_ch32.html",
                "/online/380784-kenja-no-mago_v13_ch31.html",
                "/online/337022-kenja-no-mago_v13_ch30.html",
                "/online/337021-kenja-no-mago_v13_ch29.html",
                "/online/337020-kenja-no-mago_v13_ch28.html",
                "/online/349578-kenja-no-mago_v13_ch27.html",
                "/online/339257-kenja-no-mago_v13_ch26.html",
                "/online/316951-kenja-no-mago_v13_ch25.html",
                "/online/316950-kenja-no-mago_v12_ch24.html",
                "/online/316949-kenja-no-mago_v12_ch23.html",
                "/online/313905-magis-grandson_v11_ch22.html",
                "/online/313904-magis-grandson_v11_ch21.html",
                "/online/310149-magis-grandson_v10_ch20.html",
                "/online/310148-magis-grandson_v10_ch19.html",
                "/online/305376-kenja-no-mago_v9_ch18.6.html",
                "/online/305375-kenja-no-mago_v9_ch18.5.html",
                "/online/305374-kenja-no-mago_v9_ch18.html",
                "/online/305373-kenja-no-mago_v9_ch17.html",
                "/online/305372-kenja-no-mago_v8_ch16.html",
                "/online/304094-kenja-no-mago_v8_ch15.html",
                "/online/301909-kenja-no-mago_v7_ch14.html",
                "/online/301908-kenja-no-mago_v7_ch13.html",
                "/online/299982-kenja-no-mago_v6_ch12.html",
                "/online/299197-kenja-no-mago_v6_ch11.html",
                "/online/296332-kenja-no-mago_v5_ch10.html",
                "/online/296331-kenja-no-mago_v5_ch9.html",
                "/online/296330-kenja-no-mago_v4_ch8.html",
                "/online/296329-kenja-no-mago_v4_ch7.html",
                "/online/290762-kenja-no-mago_v3_ch6.html",
                "/online/289586-kenja-no-mago_v3_ch5.html",
                "/online/288144-kenja-no-mago_v2_ch4.html",
                "/online/284119-kenja-no-mago_v2_ch3.html",
                "/online/281706-magis-grandson_v1_ch2.html",
                "/online/271734-magis-grandson_v1_ch1.html",
                "/online/270128-kenja-no-mago_v1_ch0.html"
                };
String[] arr = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            arr[i]=urls[urls.length-1-i];
        }
        urls=arr;
        for (String url :
                urls) {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://mangachan.me"+url)).build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String sub = response.body().substring(response.body().indexOf("\"fullimg\":[\"")+"\"fullimg\":[\"".length());
            sub=sub.substring(0,sub.indexOf("\",]"));
            String[] uris = sub.split("\",\"");
            for (String uri :
                    uris) {
                try {
                    BufferedImage bi = null;
                    bi = ImageIO.read(new URL(uri));
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
                    System.out.println("Copied img: "+uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }























        /*try {
            BufferedImage bi = null;
            bi = ImageIO.read(new URL("http://t9.mangas.rocks/auto/21/53/83/013.png_res.jpg"));
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
        */try {
            File f = new File("/home/user/lollol.pdf");
            f.createNewFile();
            f.delete();
            f.createNewFile();
            doc.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
