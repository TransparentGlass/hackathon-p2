package com.team.translator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Auto-Translator (Prototype)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label status = new Label("Status: Ready");

        Button btnClipboard = new Button("Translate Clipboard (Ctrl+Shift+C)");
        Button btnDrag = new Button("Drag-to-Translate (Ctrl+Shift+T)");

        btnDrag.setOnAction(e -> {
            // Real drag-to-translate: show overlay, capture region, preprocess, OCR, then display result
            Overlay overlay = new Overlay(new Stage(), new Overlay.OnSelect() {
                @Override
                public void onRectSelected(int x, int y, int w, int h) {
                    // run capture + OCR off the JavaFX thread
                    status.setText("Status: Capturing...");
                    new Thread(() -> {
                        try {
                            // Capture the selected screen area
                            java.awt.image.BufferedImage raw = Screencapture.capture(x, y, w, h);

                            // Preprocess image
                            java.awt.image.BufferedImage high = ImagePrep.toHighContrast(raw, 160);
                            java.awt.image.BufferedImage scaled = ImagePrep.scale2x(high);

                            // Run OCR (ensure Tesseract is installed and path is correct)
                            OcrService ocr = new OcrService("C:\\Program Files\\Tesseract-OCR", "eng");
                            String text = ocr.read(scaled);

                            // Show result on JavaFX thread
                            javafx.application.Platform.runLater(() -> {
                                status.setText("Status: OCR complete");
                                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                                a.setTitle("OCR Result");
                                a.setHeaderText("Recognized text");
                                a.setContentText(text == null || text.isEmpty() ? "(no text)" : text);
                                // Make sure the alert is owned by the main stage so it appears above the app
                                a.initOwner(stage);
                                a.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                                a.show();
                                // bring underlying alert window to front
                                javafx.stage.Stage dialogStage = (javafx.stage.Stage) a.getDialogPane().getScene().getWindow();
                                if (dialogStage != null) dialogStage.toFront();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            javafx.application.Platform.runLater(() -> {
                                status.setText("Status: Error during OCR");
                                javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                                err.setTitle("OCR Error");
                                err.setHeaderText("OCR failed");
                                String msg = ex.getMessage() == null ? "See console for details." : ex.getMessage();
                                err.setContentText(msg + "\n\nMake sure Tesseract is installed and TESSDATA_PREFIX points to the folder that contains the tessdata directory (e.g. C:\\Program Files\\Tesseract-OCR\\tessdata).");
                                err.showAndWait();
                            });
                        }
                    }).start();
                }

                @Override
                public void onCanceled() {
                    javafx.application.Platform.runLater(() -> status.setText("Status: Canceled"));
                }
            });

            overlay.show();
        });
            // dialog already shown in handler
        VBox root = new VBox(12,
            title,
            new HBox(10, btnClipboard, btnDrag),
            status
        );
        root.setPadding(new Insets(14));
        root.setPrefSize(520, 160);

        stage.setTitle("Translator Prototype");
        stage.setScene(new Scene(root));
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}