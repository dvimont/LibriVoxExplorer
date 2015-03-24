/*
 * Copyright (C) 2015 Daniel Vimont
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commonvox.le_console;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.commonvox.le_browser.LeBrowser;
import org.commonvox.le_catalog.LeCatalog;

/**
 *
 * @author Daniel Vimont
 */
public class LeConsole extends Application {
    static String[] commandLineArgs;
    static final double DIALOG_BUTTON_WIDTH = 80;
    static final double STAGE_WIDTH = 800;
    static final double STAGE_HEIGHT = 400;
    static final String STAGE_TITLE = "Admin Console for LibriVox EXPLORER";
    static final PrintStream STDOUT = System.out;
    static final PrintStream STDERR = System.err;
    static File userSelectedTargetDirectory 
            = new File(System.getProperty("user.home"));
    static final String LOG_FILE_NAME_PREFIX = "catalogAssemblyLog";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // for testing only
//        args = new String[] {
//                "FUNCTION=assembleCatalog",
//                "PROJECT_PATH=C:/Users/DanUltra/Documents/javaLibriVoxFiles",
//                "BUILD_FOLDER=round0011",
//                "PREVIOUS_BUILD_FOLDER=round0006",
//                "JPEG_PERMANENT_FOLDER=jpegs/fullSize/coverArt",
//                "JPEG_NEW_IMAGES_SUBFOLDER=newJpegs",
//                "LOG_OUTPUT_SUBFOLDER=logs",
//                "STARTING_AUDIOBOOK_ID=5986",
//                "PROCESSING_LIMIT=5"}; 
        commandLineArgs = args;
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        boolean assembleCatalogShutdownCompleted = false;
        TextArea textArea = new TextArea();
        textArea.setPrefSize(STAGE_WIDTH, STAGE_HEIGHT);
        TextAreaOutputStream textAreaOutputStream 
                = new TextAreaOutputStream(textArea);
        PrintStream textAreaPrintStream 
                = new PrintStream(textAreaOutputStream, true);
        System.setOut(textAreaPrintStream);
        System.setErr(textAreaPrintStream);
        
        /* setup bottom pane */
        Button startButton = new Button("Start");
        startButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        startButton.setDisable(true);
        Button cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        Button saveButton = new Button("Save log");
        saveButton.setDisable(true);
        saveButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        saveButton.setOnAction((ActionEvent e) -> {
            saveOutputText(textAreaOutputStream.toString(), primaryStage); });
        GridPane dialogButtonGridPane = new GridPane();
        dialogButtonGridPane.addRow(0,startButton,cancelButton,saveButton);
        dialogButtonGridPane.setHgap(10);
        dialogButtonGridPane.setPadding(new Insets(10));
        HBox dialogBottomHBox = new HBox(dialogButtonGridPane);
        dialogBottomHBox.setPrefWidth(STAGE_WIDTH);
        dialogBottomHBox.setAlignment(Pos.BOTTOM_RIGHT);
        
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(textArea);
        borderPane.setBottom(dialogBottomHBox);
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.setTitle(STAGE_TITLE);
        primaryStage.getIcons().add
            (new Image(LeBrowser.class.getResourceAsStream(LeBrowser.APP_ICON)));
        primaryStage.show();

        final Task<Boolean> validateArgs 
            = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    boolean argumentsValid 
                            = LeCatalog.parseAndValidate(true, commandLineArgs);
                    if (LeCatalog.getLogOutputDirectory() != null) {
                        userSelectedTargetDirectory = LeCatalog.getLogOutputDirectory();
                    }
                    return argumentsValid;
                }
            };

        validateArgs.setOnSucceeded((WorkerStateEvent t) -> {
            if (validateArgs.getValue()) {
                System.out.println(   "====================\n" +
                                      "Runtime arguments are all valid.\n"
                                    + "Review runtime arguments above and click "
                                    + "Start button to execute CATALOG ASSEMBLY.\n"
                                    + "====================\n");
                startButton.setDisable(false);
            } else {
                System.out.println(   "====================\n" +
                                      "Invalid runtime argument(s) submitted.\n"
                                    + "Review explanation above and click "
                                    + "Cancel button to exit.\n"
                                    + "====================\n");
            }
        });
        
        final Task<Boolean> assembleCatalog 
            = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    try {
                        LeCatalog.main(commandLineArgs); 
                        return true;
                    } catch (InterruptedException ie) {
                        System.out.println("\n*************\n"
                                + "Task has been cancelled upon user request.\n"
                                + "*************");
                    } catch (Exception e) {
                        System.out.println("\n*************\n"
                                + "An exception has terminated processing.\n"
                                + "*************");
                        if (e.getMessage() != null) {
                            System.err.println(e.getMessage());
                        }
                        e.printStackTrace(System.err);
                    }
                    return false;
                }
            };

        assembleCatalog.setOnSucceeded((WorkerStateEvent t) -> {
            cancelButton.setText("Close");
            System.out.println("\n===================="); 
            if(assembleCatalog.getValue()) {
                System.out.println("Catalog assembly task has completed successfully!");
            } else {
                System.out.println("Catalog assembly task has abnormally terminated.");
            }
            System.out.println("====================");
        });
        
        assembleCatalog.setOnCancelled(null);
        cancelButton.setOnAction((ActionEvent e) -> {
            if (assembleCatalog.isRunning()) {
                assembleCatalog.cancel();
                cancelButton.setText("Close");
            } else {
                System.out.flush();
                System.setOut(STDOUT);
                System.setErr(STDERR);
                primaryStage.close();
                System.out.println("Invoking Platform & System exit.");
                Platform.exit(); 
                System.exit(0);  
            }
        });
        primaryStage.setOnCloseRequest((WindowEvent e) -> { 
            e.consume();
            cancelButton.fire();
        });
        startButton.setOnAction((ActionEvent e) -> {
            startButton.setDisable(true);
            saveButton.setDisable(false);
            saveButton.requestFocus();
            new Thread(assembleCatalog).start();
        });
        
        new Thread(validateArgs).start();
    }

    public static class TextAreaOutputStream extends OutputStream {
        private final TextArea outputTextArea;
        private final StringBuilder outputStringBuilder;

        public TextAreaOutputStream(TextArea textArea) {
            this.outputTextArea = textArea;
            this.outputStringBuilder = new StringBuilder();
        }

        @Override
        public void write(int i) throws IOException {
            char conversionChar = (char) i;
            Platform.runLater(() -> {
                outputTextArea.appendText(String.valueOf(conversionChar));
            });
            outputStringBuilder.append(conversionChar);
            //STDOUT.write(i);
        }
        
        @Override
        public String toString() {
            return outputStringBuilder.toString();
        }
    }
    
    private void saveOutputText (String outputText, Stage ownerStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (Files.exists(userSelectedTargetDirectory.toPath())
                && Files.isDirectory(userSelectedTargetDirectory.toPath())) {
            directoryChooser.setInitialDirectory(userSelectedTargetDirectory);
        }
        directoryChooser.setTitle("Select target folder for log file");
        File selectedDirectory 
                = directoryChooser.showDialog(ownerStage);
        if (selectedDirectory == null) {
            return;
        } else {
            userSelectedTargetDirectory = selectedDirectory;
        }
        Path targetPath  = Paths.get(selectedDirectory.toURI());
        String targetFileName
            = LOG_FILE_NAME_PREFIX 
                + new Timestamp(new java.util.Date().getTime()).toString()
                        .replaceAll(":", "").replaceAll(" ", "_").substring(0,17)
                + ".txt";
        Path targetFile = targetPath.resolve(targetFileName);
        System.out.println("Saving log to " + targetFile.toString());
        try {
            Files.write(targetFile, outputText.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ioe) {
            System.err.print(ioe.getMessage());
            return;
        }
        Label confirmationLabel = new Label("Log saved to:\n  -->> "
                                    + targetFile.toString());
        confirmationLabel.setWrapText(true);
        confirmationLabel.setPadding(new Insets(10));
        confirmationLabel.setFont(LeBrowser.BOLD_BIGGER_FONT);
        Stage confirmationDialogStage = new Stage();
        confirmationDialogStage.initStyle(StageStyle.UTILITY);
        confirmationDialogStage.initModality(Modality.APPLICATION_MODAL);
        confirmationDialogStage.setMaxWidth(STAGE_WIDTH * 2);
        confirmationDialogStage.setResizable(false);
        confirmationDialogStage.initOwner(ownerStage);
        confirmationDialogStage.setScene
                    (new Scene(new StackPane(confirmationLabel))); 
        confirmationDialogStage.show();
    }
}
