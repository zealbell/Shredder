package com.shredder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Shredder — pick a folder, then irreversibly overwrite every file under it with
 * zero bytes and delete it. The heavy lifting runs on a {@link ShredService}
 * background task so the window stays responsive.
 */
public class ShredderApp extends Application {

    private final Label pathLabel = new Label("No folder selected");
    private final Button chooseBtn = new Button("Choose Folder…");
    private final Button shredBtn = new Button("Shred");
    private final Button cancelBtn = new Button("Stop");
    private final ProgressBar progress = new ProgressBar(0);
    private final Label statusLabel = new Label("Pick a folder to begin.");

    private final Label filesValue = new Label("0");
    private final Label foldersValue = new Label("0");
    private final Label bytesValue = new Label("0 B");
    private final Label errorsValue = new Label("0");
    private final Label elapsedValue = new Label("—");

    private Path selectedFolder;
    private ShredService currentJob;

    @Override
    public void start(Stage stage) {
        Label title = new Label("Shredder");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Overwrite files with zeros, then delete them. This cannot be undone.");
        subtitle.getStyleClass().add("subtitle");
        subtitle.setWrapText(true);

        chooseBtn.getStyleClass().add("primary");
        chooseBtn.setMaxWidth(Double.MAX_VALUE);
        chooseBtn.setOnAction(e -> chooseFolder(stage));

        pathLabel.getStyleClass().add("path");
        pathLabel.setWrapText(true);

        shredBtn.getStyleClass().add("danger");
        shredBtn.setDisable(true);
        shredBtn.setMaxWidth(Double.MAX_VALUE);
        shredBtn.setOnAction(e -> confirmAndShred());

        cancelBtn.setDisable(true);
        cancelBtn.setOnAction(e -> {
            if (currentJob != null) {
                currentJob.cancel();
            }
        });

        HBox actions = new HBox(10, shredBtn, cancelBtn);
        HBox.setHgrow(shredBtn, Priority.ALWAYS);

        progress.setMaxWidth(Double.MAX_VALUE);
        statusLabel.getStyleClass().add("status");
        statusLabel.setWrapText(true);

        VBox root = new VBox(14,
                title,
                subtitle,
                chooseBtn,
                pathLabel,
                new Separator(),
                actions,
                progress,
                statusLabel,
                buildStatsCard());
        root.setPadding(new Insets(24));
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 460, 560);
        var css = getClass().getResource("/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Shredder");
        stage.setScene(scene);
        stage.setMinWidth(420);
        stage.setMinHeight(520);
        stage.show();
    }

    private GridPane buildStatsCard() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("stats");
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        addStatRow(grid, 0, "Files shredded", filesValue);
        addStatRow(grid, 1, "Folders deleted", foldersValue);
        addStatRow(grid, 2, "Data overwritten", bytesValue);
        addStatRow(grid, 3, "Errors", errorsValue);
        addStatRow(grid, 4, "Time", elapsedValue);
        return grid;
    }

    private void addStatRow(GridPane grid, int row, String name, Label value) {
        Label key = new Label(name);
        key.getStyleClass().add("stat-key");
        value.getStyleClass().add("stat-value");
        value.setMaxWidth(Double.MAX_VALUE);
        value.setAlignment(Pos.CENTER_RIGHT);
        grid.add(key, 0, row);
        grid.add(value, 1, row);
        GridPane.setHgrow(value, Priority.ALWAYS);
    }

    private void chooseFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a folder to shred");
        if (selectedFolder != null && Files.isDirectory(selectedFolder)) {
            chooser.setInitialDirectory(selectedFolder.toFile());
        }
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            selectedFolder = dir.toPath();
            pathLabel.setText(selectedFolder.toString());
            shredBtn.setDisable(false);
            statusLabel.setText("Ready. Click Shred to destroy everything under this folder.");
        }
    }

    private void confirmAndShred() {
        if (selectedFolder == null) {
            return;
        }
        if (isProtected(selectedFolder)) {
            error("Refusing to shred a protected location:\n" + selectedFolder
                    + "\n\nPick a specific sub-folder instead.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Confirm shred");
        confirm.setHeaderText("Permanently destroy everything in this folder?");
        confirm.setContentText(selectedFolder + "\n\nEvery file will be overwritten with zeros "
                + "and deleted, then the folders removed. This cannot be undone.");
        confirm.getButtonTypes().setAll(ButtonType.CANCEL, new ButtonType("Shred"));

        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get().getButtonData().isCancelButton()) {
            return;
        }
        runShred();
    }

    private void runShred() {
        resetStats();
        currentJob = new ShredService(selectedFolder);

        progress.progressProperty().bind(currentJob.progressProperty());
        statusLabel.textProperty().bind(currentJob.messageProperty());

        setRunningUi(true);

        currentJob.setOnSucceeded(e -> {
            unbindRunningState();
            ShredStats stats = currentJob.getValue();
            showStats(stats);
            setRunningUi(false);
            statusLabel.setText(summaryLine(stats));
        });
        currentJob.setOnCancelled(e -> {
            unbindRunningState();
            showStats(currentJob.getValue());
            setRunningUi(false);
            statusLabel.setText("Stopped before finishing.");
        });
        currentJob.setOnFailed(e -> {
            unbindRunningState();
            setRunningUi(false);
            Throwable ex = currentJob.getException();
            statusLabel.setText("Failed: " + (ex == null ? "unknown error" : ex.getMessage()));
        });

        Thread t = new Thread(currentJob, "shred-worker");
        t.setDaemon(true);
        t.start();
    }

    private void unbindRunningState() {
        progress.progressProperty().unbind();
        statusLabel.textProperty().unbind();
    }

    private void setRunningUi(boolean running) {
        chooseBtn.setDisable(running);
        shredBtn.setDisable(running || selectedFolder == null);
        cancelBtn.setDisable(!running);
        if (running) {
            progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }
    }

    private void resetStats() {
        filesValue.setText("0");
        foldersValue.setText("0");
        bytesValue.setText("0 B");
        errorsValue.setText("0");
        elapsedValue.setText("—");
    }

    private void showStats(ShredStats s) {
        if (s == null) {
            return;
        }
        filesValue.setText(Long.toString(s.getFilesShredded()));
        foldersValue.setText(Long.toString(s.getFoldersDeleted()));
        bytesValue.setText(ShredStats.humanBytes(s.getBytesOverwritten()));
        errorsValue.setText(Long.toString(s.getErrors()));
        elapsedValue.setText(String.format("%.1f s", s.getElapsedMillis() / 1000.0));
    }

    private String summaryLine(ShredStats s) {
        return String.format("Shredded %d file(s) and removed %d folder(s) — %s cleared%s.",
                s.getFilesShredded(),
                s.getFoldersDeleted(),
                ShredStats.humanBytes(s.getBytesOverwritten()),
                s.getErrors() > 0 ? " (" + s.getErrors() + " error(s))" : "");
    }

    /** Guard against nuking a whole disk, home folder, or system root by accident. */
    private boolean isProtected(Path folder) {
        Path p = folder.toAbsolutePath().normalize();
        if (p.getParent() == null) {
            return true; // filesystem root, e.g. "/"
        }
        String home = System.getProperty("user.home");
        if (home != null && p.equals(Path.of(home).toAbsolutePath().normalize())) {
            return true;
        }
        String path = p.toString();
        return path.equals("/System") || path.equals("/Library")
                || path.equals("/Users") || path.equals("/Applications")
                || path.equals("/private") || path.equals("/usr") || path.equals("/etc");
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Shredder");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Thin styled separator line. */
    private static final class Separator extends Region {
        Separator() {
            getStyleClass().add("divider");
            setMinHeight(1);
            setMaxWidth(Double.MAX_VALUE);
        }
    }

    public static void main(String[] args) {
        // Keep the JVM/JavaFX exit tidy if the window is closed mid-run.
        Platform.setImplicitExit(true);
        launch(args);
    }
}
