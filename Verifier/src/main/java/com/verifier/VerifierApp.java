package com.verifier;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Verifier — pick a folder and browse its tree. Every file node expands (+/-)
 * to reveal its checksums (CRC32, Adler-32) and hashes (MD5, SHA-1, SHA-256),
 * computed lazily on first expand by a {@link DigestService} background pool
 * and cached afterwards. Expand All / Collapse All act on the whole tree.
 */
public class VerifierApp extends Application {

    private final DigestService digests = new DigestService();

    private final Label pathLabel = new Label("No folder selected");
    private final Button chooseBtn = new Button("Choose Folder…");
    private final Button expandAllBtn = new Button("Expand All");
    private final Button collapseAllBtn = new Button("Collapse All");
    private final TreeView<String> tree = new TreeView<>();
    private final Label statusLabel = new Label("Pick a folder to browse its checksums and hashes.");

    private Path selectedFolder;

    @Override
    public void start(Stage stage) {
        Label title = new Label("Verifier");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Browse a folder and inspect per-file checksums (CRC32, Adler-32) "
                + "and hashes (MD5, SHA-1, SHA-256). Expand a file to compute them.");
        subtitle.getStyleClass().add("subtitle");
        subtitle.setWrapText(true);

        chooseBtn.getStyleClass().add("primary");
        chooseBtn.setOnAction(e -> chooseFolder(stage));

        pathLabel.getStyleClass().add("path");
        pathLabel.setWrapText(true);
        pathLabel.setMaxWidth(Double.MAX_VALUE);

        expandAllBtn.setDisable(true);
        expandAllBtn.setOnAction(e -> setExpandedRecursively(tree.getRoot(), true));
        collapseAllBtn.setDisable(true);
        collapseAllBtn.setOnAction(e -> collapseAll());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox controls = new HBox(10, chooseBtn, spacer, expandAllBtn, collapseAllBtn);

        tree.setShowRoot(true);
        tree.setCellFactory(view -> new DigestTreeCell());
        VBox.setVgrow(tree, Priority.ALWAYS);

        statusLabel.getStyleClass().add("status");
        statusLabel.setWrapText(true);
        digests.pendingProperty().addListener((obs, was, now) -> {
            if (now.intValue() > 0) {
                statusLabel.setText("Hashing " + now.intValue() + " file(s)…");
            } else if (selectedFolder != null) {
                statusLabel.setText("Idle — digests are cached once computed.");
            }
        });

        VBox root = new VBox(14,
                title,
                subtitle,
                controls,
                pathLabel,
                tree,
                statusLabel);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 720, 640);
        var css = getClass().getResource("/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Verifier");
        stage.setScene(scene);
        stage.setMinWidth(560);
        stage.setMinHeight(480);
        stage.show();
    }

    @Override
    public void stop() {
        digests.shutdown();
    }

    private void chooseFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a folder to verify");
        if (selectedFolder != null && Files.isDirectory(selectedFolder)) {
            chooser.setInitialDirectory(selectedFolder.toFile());
        }
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            selectedFolder = dir.toPath();
            pathLabel.setText(selectedFolder.toString());
            loadTree(selectedFolder);
        }
    }

    /** Scans the folder on a background task, then swaps the tree root in. */
    private void loadTree(Path folder) {
        chooseBtn.setDisable(true);
        expandAllBtn.setDisable(true);
        collapseAllBtn.setDisable(true);
        statusLabel.setText("Scanning " + folder.getFileName() + "…");

        Task<TreeItem<String>> scan = new Task<>() {
            @Override
            protected TreeItem<String> call() throws IOException {
                return buildDirItem(folder);
            }
        };
        scan.setOnSucceeded(e -> {
            TreeItem<String> root = scan.getValue();
            root.setExpanded(true);
            tree.setRoot(root);
            chooseBtn.setDisable(false);
            expandAllBtn.setDisable(false);
            collapseAllBtn.setDisable(false);
            statusLabel.setText("Expand a file (+) to compute its digests, or use Expand All.");
        });
        scan.setOnFailed(e -> {
            chooseBtn.setDisable(false);
            Throwable ex = scan.getException();
            statusLabel.setText("Failed to scan: " + (ex == null ? "unknown error" : ex.getMessage()));
        });
        Thread t = new Thread(scan, "folder-scan");
        t.setDaemon(true);
        t.start();
    }

    /** Recursively builds the item tree: directories first, then files, sorted by name. */
    private TreeItem<String> buildDirItem(Path dir) throws IOException {
        DirItem item = new DirItem(dir);
        List<Path> children = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            stream.forEach(children::add);
        }
        children.sort(Comparator
                .comparing((Path p) -> !Files.isDirectory(p))
                .thenComparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER));
        for (Path child : children) {
            if (Files.isDirectory(child)) {
                item.getChildren().add(buildDirItem(child));
            } else if (Files.isRegularFile(child)) {
                item.getChildren().add(new FileItem(child));
            }
        }
        return item;
    }

    private void setExpandedRecursively(TreeItem<String> item, boolean expanded) {
        if (item == null) {
            return;
        }
        item.setExpanded(expanded);
        for (TreeItem<String> child : List.copyOf(item.getChildren())) {
            if (!child.isLeaf() || !child.getChildren().isEmpty()) {
                setExpandedRecursively(child, expanded);
            }
        }
    }

    /** Collapse everything but keep the chosen folder itself open. */
    private void collapseAll() {
        setExpandedRecursively(tree.getRoot(), false);
        if (tree.getRoot() != null) {
            tree.getRoot().setExpanded(true);
        }
    }

    public static void main(String[] args) {
        Platform.setImplicitExit(true);
        launch(args);
    }

    /** Directory node — plain container, styled by the cell factory. */
    private static final class DirItem extends TreeItem<String> {
        DirItem(Path dir) {
            super(dir.getFileName() == null ? dir.toString() : dir.getFileName().toString());
        }
    }

    /**
     * File node. Expanding it (+) the first time submits the file to the
     * digest pool; the placeholder child is then replaced with one row per
     * algorithm. Results stay attached (and cached), so collapsing (-) and
     * re-expanding is free.
     */
    private final class FileItem extends TreeItem<String> {
        private final Path file;
        private boolean requested;

        FileItem(Path file) {
            super(file.getFileName().toString());
            this.file = file;
            getChildren().add(new TreeItem<>("computing…"));
            expandedProperty().addListener((obs, was, expanded) -> {
                if (expanded) {
                    computeDigests();
                }
            });
        }

        private void computeDigests() {
            if (requested) {
                return;
            }
            requested = true;
            digests.compute(file,
                    this::showDigests,
                    error -> {
                        requested = false; // allow a retry on next expand
                        getChildren().setAll(new TreeItem<>("error: " + error));
                    });
        }

        private void showDigests(Map<String, String> result) {
            List<TreeItem<String>> rows = new ArrayList<>(result.size());
            for (Map.Entry<String, String> entry : result.entrySet()) {
                rows.add(new AlgoItem(entry.getKey(), entry.getValue()));
            }
            getChildren().setAll(rows);
        }
    }

    /** One "ALGORITHM  value" row under a file. */
    private static final class AlgoItem extends TreeItem<String> {
        final String value;

        AlgoItem(String algorithm, String value) {
            super(String.format("%-9s %s", algorithm, value));
            this.value = value;
        }
    }

    /** Styles rows by node kind and offers "Copy value" on digest rows. */
    private final class DigestTreeCell extends TreeCell<String> {
        @Override
        protected void updateItem(String text, boolean empty) {
            super.updateItem(text, empty);
            getStyleClass().removeAll("dir-cell", "file-cell", "algo-cell");
            setContextMenu(null);
            if (empty || text == null) {
                setText(null);
                return;
            }
            TreeItem<String> item = getTreeItem();
            if (item instanceof DirItem) {
                setText("📁 " + text);
                getStyleClass().add("dir-cell");
            } else if (item instanceof FileItem) {
                setText("📄 " + text);
                getStyleClass().add("file-cell");
            } else {
                setText(text);
                getStyleClass().add("algo-cell");
                if (item instanceof AlgoItem algo) {
                    MenuItem copy = new MenuItem("Copy value");
                    copy.setOnAction(e -> {
                        ClipboardContent content = new ClipboardContent();
                        content.putString(algo.value);
                        Clipboard.getSystemClipboard().setContent(content);
                    });
                    setContextMenu(new ContextMenu(copy));
                }
            }
        }
    }
}
