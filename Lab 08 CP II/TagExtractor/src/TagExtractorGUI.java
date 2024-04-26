import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class TagExtractorGUI extends JFrame {
    private JTextArea textArea;
    private JButton selectFileButton;
    private JButton selectStopWordsButton;
    private JButton extractTagsButton;
    private JButton saveTagsButton;
    private File selectedFile;
    private Set<String> stopWords;
    private Map<String, Integer> tagFrequencies;

    public TagExtractorGUI() {
        setTitle("Tag Extractor");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        selectFileButton = new JButton("Select Text File");
        selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectTextFile();
            }
        });
        buttonPanel.add(selectFileButton);

        selectStopWordsButton = new JButton("Select Stop Words File");
        selectStopWordsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectStopWordsFile();
            }
        });
        buttonPanel.add(selectStopWordsButton);

        extractTagsButton = new JButton("Extract Tags");
        extractTagsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                extractTags();
            }
        });
        buttonPanel.add(extractTagsButton);

        saveTagsButton = new JButton("Save Tags");
        saveTagsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveTags();
            }
        });
        buttonPanel.add(saveTagsButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void selectTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            textArea.setText("Selected Text File: " + selectedFile.getName());
        }
    }

    private void selectStopWordsFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File stopWordsFile = fileChooser.getSelectedFile();
            stopWords = loadStopWords(stopWordsFile);
            textArea.append("\nSelected Stop Words File: " + stopWordsFile.getName());
        }
    }

    private Set<String> loadStopWords(File stopWordsFile) {
        Set<String> stopWords = new HashSet<>();
        try (Scanner scanner = new Scanner(stopWordsFile)) {
            while (scanner.hasNextLine()) {
                stopWords.add(scanner.nextLine().toLowerCase());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stopWords;
    }

    private void extractTags() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a text file first.");
            return;
        }
        if (stopWords == null || stopWords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a stop words file first.");
            return;
        }
        tagFrequencies = new HashMap<>();
        try (Scanner scanner = new Scanner(selectedFile)) {
            while (scanner.hasNext()) {
                String word = scanner.next().replaceAll("[^a-zA-Z]", "").toLowerCase();
                if (!stopWords.contains(word)) {
                    tagFrequencies.put(word, tagFrequencies.getOrDefault(word, 0) + 1);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        displayTags();
    }

    private void displayTags() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        textArea.setText(sb.toString());
    }

    private void saveTags() {
        if (tagFrequencies == null || tagFrequencies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tags extracted to save.");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            // Ensure the file has a .txt extension
            if (!outputFile.getName().toLowerCase().endsWith(".txt")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".txt");
            }
            try (PrintWriter writer = new PrintWriter(outputFile)) {
                for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue());
                }
                writer.flush();
                JOptionPane.showMessageDialog(this, "Tags saved successfully.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TagExtractorGUI();
            }
        });
    }
}
