import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    //    static String name_url = "9482-battle-angel-alita.html";
    static int amount = Integer.MAX_VALUE;
    static int threadCount = 10;
    static JTextArea log;
    static JProgressBar mainProgress;
    static JFrame jf;

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("PDFManga: Save manga");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setApproveButtonText("Select");
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
            }

            @Override
            public String getDescription() {
                return "PDF File";
            }
        });
        jf = new JFrame("PDFManga");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLocationRelativeTo(null);
        JPanel preMain = new JPanel();
        preMain.setLayout(new BoxLayout(preMain, BoxLayout.Y_AXIS));
        JPanel main = new JPanel();
        main.setLayout(new GridLayout(4, 1));
        JPanel urlPanel = new JPanel();
        urlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        urlPanel.add(new JLabel("URL"));
        JTextField url = new JTextField(19);
        urlPanel.add(url);
        /*JButton check = new JButton("Check");
        urlPanel.add(check);*/
        main.add(urlPanel);
        JPanel chapterPanel = new JPanel();
        chapterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        chapterPanel.add(new JLabel("Download from"));
        JTextField downFrom = new JTextField(3);
        chapterPanel.add(downFrom);
        chapterPanel.add(new JLabel("to"));
        JTextField downTo = new JTextField(3);
        chapterPanel.add(downTo);
        chapterPanel.add(new JLabel("chapter"));
        main.add(chapterPanel);
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(new JLabel("Output"));
        JTextField filePath = new JTextField(15);
        filePanel.add(filePath);
        JButton browseFile = new JButton("Browse");
        browseFile.addActionListener(e -> {
            if (fileChooser.showSaveDialog(jf) == JFileChooser.APPROVE_OPTION) {
                filePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        filePanel.add(browseFile);
        main.add(filePanel);
        JPanel startPanel = new JPanel();
        startPanel.setLayout(new GridLayout(1, 2));
        JButton cancel = new JButton("Cancel");
        cancel.setEnabled(false);
        cancel.addActionListener(e -> System.exit(0));
        JButton start = new JButton("Start");
        {
            JPanel jp1 = new JPanel();
            jp1.setLayout(new FlowLayout(FlowLayout.LEFT));
            jp1.add(cancel);
            startPanel.add(jp1);
            JPanel jp2 = new JPanel();
            jp2.setLayout(new FlowLayout(FlowLayout.RIGHT));
            jp2.add(start);
            startPanel.add(jp2);
        }
        main.add(startPanel);
        preMain.add(main);
        JPanel logPane = new JPanel();
        logPane.setLayout(new BoxLayout(logPane, BoxLayout.Y_AXIS));
        mainProgress = new JProgressBar(0, 4);
        mainProgress.setEnabled(false);
        logPane.add(mainProgress);
        JScrollPane scroll = new JScrollPane();
        log = new JTextArea(20, 30);
        scroll.getViewport().add(log);
        log.setEditable(false);
        log.setEnabled(false);
        mainProgress.setStringPainted(true);
        logPane.add(scroll);
        preMain.add(logPane);
        jf.add(preMain);
        jf.pack();
        jf.setVisible(true);
        jf.setResizable(false);
        start.addActionListener(e -> {
            cancel.setEnabled(true);
            mainProgress.setEnabled(true);
            log.setEnabled(true);
            start.setEnabled(false);
            new Thread(() -> begin(url.getText().substring(url.getText().lastIndexOf("/") + 1), filePath.getText())).start();
        });
    }

    public static void begin(String name_url, String save) {
        PDDocument doc = new PDDocument();
        String[] select = body(name_url);
        ArrayList<String> urls = urls(select);
        log.append(urls.size() + " chapters\n");
        ArrayList<String> imgURLs = imgURLs(urls);
        log.append(imgURLs.size() + " images\n");
        PDPage[] buffer = new PDPage[imgURLs.size()];
        int size = imgURLs.size() / threadCount;
        AtomicInteger count = new AtomicInteger();
        mainProgress.setMaximum(imgURLs.size());
        mainProgress.setString(count.get() * 100 / imgURLs.size() + "% " + count.get() + "/" + imgURLs.size());
        mainProgress.setValue(count.get());
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
                        log.append("Page " + (size * a + j) + " copied. " + (count.incrementAndGet()) + " of " + imgURLs.size() + "\n");
                        mainProgress.setString(count.get() * 100 / imgURLs.size() + "% " + count.get() + "/" + imgURLs.size() + " images");
                        mainProgress.setValue(count.get());
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
                    log.append("Page " + (size * a + j) + " copied. " + (count.incrementAndGet()) + " of " + imgURLs.size() + "\n");
                    mainProgress.setString(count.get() * 100 / imgURLs.size() + "% " + count.get() + "/" + imgURLs.size() + " images");
                    mainProgress.setValue(count.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (count.get() != imgURLs.size()) {
        }
        for (PDPage bi :
                buffer) {
            if (bi == null) log.append("Page wasn't downloaded\n");
        }
        log.append("Downloading completed\n");
        int a = 0;
        mainProgress.setMaximum(buffer.length);
        mainProgress.setValue(a);
        mainProgress.setString(a * 100 / buffer.length + "% " + a + "/" + buffer.length + " pages");
        for (PDPage bi :
                buffer) {
            doc.addPage(bi);
            log.append(++a + " page has been drew.\n");
            mainProgress.setValue(a);
            mainProgress.setString(a * 100 / buffer.length + "% " + a + "/" + buffer.length + " pages");
        }
        try {
            File f = new File(save);
            f.createNewFile();
            f.delete();
            f.createNewFile();
            doc.save(f);
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.append("Manga downloading completed!\n");
        mainProgress.setString("Manga downloading completed!");
        JOptionPane.showMessageDialog(jf,
                "Manga downloading completed!");
    }

    public static ArrayList<String> imgURLs(ArrayList<String> urls) {
        ArrayList<String> ret = new ArrayList<>();
        mainProgress.setMaximum(urls.size());
        int i = 0;
        mainProgress.setString(i * 100 / urls.size() + "% " + i + "/" + urls.size());
        mainProgress.setValue(i);
        for (String url :
                urls) {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://manga-chan.me" + url)).build();
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
            log.append("Page: " + url + " parsed\n");
            mainProgress.setValue(++i);
            mainProgress.setString(i * 100 / urls.size() + "% " + i + "/" + urls.size() + " chapters");
        }
        return ret;
    }

    public static String[] body(String name_url) {
        HttpClient cl = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest rq = HttpRequest.newBuilder().uri(URI.create("https://manga-chan.me/" + name_url)).build();
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
