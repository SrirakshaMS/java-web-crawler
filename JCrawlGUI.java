import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JCrawlGUI extends JFrame {
    private JTextField urlTextField;
    private JTextField directoryTextField;
    private JButton scrapeUrlsButton;
    private JButton downloadImagesButton;
    private JButton searchButton;
    private JTextField searchTextField;
    private JTextArea outputTextArea;

    public JCrawlGUI() {
        setTitle("JCrawl");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        urlTextField = new JTextField(40);
        directoryTextField = new JTextField(40);
        scrapeUrlsButton = new JButton("Scrape URLs");
        downloadImagesButton = new JButton("Download Images");
        searchTextField = new JTextField(40);
        searchButton = new JButton("Search");
        outputTextArea = new JTextArea(15, 50);

        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        outputTextArea.setEditable(false);

        mainPanel.add(new JLabel("URL:"));
        mainPanel.add(urlTextField);
        mainPanel.add(new JLabel("Directory:"));
        mainPanel.add(directoryTextField);
        mainPanel.add(scrapeUrlsButton);
        mainPanel.add(downloadImagesButton);
        mainPanel.add(new JLabel("Search:"));
        mainPanel.add(searchTextField);
        mainPanel.add(searchButton);
        mainPanel.add(scrollPane);

        scrapeUrlsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = urlTextField.getText();
                if (!url.isEmpty()) {
                    int numUrls = 5; // Default value
                    try {
                        numUrls = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of URLs to scrape:"));
                    } catch (NumberFormatException ex) {
                        outputTextArea.append("Invalid number. Using default value (5).\n");
                    }
                    outputTextArea.setText("Scraping URLs...\n");
                    Set<String> urls = scrapeUrls(url, numUrls);
                    outputTextArea.append("Scraped URLs:\n");
                    for (String u : urls) {
                        outputTextArea.append(u + "\n");
                    }
                } else {
                    outputTextArea.setText("Please enter a valid URL.\n");
                }
            }
        });

        downloadImagesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = urlTextField.getText();
                String directory = directoryTextField.getText();
                if (!url.isEmpty() && !directory.isEmpty()) {
                    outputTextArea.setText("Downloading images...\n");
                    try {
                        // Connect to the website and get the HTML document
                        Document doc = Jsoup.connect(url).get();

                        // Select all image elements from the HTML document
                        Elements images = doc.select("img");

                        // Create the directory to save images if it does not exist
                        File dir = new File(directory);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        // Download and save each image
                        for (Element image : images) {
                            String imageUrl = image.absUrl("src"); // Get the absolute URL of the image
                            if (!imageUrl.isEmpty()) {
                                downloadImage(imageUrl, directory);
                            }
                        }

                        outputTextArea.append("Images downloaded successfully.");
                    } catch (IOException ex) {
                        outputTextArea.append("Error downloading images: " + ex.getMessage() + "\n");
                    }
                } else {
                    outputTextArea.setText("Please enter both URL and directory.\n");
                }
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchTextField.getText();
                if (!searchTerm.isEmpty()) {
                    Set<String> matchingFiles = search(searchTerm);
                    outputTextArea.setText("Matching files:\n");
                    for (String file : matchingFiles) {
                        outputTextArea.append(file + "\n");
                    }
                } else {
                    outputTextArea.setText("Please enter a search term.\n");
                }
            }
        });

        add(mainPanel);
        setVisible(true);
    }

    static Set<String> scrapeUrls(String url, int numUrls) {
        Set<String> urls = new HashSet<>();
        try {
            // Connect to the website and get the HTML document
            Document doc = Jsoup.connect(url).get();

            // Select all anchor elements from the HTML document
            Elements links = doc.select("a[href]");

            // Extract and store the URLs up to the specified number
            int count = 0;
            for (Element link : links) {
                String absUrl = link.absUrl("href");
                if (!absUrl.isEmpty() && isValidUrl(absUrl)) {
                    urls.add(absUrl);
                    count++;
                    if (count >= numUrls) {
                        break;
                    }
                }
            }

            // Save content of each URL to a text file
            saveUrlsContent(urls);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urls;
    }

    static boolean isValidUrl(String url) {
        // Add custom URL validation logic here
        // For example, check if the URL starts with "http" or "https"
        return url.startsWith("http://") || url.startsWith("https://");
    }

    static void saveUrlsContent(Set<String> urls) {
        int i = 1;
        for (String url : urls) {
            try {
                Document doc = Jsoup.connect(url).get();
                String content = extractMainContent(doc);
                String fileName = i + ".txt";
                FileWriter writer = new FileWriter(fileName);
                writer.write(content);
                writer.close();
                System.out.println("Content of URL " + url + " saved to " + fileName);
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String extractMainContent(Document doc) {
        // Select the main content element based on your website's structure
        // For example, if your main content is within a <div> with id="main-content"
        Element mainContent = doc.selectFirst("#main-content");
        if (mainContent != null) {
            return mainContent.text();
        } else {
            // If main content is not found, return the entire text of the document
            return doc.text();
        }
    }

    static void downloadImage(String imageUrl, String saveDirectory) {
        try {
            // Open a connection to the image URL
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();

            // Get the image name from the URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // Create an output stream to save the image
            InputStream inputStream = connection.getInputStream();
            OutputStream outputStream = new FileOutputStream(saveDirectory + "/" + fileName);

            // Read from the input stream and write to the output stream
            byte[] buffer = new byte[2048];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // Close streams
            inputStream.close();
            outputStream.close();

            System.out.println("Downloaded: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Set<String> search(String searchTerm) {
        Set<String> matchingFiles = new HashSet<>();
        File directory = new File(".");
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
        for (File file : files) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(searchTerm)) {
                        matchingFiles.add(file.getName());
                        break;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return matchingFiles;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JCrawlGUI();
            }
        });
    }
}
