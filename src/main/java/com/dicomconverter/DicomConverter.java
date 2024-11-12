package com.dicomconverter;

import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.border.*;


import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;

import javax.imageio.stream.ImageInputStream;
import java.io.File;

public class DicomConverter extends JFrame {
    private List<File> selectedFiles = new ArrayList<>();
    private JList<String> fileList;
    private JRadioButton pdfOption;
    private JRadioButton jpgOption;
    private JCheckBox combineIntoOne;
    private DefaultListModel<String> listModel;
    private static final String FOOTER_TEXT = "Software developed by Ippokratis Kozanis IT team";
    private static final Color FOOTER_COLOR = new Color(70, 70, 70);
    private static final Font FOOTER_FONT = new Font("Arial", Font.ITALIC, 12);

    public DicomConverter() {
        super("Ippokratis Kozanis - DICOM Converter");
        setupUI();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(0, 0));
    setSize(800, 600);

    // Προσθήκη του header με το λογότυπο
    JPanel headerPanel = createHeaderPanel();
    add(headerPanel, BorderLayout.NORTH);

    // Κύριο panel
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Λίστα αρχείων
    listModel = new DefaultListModel<>();
    fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Επιλεγμένα αρχεία",
    TitledBorder.LEFT,
    TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12)
        ));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

    // Panel επιλογών
    JPanel optionsPanel = createOptionsPanel();

    // Panel κουμπιών
    JPanel buttonPanel = createButtonPanel();

    // Συνδυασμός options και buttons
    JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(optionsPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

    add(mainPanel, BorderLayout.CENTER);

    // Footer
    JPanel footerPanel = createFooterPanel();
    add(footerPanel, BorderLayout.SOUTH);

    setLocationRelativeTo(null);
}

private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(Color.WHITE);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    try {
        // Φόρτωση του λογότυπου
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/logo_new.png"));
        // Προσαρμογή μεγέθους εικόνας
        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImg));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(logoLabel, BorderLayout.CENTER);
    } catch (Exception e) {
        // Αν δεν βρεθεί το λογότυπο, χρησιμοποιούμε text
        JLabel titleLabel = new JLabel("DICOM Converter - Ippokratis Kozanis");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
    }

    return headerPanel;
}

private JPanel createOptionsPanel() {
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
    optionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Επιλογές μετατροπής",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12)
    ));

    // Επιλογές μορφής εξόδου
    ButtonGroup formatGroup = new ButtonGroup();
    pdfOption = new JRadioButton("Μετατροπή σε PDF", true);
    jpgOption = new JRadioButton("Μετατροπή σε JPG", false);

    // Styling για τα radio buttons
    pdfOption.setFont(new Font("Arial", Font.PLAIN, 12));
    jpgOption.setFont(new Font("Arial", Font.PLAIN, 12));

    formatGroup.add(pdfOption);
    formatGroup.add(jpgOption);

    // Επιλογή συνδυασμού αρχείων
    combineIntoOne = new JCheckBox("Συνδυασμός σε ένα αρχείο", true);
    combineIntoOne.setFont(new Font("Arial", Font.PLAIN, 12));

    pdfOption.addActionListener(e -> combineIntoOne.setEnabled(true));
    jpgOption.addActionListener(e -> {
        combineIntoOne.setSelected(false);
        combineIntoOne.setEnabled(false);
    });

    // Προσθήκη padding
    optionsPanel.add(Box.createVerticalStrut(5));
    optionsPanel.add(pdfOption);
    optionsPanel.add(Box.createVerticalStrut(5));
    optionsPanel.add(jpgOption);
    optionsPanel.add(Box.createVerticalStrut(10));
    optionsPanel.add(combineIntoOne);
    optionsPanel.add(Box.createVerticalStrut(5));

    return optionsPanel;
}

private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

    JButton addButton = createStyledButton("Προσθήκη αρχείων");
    JButton removeButton = createStyledButton("Αφαίρεση επιλεγμένων");
    JButton convertButton = createStyledButton("Μετατροπή");

    // Προσθήκη ειδικού styling για το κουμπί μετατροπής
    convertButton.setBackground(new Color(0, 120, 212));
    convertButton.setForeground(Color.WHITE);

    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);
    buttonPanel.add(convertButton);

    // Event Listeners
    addButton.addActionListener(e -> addFiles());
    removeButton.addActionListener(e -> removeSelectedFiles());
    convertButton.addActionListener(e -> convertFiles());

    return buttonPanel;
}

private JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", Font.PLAIN, 12));
    button.setFocusPainted(false);
    button.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
    ));
    return button;
}

private JPanel createFooterPanel() {
    JPanel footerPanel = new JPanel(new BorderLayout());
    footerPanel.setBackground(new Color(245, 245, 245));
    footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    JLabel footerLabel = new JLabel(FOOTER_TEXT);
    footerLabel.setFont(FOOTER_FONT);
    footerLabel.setForeground(FOOTER_COLOR);
    footerLabel.setHorizontalAlignment(SwingConstants.CENTER);

    footerPanel.add(footerLabel, BorderLayout.CENTER);

    return footerPanel;
}

    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".dcm");
            }
            public String getDescription() {
                return "DICOM Files (*.dcm)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                    listModel.addElement(file.getName());
                }
            }
        }
    }

    private void removeSelectedFiles() {
        int[] selectedIndices = fileList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            selectedFiles.remove(selectedIndices[i]);
            listModel.remove(selectedIndices[i]);
        }
    }

    private void convertFiles() {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Παρακαλώ επιλέξτε τουλάχιστον ένα αρχείο DICOM.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Επιλέξτε φάκελο αποθήκευσης");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputDir = fileChooser.getSelectedFile();

            try {
                if (pdfOption.isSelected()) {
                    if (combineIntoOne.isSelected()) {
                        convertToCombinedPdf(selectedFiles, outputDir);
                    } else {
                        convertToSeparatePdfs(selectedFiles, outputDir);
                    }
                } else {
                    convertToJpg(selectedFiles, outputDir);
                }

                JOptionPane.showMessageDialog(this,
                        "Η μετατροπή ολοκληρώθηκε επιτυχώς!");

                // Άνοιγμα του φακέλου με τα αποτελέσματα
                Desktop.getDesktop().open(outputDir);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Σφάλμα κατά τη μετατροπή: " + e.getMessage(),
                        "Σφάλμα",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void convertToCombinedPdf(List<File> files, File outputDir) throws IOException {
        File outputFile = new File(outputDir, "combined_output.pdf");
        try (PDDocument document = new PDDocument()) {
            for (File file : files) {
                try {
                    BufferedImage image = loadDicomImage(file);
                    if (image != null) {
                        PDPage page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                                document,
                                imageToByteArray(image, "png"),
                                file.getName()
                        );

                        try (PDPageContentStream contentStream =
                                     new PDPageContentStream(document, page)) {
                            float scale = Math.min(
                                    page.getMediaBox().getWidth() / pdImage.getWidth(),
                                    page.getMediaBox().getHeight() / pdImage.getHeight()
                            );

                            float width = pdImage.getWidth() * scale;
                            float height = pdImage.getHeight() * scale;
                            float x = (page.getMediaBox().getWidth() - width) / 2;
                            float y = (page.getMediaBox().getHeight() - height) / 2;

                            contentStream.drawImage(pdImage, x, y, width, height);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            document.save(outputFile);
        }
    }

    private void convertToSeparatePdfs(List<File> files, File outputDir) throws IOException {
        for (File file : files) {
            String outputName = file.getName().replaceFirst("[.][^.]+$", "") + ".pdf";
            File outputFile = new File(outputDir, outputName);

            try (PDDocument document = new PDDocument()) {
                BufferedImage image = loadDicomImage(file);
                if (image != null) {
                    PDPage page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                            document,
                            imageToByteArray(image, "png"),
                            file.getName()
                    );

                    try (PDPageContentStream contentStream =
                                 new PDPageContentStream(document, page)) {
                        float scale = Math.min(
                                page.getMediaBox().getWidth() / pdImage.getWidth(),
                                page.getMediaBox().getHeight() / pdImage.getHeight()
                        );

                        float width = pdImage.getWidth() * scale;
                        float height = pdImage.getHeight() * scale;
                        float x = (page.getMediaBox().getWidth() - width) / 2;
                        float y = (page.getMediaBox().getHeight() - height) / 2;

                        contentStream.drawImage(pdImage, x, y, width, height);
                    }
                }
                document.save(outputFile);
            }
        }
    }

    private void convertToJpg(List<File> files, File outputDir) throws IOException {
        for (File file : files) {
            BufferedImage image = loadDicomImage(file);
            if (image != null) {
                String outputName = file.getName().replaceFirst("[.][^.]+$", "") + ".jpg";
                File outputFile = new File(outputDir, outputName);
                ImageIO.write(image, "jpg", outputFile);
            }
        }
    }

    private BufferedImage loadDicomImage(File file) throws IOException {
        try {
            // Δημιουργία ImageReader για DICOM
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
            DicomImageReader reader;

            if (!readers.hasNext()) {
                // Αν δεν βρέθηκε DICOM reader, δημιούργησε έναν
                reader = new DicomImageReader(new DicomImageReaderSpi());
            } else {
                reader = (DicomImageReader) readers.next();
            }

            // Άνοιγμα του αρχείου DICOM
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis);

                // Δημιουργία παραμέτρων ανάγνωσης
                DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();

                // Ανάγνωση της εικόνας
                BufferedImage bi = reader.read(0, param);

                // Αν η εικόνα είναι grayscale, μετατροπή σε RGB
                if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY ||
                        bi.getType() == BufferedImage.TYPE_USHORT_GRAY) {
                    BufferedImage rgbImage = new BufferedImage(
                            bi.getWidth(),
                            bi.getHeight(),
                            BufferedImage.TYPE_INT_RGB
                    );

                    Graphics2D g = rgbImage.createGraphics();
                    g.drawImage(bi, 0, 0, null);
                    g.dispose();

                    return rgbImage;
                }

                return bi;
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            throw new IOException("Σφάλμα κατά την ανάγνωση του DICOM αρχείου: " + e.getMessage(), e);
        }
    }

    private byte[] imageToByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new DicomConverter().setVisible(true);
        });
    }
}