package com.team.translator;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Overlay {
    public interface OnSelect {
        void onRectSelected(int x, int y, int w, int h);
        void onCanceled();
    }

    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext g;
    private final OnSelect callback;

    private double startX, startY, curX, curY;

    public Overlay(Stage stage, OnSelect callback) {
        this.stage = stage;
        this.callback = callback;

        // Full-screen transparent stage
        stage.initStyle(StageStyle.TRANSPARENT);
    // Use visual bounds to exclude OS taskbars and ensure proper positioning
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();  // later you can merge all screens
    // Position the stage to cover the primary screen so scene coordinates map to screen coordinates
    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());
    stage.setWidth(bounds.getWidth());
    stage.setHeight(bounds.getHeight());
    canvas = new Canvas(bounds.getWidth(), bounds.getHeight());
        g = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new javafx.scene.Group(canvas), Color.TRANSPARENT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hide();
                callback.onCanceled();
            }
        });

        // Mouse handlers
        // Use scene-relative coordinates for drawing (more reliable across DPI / stage positioning)
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            startX = curX = e.getSceneX();
            startY = curY = e.getSceneY();
            redraw();
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            curX = e.getSceneX();
            curY = e.getSceneY();
            redraw();
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            // Compute rectangle in scene coordinates
            double sx = Math.min(startX, curX);
            double sy = Math.min(startY, curY);
            double sw = Math.abs(curX - startX);
            double sh = Math.abs(curY - startY);

            // Make sure the final rectangle is visible before asking for confirmation
            redraw();

            int w = (int) sw;
            int h = (int) sh;

            if (w > 3 && h > 3) {
                // Convert scene coords back to screen coords for Robot capture
                int x = (int) Math.round(sx + stage.getX());
                int y = (int) Math.round(sy + stage.getY());

                // Ask user to confirm the selection before proceeding
                javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirm.initOwner(stage);
                confirm.setTitle("Confirm selection");
                confirm.setHeaderText("Use this selection?");
                confirm.setContentText(String.format("x=%d y=%d  w=%d h=%d", x, y, w, h));
                // Use non-blocking show() and handle the user's choice asynchronously so
                // the JavaFX thread isn't blocked and the main UI remains responsive.
                confirm.show();
                confirm.setOnHidden(evt -> {
                    javafx.scene.control.ButtonType result = confirm.getResult();
                    if (result == javafx.scene.control.ButtonType.OK) {
                        hide();
                        callback.onRectSelected(x, y, w, h);
                    } else {
                        hide();
                        callback.onCanceled();
                    }
                });
            } else {
                hide();
                callback.onCanceled();
            }
        });

            stage.setAlwaysOnTop(true);
            stage.setFullScreen(false);
            stage.setScene(scene);
        }

        private void redraw() {
            // Dim the screen slightly
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.setFill(new Color(0, 0, 0, 0.2));
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Draw selection rectangle
            double x = Math.min(startX, curX);
            double y = Math.min(startY, curY);
            double w = Math.abs(curX - startX);
            double h = Math.abs(curY - startY);

            g.setFill(new Color(1, 1, 1, 0.15));
            g.fillRect(x, y, w, h);

            g.setStroke(Color.web("#00d4ff"));
            g.setLineWidth(2);
            g.strokeRect(x, y, w, h);
        }

        public void show() {
            Platform.runLater(() -> {
                // Ensure initial dim is drawn and the stage is visible and focused to receive events
                redraw();
                stage.show();
                stage.toFront();
                stage.requestFocus();
            });
        }

        public void hide() { Platform.runLater(stage::hide); }
    }


