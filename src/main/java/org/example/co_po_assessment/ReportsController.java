package org.example.co_po_assessment;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {
    @FXML private ListView<ReportItem> reportsList;
    @FXML private Label statusLabel;

    private static final Path CO_DIR = Paths.get("co_reports");
    private static final Path PO_DIR = Paths.get("po_reports");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportsList.setOnMouseClicked(this::onListDoubleClick);
        loadReports();
    }

    @FXML
    public void onRefresh(ActionEvent e) { loadReports(); }

    @FXML
    public void onOpenSelected() {
        ReportItem item = reportsList.getSelectionModel().getSelectedItem();
        if (item == null) { setStatus("No report selected."); return; }
        openFile(item.file());
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) reportsList.getScene().getWindow();
        stage.close();
    }

    private void onListDoubleClick(MouseEvent evt) { if (evt.getClickCount() == 2) onOpenSelected(); }

    private void loadReports() {
        List<ReportItem> items = new ArrayList<>();
        items.addAll(scanDir(CO_DIR, "CO"));
        items.addAll(scanDir(PO_DIR, "PO"));
        items.sort(Comparator.comparing((ReportItem r) -> r.type()).thenComparing(ReportItem::name));
        reportsList.getItems().setAll(items);
        setStatus(items.isEmpty() ? "No reports found." : "Loaded " + items.size() + " reports.");
    }

    private List<ReportItem> scanDir(Path dir, String type) {
        List<ReportItem> list = new ArrayList<>();
        if (!Files.exists(dir)) return list;
        try {
            Files.list(dir)
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                .forEach(p -> {
                    try {
                        long size = Files.size(p);
                        Instant mod = Files.getLastModifiedTime(p).toInstant();
                        String meta = formatSize(size) + ", " + formatter.format(mod.atZone(ZoneId.systemDefault()));
                        list.add(new ReportItem(type, p.getFileName().toString(), p.toFile(), meta));
                    } catch (IOException ignored) { }
                });
        } catch (IOException e) { setStatus("Error reading " + dir + ": " + e.getMessage()); }
        return list;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0; if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0; return String.format("%.2f MB", mb);
    }

    private void openFile(File file) {
        if (file == null || !file.exists()) { setStatus("File missing."); return; }
        try {
            if (Desktop.isDesktopSupported()) { Desktop.getDesktop().open(file); setStatus("Opened " + file.getName()); }
            else setStatus("Desktop API not supported.");
        } catch (IOException ex) { setStatus("Failed to open: " + ex.getMessage()); }
    }

    private void setStatus(String msg) { Platform.runLater(() -> statusLabel.setText(msg)); }

    private record ReportItem(String type, String name, File file, String meta) {
        @Override public String toString() { return type + " - " + name + " (" + meta + ")"; }
    }
}

