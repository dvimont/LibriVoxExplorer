/*
 * Copyright (C) 2014 Daniel Vimont
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
package org.commonvox.le_browser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javax.xml.bind.JAXBException;
import org.commonvox.le_catalog.Audiobook;
import org.commonvox.le_catalog.Author;
import org.commonvox.le_catalog.Catalog;
import org.commonvox.le_catalog.CatalogCallback;
import org.commonvox.le_catalog.CatalogMarshaller;
import org.commonvox.le_catalog.Downloads;
import org.commonvox.le_catalog.Genre;
import org.commonvox.le_catalog.HasLibrivoxId;
import org.commonvox.le_catalog.Language;
import org.commonvox.le_catalog.PublicationDate;
import org.commonvox.le_catalog.Reader;
import org.commonvox.le_catalog.SearchParameters;
import org.commonvox.le_catalog.Section;
import org.commonvox.le_catalog.Title;
import org.commonvox.le_catalog.Translator;
import org.commonvox.le_catalog.MyBookmarks;
import org.commonvox.le_catalog.MyList;
import org.commonvox.le_catalog.Work;
import org.commonvox.indexedcollection.InvalidIndexedCollectionQueryException;
import org.commonvox.indexedcollection.Key;
import org.commonvox.indexedcollection.IndexedKey;
import org.commonvox.le_catalog.InterruptibleDownloader;
import org.commonvox.le_catalog.PersistedUserSelectedCollection;
import org.commonvox.le_console.LeConsole;

/**
 *
 * @author Daniel Vimont
 */
public class LeBrowser extends Application {
    //***************************** VERSION & RELEASE METADATA
    static final int VERSION = 1;
    static final int RELEASE = 5;
    static final int SM_LEVEL = 1;
    //***************************** END VERSION & RELEASE METADATA
    static final String V_R_SM = VERSION + "." + RELEASE + "." + SM_LEVEL;
    static final String PRODUCT_NAME = "CommonVox.org presents: LibriVox EXPLORER";
            //"LibriVox EXPLORER and iTunes-Importer";
    static final String COMMONVOX_URL_STRING = "http://commonvox.org";
    static final String TUTORIAL_URL_STRING 
                            = COMMONVOX_URL_STRING + "/le-tutorial";
    static final String COMMENTS_URL_STRING 
                            = COMMONVOX_URL_STRING + "/le-comments";
    static final String SOFTWARE_UPDATE_URL_STRING 
                            = COMMONVOX_URL_STRING + "/le-download";
    Stage mainStage = new Stage();
    Stage downloadDialogStage;
    Scene mainScene;
    StackPane startupStackPane;
    StackPane startupLogoStackPane;
    final Label startupProgressText = new Label("Startup is commencing.");
    ProgressBar startupProgressBar = new ProgressBar();
    StackPane mainStackPane;
    Pane topPaneHBox;
    boolean detailPaneActive = false;
    boolean quickBrowsePaneActive = false;
    TabPane centerTabPane;
    Label tabPaneTitleLabel = new Label();
    ToggleButton tbGenre;
    ToggleButton tbAuthor;
    ToggleButton tbReader;
    ToggleButton tbLanguage;
    ToggleButton tbMyList;
    ToggleButton tbTitle;
    ToggleButton tbNewest;
    //ToggleButton tbPopularity;
    ToggleButton tbDownloads;
    FadeTransition startupAnimation;
    
    Button leftDetailSlideshowButton;
    Button rightDetailSlideshowButton;
    
    HBox mediaPlayerControlBox;
    HBox mediaPlayerContainerPane;
    MediaPlayer mediaPlayer;
    Label mediaCurrentlyPlayingLabel = new Label();
    Duration mediaFullDuration;
    Label mediaFullDurationLabel;
    Duration mediaCurrentTime;
    Label mediaCurrentTimeLabel;
    Slider mediaTimeSlider;
    Audiobook currentDetailAudiobook;
    int mediaCurrentSectionIndex;
    ProgressIndicator mp3LoadProgressIndicator;
    Label mp3LoadProgressLabel;
    private final String LOADING_MEDIA = "LOADING MEDIA...";
    private final String RESUMING_BOOKMARK = "RESUMING AT BOOKMARK";
    HBox mp3LoadProgressHBox;
    ThreadPoolExecutor downloadExecutor 
        = new ThreadPoolExecutor(5,10,10,TimeUnit.SECONDS,new LinkedBlockingQueue<>());
    Task<SearchParameters> searchTask;
    TextField searchTextField;
    private List<Audiobook> m4bAudiobooks;
    private Random randomIntegerGenerator = new Random();

    static final String FILE_SEPARATOR = System.getProperty("file.separator");
    static final String ADD_TO_ITUNES_SUBPATH
            = FILE_SEPARATOR + "Music" + FILE_SEPARATOR + "iTunes" 
            + FILE_SEPARATOR + "iTunes Media" + FILE_SEPARATOR 
            + "Automatically Add to iTunes";
    static final File ADD_TO_ITUNES_DIRECTORY
            = new File(System.getProperty("user.home") + ADD_TO_ITUNES_SUBPATH);
    static final File ADD_TO_ITUNES_PUBLIC_DIRECTORY
            = new File(System.getenv("PUBLIC") + ADD_TO_ITUNES_SUBPATH);
    // MAC constants added  v1.4.0
    static final String ADD_TO_ITUNES_SUBPATH_MAC
            = ADD_TO_ITUNES_SUBPATH + ".localized"; 
    static final File ADD_TO_ITUNES_DIRECTORY_MAC
            = new File(System.getProperty("user.home") + ADD_TO_ITUNES_SUBPATH_MAC);
    static final File ADD_TO_ITUNES_PUBLIC_DIRECTORY_MAC
            = new File(System.getenv("PUBLIC") + ADD_TO_ITUNES_SUBPATH_MAC);
    static File iTunesImportDirectory = null;
    static File userDownloadDirectory = new File(System.getProperty("user.home"));
    static File targetDirectory = null;
    static final String STAGE_TITLE 
            = PRODUCT_NAME + " v" + V_R_SM +
                " -- free/libre open source software (FLOSS)";
    static final String STARTUP_STAGE_TITLE
            = "Starting up: " + STAGE_TITLE;
    static final String CURRENT_DETAIL_AUDIOBOOK_MSG_TEMPLATE
            = "ID of last active detail audiobook was: %s";
    static final double SCENE_WIDTH = 1000; // originally set to 900
    static final double SCENE_HEIGHT = 500;
    static final double PROGRESS_BAR_WIDTH = 300;
    static final double PROGRESS_BAR_HEIGHT = 20;
    static final double BTN_WIDTH = 90;
    static final double BTN_HEIGHT = 30;
    static final double IMAGE_SIDE_LENGTH = 150;
    static final double IMAGE_DETAIL_SIDE_LENGTH = 200;
    static final double DETAIL_WINDOW_WIDTH = 855;
    static final double DETAIL_WINDOW_HEIGHT = 475; 
    static final double DIALOG_WIDTH = 360;
    static final double DIALOG_HEIGHT = 360;
    static final double DIALOG_BUTTON_WIDTH = 80;
    static final Insets INSETS_10 = new Insets(10);

    static final String PILL_BUTTON_CSS_FILE = "PillButton.css";
    static final String WEBVIEW_CSS_FILE = "WebView.css";
    static final String RADIO_BUTTON_CSS_FILE = "Radio.css";
    public static final String APP_ICON = "images/app_icon.png";
    static final String LIBRIVOX_LOGO_JPG_FILE = "images/librivoxLogoCropped.jpg";
    final Image OOPS_SMILEY_IMAGE
            = new Image(getClass().getResourceAsStream("images/oopsSmiley.jpg"));
    final Image LOGO_IMAGE 
            = new Image(getClass().getResourceAsStream("images/comboLogo.jpg"));
    final Image PLAY_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/playButton160.png"));
    final Image PAUSE_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/pauseButton160.png"));
    final Image ITEM_PLAYING_IMAGE
            = new Image(getClass().getResourceAsStream("images/itemPlaying.png"));
    final Image MENU_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/menuButton.png"));
    final Image MENU_BUTTON_RED_ASTERISK_IMAGE
        = new Image(getClass().getResourceAsStream("images/menuButtonRedAsterisk.png"));
    final Image SEARCH_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/search32.png"));
    final Image SEARCH_GOOGLE_IMAGE
            = new Image(getClass().getResourceAsStream("images/googleSearch.png"));
    final Image CHECKMARK_GREEN_IMAGE
            = new Image(getClass().getResourceAsStream("images/checkMarkGreen26.png"));
    final Image CHECKMARK_BLACK_IMAGE
            = new Image(getClass().getResourceAsStream("images/checkMarkBlack26.png"));
    final Image SHARE_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/shareButton.png"));
    final Image FB_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/fb64.png"));
    final Image TWITTER_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/twitter64.png"));
    final Image GPLUS_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/g+64.png"));
    final Image TUMBLR_BUTTON_IMAGE
            = new Image(getClass().getResourceAsStream("images/tumblr64.png"));
    final Image COMMONVOX_PRESENTS_IMAGE
        = new Image(getClass().getResourceAsStream("images/commonvox_presents.png"));
    final Image LIBRIVOX_EXPLORER_LOGO
        = new Image(getClass().getResourceAsStream("images/librivoxExplorerLogo.png")); 
    final Image LIBRIVOX_EXPLORER_SUBTITLE
        = new Image(getClass().getResourceAsStream("images/librivoxExplorerSubtitle.png")); 
    final Image A_READING
        = new Image(getClass().getResourceAsStream("images/aReading.jpg"));
    final Image HELP_IMAGE
        = new Image(getClass().getResourceAsStream("images/helpImage.png"));
    final Label HIDDEN_DUMMY_LABEL = new Label();
    final int MAX_EXPANDED_PANES = 8; // changed from 10 to 8 in v1.4.1
    private enum ExpandedPanesOption {COUNT, REFRESH, CLOSE};

    ObservableList<ImageView> sectionMediaImageViews;
    ImageView playButtonImageView = new ImageView();
    static final String AUDIOBOOK_TEXT_LABEL_PINK_CSS
            = "-fx-background-color: pink; -fx-border-color:black; "
                    + "-fx-border-width: 1; -fx-border-style: solid;";
    static final String AUDIOBOOK_TEXT_LABEL_BLUE_CSS
            = "-fx-background-color: lightblue; -fx-border-color:black; "
                    + "-fx-border-width: 1; -fx-border-style: solid;";
    static final String AUDIOBOOK_IMAGE_LABEL_CSS
            = "-fx-background-color: " 
                    + "linear-gradient(to bottom, #dbdcdd 0%, #a9a9ab 100%);" 
                    + "-fx-padding: 6px;";
    static final String SKYBLUE_CSS = "-fx-background-color: skyblue;";
    static final String LIGHTCYAN_CSS = "-fx-background-color: lightcyan;";
    static final String LIGHTGREEN_CSS = "-fx-background-color: lightgreen;";
    static final String WHITE_CSS = "-fx-background-color: white;";
    static final String WHITESMOKE_CSS = "-fx-background-color: whitesmoke;";
    static final String SLATEGRAY_CSS = "-fx-background-color: slategray;";
    static final String STYLE_SCALE_SHAPE_FALSE = "-fx-scale-shape: false;"; 
    static final String GOOGLE_QUERY_URL = "https://www.google.com/search?q=";
    static final String BOLD_TEXT_CSS = "-fx-font-weight: bold";
    static final String RED_BOLD_TEXT_CSS = "-fx-text-fill: red; -fx-font-weight: bold";
    static final Font BOLD_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD, 
                                            Font.getDefault().getSize());
    static final Font ITALIC_FONT 
            = Font.font(Font.getDefault().toString(), FontPosture.ITALIC, 
                                            Font.getDefault().getSize());
    static final Font ITALIC_BIGGER_FONT 
            = Font.font(Font.getDefault().toString(), FontPosture.ITALIC, 
                                            Font.getDefault().getSize()+2);
    static final Font ITALIC_BIGGEST_FONT 
            = Font.font(Font.getDefault().toString(), FontPosture.ITALIC, 20);
    static final Font BOLD_SMALLER_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD,
                                            Font.getDefault().getSize()-1);
    static final Font BOLD_SMALLEST_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD, 
                                            Font.getDefault().getSize()-2);
    static final Font BOLD_SLIGHTLY_BIGGER_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD,
                                            Font.getDefault().getSize()+1);
    public static final Font BOLD_BIGGER_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD,
                                            Font.getDefault().getSize()+2);
    static final Font BOLD_BIGGEST_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD, 20);
    static final Font BOLD_ENORMOUS_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD, 24);
    static final Font BIGGER_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.NORMAL, 
                                            Font.getDefault().getSize()+1);
    static final Font SMALLER_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.NORMAL, 
                                            Font.getDefault().getSize()-1);
    static final Font SMALLEST_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.NORMAL, 
                                            Font.getDefault().getSize()-2);
    static final Font SMALLEST_BOLD_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.EXTRA_BOLD, 
                                            Font.getDefault().getSize()-2);
    static final Font SUPERSMALL_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.BOLD, 10);
    static final Font SUPERSMALL_ITALIC_FONT 
            = Font.font(Font.getDefault().toString(), FontPosture.ITALIC, 10);
    static final Font SUPERSMALL_BOLD_FONT 
            = Font.font(Font.getDefault().toString(), FontWeight.BOLD, 10);
    static final Background RED_BACKGROUND 
            = new Background(new BackgroundFill
                                (Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
    static final Background GRAY_BACKGROUND 
            = new Background(new BackgroundFill
                                (Color.GREY, CornerRadii.EMPTY, Insets.EMPTY));
    static final Background WHITESMOKE_BACKGROUND 
            = new Background(new BackgroundFill
                                (Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY));
    static final Background BLACK_BACKGROUND 
            = new Background(new BackgroundFill
                                (Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY));
    static final Background DOWNLOAD_PANES_BACKGROUND = WHITESMOKE_BACKGROUND;
    static final Color SECTION_TEXT_COLOR = Color.LIGHTGRAY; 
    private enum ArrowType {LEFT_ARROW, RIGHT_ARROW};
    static final String REGEX_HREF = "href\\=\".*?\"";
    private enum OverlayPaneOption 
        {DETAIL("Audiobook Details"),NEWEST("The Latest Offerings"),
            RANDOM("A Random Selection"),SEARCH("Search results (powered by Google)");
            private final String title;
            OverlayPaneOption(String title) { this.title = title; }
            private String getTitle() { return this.title; }  };
    Catalog catalog;
    MyList myList = new MyList();
    MyBookmarks myBookmarks = new MyBookmarks();
    Class<? extends Key> selectedOrderClass;
    boolean alternateLabelColor = false;
    boolean buildCatalogShutdownCompleted = false;
    final FinalBoolean downloadShutdownCompleted = new FinalBoolean();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            if (args[0].toLowerCase().equals("console")) {
                LeConsole.main(Arrays.copyOfRange(args, 1, args.length));
            } else {
                System.out.println("Invalid command-line invocation: " + args[0]);
                return;
            }
        } else {
            launch(args);
        }
    }
    
    @Override
    public void start(final Stage primaryStage) 
            throws Exception {
        System.out.println("Starting session of " + PRODUCT_NAME + " v" + V_R_SM);
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("LE v" + V_R_SM + ": Exception in main thread: " + throwable.getMessage());
            if (currentDetailAudiobook != null) {
                System.out.println(String.format
                    (CURRENT_DETAIL_AUDIOBOOK_MSG_TEMPLATE, 
                                        currentDetailAudiobook.getId()));
            }
            throwable.printStackTrace();
            showExceptionStage(thread.getName(), throwable); 
        });

        final Task<Catalog> buildCatalog 
                = new Task<Catalog>() {
            @Override 
            protected Catalog call() 
            {
                try {
                    CatalogCallback callback
                            = new CatalogCallback () {
                                @Override
                                public void updateTaskProgress
                                                    (long workDone, long max) {
                                    updateProgress(workDone,max);
                                }
                                @Override
                                public void updateTaskMessage(String message) {
                                    updateMessage(message);
                                }
                                @SuppressWarnings("unchecked")
                                @Override
                                public void passbackObject(Object object) {
                                    m4bAudiobooks = (List<Audiobook>)object;
                                    Platform.runLater(() -> { 
                                        runStartupCoverArtShow();
                                    });
                                }
                            };
                    catalog = CatalogMarshaller.unmarshalCatalogFromXml(callback);
                    if (!this.isCancelled()) {
                        // file existence validation added v1.3.2
                        callback.updateTaskMessage("Checking for audiobook metadata updates.");
                        if (InterruptibleDownloader.getFileSize
                                (CatalogMarshaller.DEFAULT_URL_STRING_NEW_AUDIOBOOKS) > 0) {
                            catalog.merge
                                (CatalogMarshaller.unmarshalCatalogFromXml
                                    (CatalogMarshaller.DEFAULT_URL_STRING_NEW_AUDIOBOOKS,
                                                                        callback));
                        } else {
                            System.out.println("** no audiobook updates found for processing **");
                        }
                    }
                    if (!this.isCancelled()) {
                        // file existence validation added v1.3.2
                        callback.updateTaskMessage("Checking for audiobook metadata corrections.");
                        if (InterruptibleDownloader.getFileSize
                                (CatalogMarshaller.DEFAULT_URL_STRING_CORRECTIONS) > 0) {
                            catalog.merge
                                (CatalogMarshaller.unmarshalCatalogFromXml
                                    (CatalogMarshaller.DEFAULT_URL_STRING_CORRECTIONS,
                                                                        callback));
                        } else {
                            System.out.println("** no audiobook corrections found for processing **");
                        }
                    }
                    if (!this.isCancelled()) {       
                        Catalog.printMemoryUsage
                            ("After catalog data unmarshalling.", Catalog.DIAGNOSTIC_MODE);
                        catalog.bootUp(callback);
                    }
                } catch (InterruptedException e) {
                    if (this.isCancelled()) {
                        buildCatalogShutdownCompleted = true;
                        return null;
//                    } else {
//                        throw e;
                    }
                } catch (Exception e) {
                    this.updateTitle("buildCatalog");
                    Platform.runLater(() -> { 
                        showExceptionStage(this.getTitle(), e); });
                    this.cancel();
                }
                buildCatalogShutdownCompleted = true;
                return catalog;
            }
        };
        buildCatalog.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent t) {
                setITunesImportDirectory();
                catalog = buildCatalog.getValue();
                Catalog.printMemoryUsage
                        ("After catalog build.", Catalog.DIAGNOSTIC_MODE);
                mainScene = getMainScene();
                FadeTransition fadeOut =
                        new FadeTransition(Duration.seconds(3), startupStackPane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.3);
                fadeOut.play();
                fadeOut.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainStage.setTitle(STAGE_TITLE);
                        mainStage.setScene(mainScene);
                        mainStage.show();
                        Catalog.printMemoryUsage
                            ("After mainStage build.", Catalog.DIAGNOSTIC_MODE);
                    }
                });
            }
        });
        /** If user closes main window (mainStage) while buildCatalog task
         * is running, buildCatalog task (and all other active tasks) must 
         * be canceled. */
        mainStage.setOnCloseRequest((WindowEvent e) -> { 
            e.consume();
            if (buildCatalog.isRunning()) {
                buildCatalog.cancel();
                while (!buildCatalogShutdownCompleted) {
                    try { Thread.sleep(200);
                    } catch(InterruptedException ex) 
                        { Thread.currentThread().interrupt(); } 
                }
            }
            doShutdownSequence();
        });
        
        Image appIcon = new Image(getClass().getResourceAsStream(APP_ICON));
        mainStage.getIcons().add(appIcon);
        mainStage.setTitle(STARTUP_STAGE_TITLE);
        mainStage.setScene(getStartupScene(buildCatalog));
        mainStage.show();
        startupAnimation.play();
        
        startupProgressText.textProperty().bind(buildCatalog.messageProperty());
        startupProgressBar.progressProperty().bind(buildCatalog.progressProperty());
    }
    
    private void doShutdownSequence() {
        if (searchTask != null && searchTask.isRunning()) {
            searchTask.cancel();
        }
        if (downloadExecutor.getActiveCount() > 0) {
            //e.consume();
            getCancelDownloadConfirmation(null, mainStage);
            return;
        }
        if (mediaPlayer != null 
                && (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)
                    || mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED))
                && PersistedAppSettings.getBookmarkSuppressSetting().equals
                    (PersistedAppSettings.BookmarkSuppressSetting.BOOKMARKS_ALLOW))
        {
            getBookmarkConfirmation(true, mainStage, currentDetailAudiobook,
                                                    mediaCurrentSectionIndex);
            return;
        }
        System.out.println("Invoking Platform & System exit.");
        Platform.exit(); 
        System.exit(0); // 2015-01-12 added to prevent JWrapper from hanging
    }

    private void showExceptionStage (String taskName, Throwable throwable) {
        Stage exceptionStage = new Stage();
        exceptionStage.initStyle(StageStyle.UTILITY);
        exceptionStage.setTitle("Internal error encountered");
        exceptionStage.setResizable(false);
        if (mainStage != null) {
            exceptionStage.initOwner(mainStage);
        }
        exceptionStage.initModality(Modality.APPLICATION_MODAL);

        BorderPane exceptionBorderPane = new BorderPane();
        Scene exceptionScene = new Scene(exceptionBorderPane);
        exceptionBorderPane.setBackground(DOWNLOAD_PANES_BACKGROUND);
        
        ImageView oopsSmileyView = new ImageView(OOPS_SMILEY_IMAGE);
        oopsSmileyView.setFitHeight(BTN_HEIGHT * 4);
        oopsSmileyView.setPreserveRatio(true);

        Label exceptionLabel 
                = new Label("An internal error has occurred.\n"
                    + "You are not at fault!!\n"
                    + "The gory details are displayed below.");
        exceptionLabel.setGraphic(oopsSmileyView);
        exceptionLabel.setContentDisplay(ContentDisplay.LEFT);
        exceptionLabel.setTextAlignment(TextAlignment.LEFT);
        exceptionLabel.setFont(BOLD_BIGGER_FONT);
        exceptionLabel.setPadding(new Insets(5));
        exceptionBorderPane.setTop(exceptionLabel);

        TextArea exceptionText = new TextArea();
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        String currentDetailAudiobookMsg = "";
        if (currentDetailAudiobook != null) {
            currentDetailAudiobookMsg 
                    = String.format (CURRENT_DETAIL_AUDIOBOOK_MSG_TEMPLATE, 
                                        currentDetailAudiobook.getId()) + "\n";
        }
        exceptionText.setText
            ("Exception encountered in task: " + taskName + "\n" 
                    + currentDetailAudiobookMsg
                    + throwable.getMessage() + "\n" + stackTrace);
        exceptionText.setMinWidth(DIALOG_WIDTH * 0.95);
        exceptionText.setMaxWidth(DIALOG_WIDTH * 0.95);
        exceptionText.setEditable(false);
        exceptionText.setPadding(new Insets(5));
        StackPane exceptionTextStackPane = new StackPane(exceptionText);
        exceptionBorderPane.setBottom(exceptionTextStackPane);
        
        Button submitReportButton = new Button("Submit\nProblem Report");
        submitReportButton.setTextAlignment(TextAlignment.CENTER);
        submitReportButton.setFont(BOLD_BIGGER_FONT);
        submitReportButton.setOnMouseEntered
            ((MouseEvent me)->{exceptionScene.setCursor(Cursor.HAND);});
        submitReportButton.setOnMouseExited
            ((MouseEvent me)->{exceptionScene.setCursor(Cursor.DEFAULT);});
        submitReportButton.setOnMouseClicked
            ((MouseEvent me)->{
                String opSys = System.getProperty("os.name") + ", version=" 
                                    + System.getProperty("os.version");
                getHostServices().showDocument
                    (Catalog.getProblemReportUrlString
                        ("Internal exception encountered " 
                                + "(technical details provided automatically).\n",
                            exceptionText.getText(), 
                            opSys));
                doShutdownSequence();
            });
        Label pleaseSubmitMessage = new Label("We would greatly appreciate it "
                    + "if you would click this button to submit a "
                    + "problem report to us. A prefilled form will open "
                    + "in your browser for you to submit the problem report.");
        pleaseSubmitMessage.setMinWidth(DIALOG_WIDTH * 0.95);
        pleaseSubmitMessage.setMaxWidth(DIALOG_WIDTH * 0.95);
        pleaseSubmitMessage.setWrapText(true);
        pleaseSubmitMessage.setPadding(INSETS_10);
        VBox submitReportVBox = new VBox(submitReportButton, pleaseSubmitMessage);
        submitReportVBox.setAlignment(Pos.CENTER);
        exceptionBorderPane.setCenter(submitReportVBox);
        
        exceptionStage.setScene(exceptionScene);
        exceptionStage.show();
        exceptionStage.setOnCloseRequest((WindowEvent we) -> { 
            doShutdownSequence();
        });
    }
    
    private static void setITunesImportDirectory() {
        if (ADD_TO_ITUNES_DIRECTORY.exists()) {
            iTunesImportDirectory = ADD_TO_ITUNES_DIRECTORY;
        } else if (ADD_TO_ITUNES_PUBLIC_DIRECTORY.exists()) {
            iTunesImportDirectory = ADD_TO_ITUNES_PUBLIC_DIRECTORY;
        } else if (ADD_TO_ITUNES_DIRECTORY_MAC.exists()) {
            iTunesImportDirectory = ADD_TO_ITUNES_DIRECTORY_MAC;  // v1.4.0
        } else if (ADD_TO_ITUNES_PUBLIC_DIRECTORY_MAC.exists()) {
            iTunesImportDirectory = ADD_TO_ITUNES_PUBLIC_DIRECTORY_MAC; // v1.4.0
        } else {
            System.out.println("iTunes import directory NOT found.  "
                                        + "iTunes import NOT enabled.");
            return;
        }
        System.out.println("iTunes import directory found.  "
                                        + "iTunes import enabled.");
    }
    
    private Scene getStartupScene (Task<Catalog> buildCatalog) {
        startupProgressText.setFont(SMALLEST_FONT);
        startupProgressText.setTextFill(Color.DARKGREEN);
        startupProgressBar.setPrefSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        startupProgressBar.setStyle("-fx-accent: lightgreen;");
        StackPane progressStackPane = new StackPane();
        progressStackPane.getChildren().addAll(startupProgressBar, startupProgressText);
        progressStackPane.setPrefSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT+5);
        // extra HBox layer required for Pos.BOTTOM_RIGHT positioning to work!
        HBox progressHBox = new HBox(progressStackPane); 
        progressHBox.setMaxHeight(PROGRESS_BAR_HEIGHT+5);
        progressHBox.setStyle("-fx-padding: 3; ");
        HBox startupLayer1 = new HBox(progressHBox);
        startupLayer1.setAlignment(Pos.BOTTOM_RIGHT);
        startupLayer1.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        
        ImageView commonvoxPresentsImageView = new ImageView(COMMONVOX_PRESENTS_IMAGE);
        commonvoxPresentsImageView.setPreserveRatio(true);
        commonvoxPresentsImageView.setFitHeight(SCENE_HEIGHT * 0.2);
        commonvoxPresentsImageView.setTranslateX(SCENE_WIDTH * 0.4);
        commonvoxPresentsImageView.setTranslateY(SCENE_HEIGHT * 0.4);
        ImageView librivoxExplorerLogoImageView 
                            = new ImageView(LIBRIVOX_EXPLORER_LOGO);
        librivoxExplorerLogoImageView.setTranslateX(SCENE_WIDTH * 0.23);
        librivoxExplorerLogoImageView.setTranslateY(SCENE_HEIGHT * 0.30);
        librivoxExplorerLogoImageView.setOpacity(0);
        ImageView librivoxExplorerSubtitleImageView 
                            = new ImageView(LIBRIVOX_EXPLORER_SUBTITLE);
        librivoxExplorerSubtitleImageView.setTranslateX(SCENE_WIDTH * 0.28);
        librivoxExplorerSubtitleImageView.setTranslateY(SCENE_HEIGHT * 0.60);
        librivoxExplorerSubtitleImageView.setOpacity(0);
        ImageView helpImageView = new ImageView(HELP_IMAGE);
        helpImageView.setPreserveRatio(true);
        helpImageView.setFitHeight(SCENE_HEIGHT * 0.6);
        helpImageView.setTranslateX(SCENE_WIDTH * 0.78);
        helpImageView.setTranslateY(0);
        helpImageView.setOpacity(0);
        startupLogoStackPane 
                = new StackPane(commonvoxPresentsImageView,
                                librivoxExplorerLogoImageView,
                                librivoxExplorerSubtitleImageView,
                                helpImageView);
        startupLogoStackPane.setAlignment(Pos.TOP_LEFT);
        startupLogoStackPane.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        
        startupAnimation =
                new FadeTransition(Duration.seconds(2), commonvoxPresentsImageView);
        startupAnimation.setFromValue(0.0);
        startupAnimation.setToValue(1.0);
        startupAnimation.setOnFinished((ActionEvent ae1) -> {
            TranslateTransition translateTran 
                    = new TranslateTransition(Duration.seconds(1), 
                                                    commonvoxPresentsImageView);
            translateTran.setFromX(SCENE_WIDTH * 0.4);
            translateTran.setToX(SCENE_WIDTH * 0.1);
            translateTran.setFromY(SCENE_HEIGHT * 0.4);
            translateTran.setToY(SCENE_HEIGHT * 0.1);
            translateTran.play();
            translateTran.setOnFinished((ActionEvent ae2) -> { 
                FadeTransition fadeInLv =
                    new FadeTransition(Duration.seconds(2), 
                                            librivoxExplorerLogoImageView);
                fadeInLv.setFromValue(0.0);
                fadeInLv.setToValue(1.0);
                fadeInLv.play();
                fadeInLv.setOnFinished((ActionEvent ae3) -> {
                    try { Thread.sleep(500);
                    } catch(InterruptedException ex) { Thread.currentThread().interrupt(); } 
                    FadeTransition fadeInLvSub =
                        new FadeTransition(Duration.seconds(1), librivoxExplorerSubtitleImageView);
                    fadeInLvSub.setFromValue(0.0);
                    fadeInLvSub.setToValue(1.0);
                    fadeInLvSub.play();
                    fadeInLvSub.setOnFinished((ActionEvent ae4) -> {
                        /*
                        Thread buildCatalogThread = new Thread(buildCatalog);
                        buildCatalogThread.setUncaughtExceptionHandler
                                    (new ThreadExceptionHandler(mainStage));
                        buildCatalogThread.start();
                        */
                        new Thread(buildCatalog).start();
                        FadeTransition fadeInHelp =
                            new FadeTransition(Duration.seconds(3), helpImageView);
                        fadeInHelp.setFromValue(0.0);
                        fadeInHelp.setToValue(1.0);
                        fadeInHelp.play();
                    });
                });
            });
        });

        startupStackPane = new StackPane(startupLayer1, startupLogoStackPane);
        startupStackPane.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        startupStackPane.setStyle(SLATEGRAY_CSS);
        Scene startupScene 
                = new Scene(startupStackPane, SCENE_WIDTH, SCENE_HEIGHT);
        return startupScene;
    }
    
    private void runStartupCoverArtShow() {
        FadeTransition fadeOut 
                = new FadeTransition(Duration.seconds(1), startupLogoStackPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.play();
        fadeOut.setOnFinished((ActionEvent ae1) -> {
            startupStackPane.getChildren().remove(startupLogoStackPane);
            ImageView aReadingImageView = new ImageView(A_READING);
            aReadingImageView.setPreserveRatio(true);
            aReadingImageView.setFitHeight(SCENE_HEIGHT * 0.8);
            Label aReadingLabel 
                    = new Label("Please wait while startup is completed.", 
                                                        aReadingImageView);
            //aReadingLabel.setPadding(new Insets(0,0,30,0)); // removed 2015-02-12
            aReadingLabel.setAlignment(Pos.CENTER);
            aReadingLabel.setContentDisplay(ContentDisplay.TOP);
            aReadingLabel.setFont(BOLD_BIGGEST_FONT);
            aReadingLabel.setBackground(WHITESMOKE_BACKGROUND); // added 2015-02-12
            aReadingLabel.setOpacity(0);
            AnchorPane aReadingAnchorPane = new AnchorPane(aReadingLabel);
            AnchorPane.setTopAnchor(aReadingLabel, SCENE_HEIGHT * 0.04);
            AnchorPane.setLeftAnchor(aReadingLabel, SCENE_WIDTH * 0.195);
            startupStackPane.getChildren().add(aReadingAnchorPane);
            try { Thread.sleep(200);
            } catch(InterruptedException ex) { Thread.currentThread().interrupt(); } 
            FadeTransition fadeInReadingPicture 
                    = new FadeTransition(Duration.seconds(2), aReadingLabel);
            fadeInReadingPicture.setFromValue(0.0);
            fadeInReadingPicture.setToValue(1.0);
            fadeInReadingPicture.play();
            fadeInReadingPicture.setOnFinished((ActionEvent ae2) -> {
                final int COVER_ART_DISPLAY_ITERATIONS = 8;
                final int COVER_ART_NODES_SIZE = 6;
                final double FADE_DURATION = 2.0;
                List<Work> randomAudiobooks 
                        = catalog.getRandomAudiobooks
                            (COVER_ART_DISPLAY_ITERATIONS * COVER_ART_NODES_SIZE,
                                    true);
                List<StackPane> audiobookStackPanes
                    = new ArrayList<>(COVER_ART_NODES_SIZE);
                for (int i=0; i < COVER_ART_NODES_SIZE; i++) {
                    StackPane stackPane = new StackPane();
                    stackPane.setOpacity(0);
                    audiobookStackPanes.add(stackPane);
                }
                int nodeCount = 0;
                for (Work audiobook : randomAudiobooks) {
                    if (nodeCount >= COVER_ART_NODES_SIZE) {
                        nodeCount = 0;
                    }
                    StackPane audiobookImageNode 
                        = new StackPane(getAudiobookImageNode
                            ((Audiobook)audiobook, IMAGE_SIDE_LENGTH, true, false));
                    audiobookImageNode.setAlignment(Pos.TOP_CENTER);
                    audiobookImageNode.setPadding(Insets.EMPTY);
                    audiobookStackPanes.get(nodeCount++)
                                        .getChildren().add(audiobookImageNode);
                }
                AnchorPane coverArtAnchorPane 
                        = new AnchorPane(audiobookStackPanes.toArray(new StackPane[0]));
                AnchorPane.setTopAnchor(audiobookStackPanes.get(0), SCENE_HEIGHT * 0.1);
                AnchorPane.setLeftAnchor(audiobookStackPanes.get(0), SCENE_WIDTH * 0.7);
                AnchorPane.setTopAnchor(audiobookStackPanes.get(1), SCENE_HEIGHT * 0.6);
                AnchorPane.setLeftAnchor(audiobookStackPanes.get(1), SCENE_WIDTH * 0.10);
                AnchorPane.setTopAnchor(audiobookStackPanes.get(2), SCENE_HEIGHT * 0.02);
                AnchorPane.setLeftAnchor(audiobookStackPanes.get(2), SCENE_WIDTH * 0.35);
                AnchorPane.setTopAnchor(audiobookStackPanes.get(3), SCENE_HEIGHT * 0.55);
                AnchorPane.setLeftAnchor(audiobookStackPanes.get(3), SCENE_WIDTH * 0.75);
                AnchorPane.setTopAnchor(audiobookStackPanes.get(4), SCENE_HEIGHT * 0.15);
                AnchorPane.setLeftAnchor(audiobookStackPanes.get(4), SCENE_WIDTH * 0.05);
                AnchorPane.setTopAnchor(audiobookStackPanes.get(5), SCENE_HEIGHT * 0.48);
                AnchorPane.setLeftAnchor(audiobookStackPanes.get(5), SCENE_WIDTH * 0.40);
                startupStackPane.getChildren().add(coverArtAnchorPane); 
                fadeInCoverArtNodes
                    (audiobookStackPanes, new FinalInteger(0), FADE_DURATION);
            });
        });
    }
    
    private void fadeInCoverArtNodes
            (List<StackPane> audiobookStackPanes, FinalInteger index, 
                                                    double fadeDuration) {
        if (buildCatalogShutdownCompleted) {
            return;
        }
        StackPane audiobookStackPane = audiobookStackPanes.get(index.get());
        FadeTransition fadeIn 
            = new FadeTransition(Duration.seconds(fadeDuration), audiobookStackPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        fadeIn.setOnFinished((ActionEvent ae) -> {
            if (index.increment() < audiobookStackPanes.size()) {
                fadeInCoverArtNodes(audiobookStackPanes, index, fadeDuration);
            } else {
                peelCoverArtNodes
                    (audiobookStackPanes, new FinalInteger(0), fadeDuration);
            }
        });
    }
    
    private void peelCoverArtNodes 
            (List<StackPane> audiobookStackPanes, FinalInteger index, double fadeDuration) {
        if (buildCatalogShutdownCompleted) {
            return;
        }
        StackPane audiobookStackPane = audiobookStackPanes.get(index.get());
        Node peelNode 
                = audiobookStackPane.getChildren()
                            .get(audiobookStackPane.getChildren().size()-1);
        FadeTransition fadeOut 
            = new FadeTransition(Duration.seconds(fadeDuration*0.5), audiobookStackPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.play();
        fadeOut.setOnFinished((ActionEvent ae1) -> {
            audiobookStackPane.getChildren().remove(peelNode);
            FadeTransition fadeIn 
                = new FadeTransition(Duration.seconds(fadeDuration*0.5), audiobookStackPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            fadeIn.setOnFinished((ActionEvent ae2) -> {
                if (index.increment() < audiobookStackPanes.size()) {
                    peelCoverArtNodes(audiobookStackPanes, index, fadeDuration);
                } else {
                    // keep "peeling" cover art nodes until only 1 left in each stack
                    if (audiobookStackPane.getChildren().size() > 1) {
                        peelCoverArtNodes
                            (audiobookStackPanes, new FinalInteger(0), fadeDuration);
                    }
                }
            });
        });
    }
            
    private Scene getMainScene () {
        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.setTop(getTopPane());
        mainBorderPane.setCenter(getCenterPane());
        mainStackPane = new StackPane(mainBorderPane);
        Scene scene = new Scene(mainStackPane, SCENE_WIDTH, SCENE_HEIGHT);
        return scene;
    }
    
    private Node getTopPane () {
        topPaneHBox = new HBox();
        topPaneHBox.setStyle(SKYBLUE_CSS);
        topPaneHBox.setPadding(INSETS_10);
        Node logoView = getLogoView();
        logoView.setOnMouseClicked((MouseEvent me) -> {
            Catalog.printMemoryUsage("User-prompted memory report", true);
        });
        topPaneHBox.getChildren().addAll
            (logoView, getPillButtons(), getQuickBrowseButtons(), getMenuAndSearchButtons());
        setTabPaneTitleLabel(Genre.class);
        VBox topPaneVBox = new VBox(topPaneHBox, tabPaneTitleLabel);
        return topPaneVBox;
    }
    
    private Node getLogoView () {
        ImageView logoView = new ImageView(LOGO_IMAGE);
        logoView.setFitHeight(BTN_HEIGHT * 3);
        logoView.setPreserveRatio(true);
        return logoView;
    }
    
    private Node getCenterPane () {
        /** TabPane used to prevent latency when switching between ScrollPanes
         * (forces rendering of all panes at startup time). */
        centerTabPane = new TabPane();
        centerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // hide tabs (leaves slight, barely discernable "nubs" at bottom of pane)
        centerTabPane.setTabMaxHeight(0);
        centerTabPane.setTabMaxWidth(0);
        centerTabPane.setSide(Side.BOTTOM);
        /* suppress default arrow-scrolling through tabs of TabPane */
        centerTabPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent ke) -> {
            switch (ke.getCode()) {
                case LEFT: case KP_LEFT: case RIGHT: case KP_RIGHT: 
                case UP: case KP_UP: case DOWN: case KP_DOWN:
                    ke.consume();
            }
        });
        
        Tab genreTab = new Tab();
        genreTab.setContent(getAudiobookAccordionsScrollPane(Genre.class));
        Tab authorTab = new Tab();
        authorTab.setContent(getAlphabeticAccordionsScrollPane(Author.class));
        Tab readerTab = new Tab();
        readerTab.setContent(getAlphabeticAccordionsScrollPane(Reader.class));
        Tab languageTab = new Tab();
        languageTab.setContent(getAudiobookAccordionsScrollPane(Language.class));
        Tab myListTab = new Tab();
        myListTab.setContent(getMyListAccordionsScrollPane());
        myListTab.setId("myListTab");
        centerTabPane.getTabs().addAll
                (genreTab, authorTab, readerTab, languageTab, myListTab);
        
        // set defaults
        centerTabPane.getSelectionModel().select(genreTab); // default selected tab
        selectedOrderClass = Title.class; // default order

        tbGenre.setOnAction((ActionEvent e) -> {
            resetUpperToggleButtonFonts();
            tbGenre.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            setTabPaneTitleLabel(Genre.class);
            centerTabPane.getSelectionModel().select(genreTab);
        });        
        tbAuthor.setOnAction((ActionEvent e) -> {
            resetUpperToggleButtonFonts();
            tbAuthor.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            setTabPaneTitleLabel(Author.class);
            centerTabPane.getSelectionModel().select(authorTab);
        });        
        tbReader.setOnAction((ActionEvent e) -> {
            resetUpperToggleButtonFonts();
            tbReader.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            setTabPaneTitleLabel(Reader.class);
            centerTabPane.getSelectionModel().select(readerTab);
        });        
        tbLanguage.setOnAction((ActionEvent e) -> {
            resetUpperToggleButtonFonts();
            tbLanguage.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            setTabPaneTitleLabel(Language.class);
            centerTabPane.getSelectionModel().select(languageTab);
        });        
        tbMyList.setOnAction((ActionEvent e) -> {
            resetUpperToggleButtonFonts();
            tbMyList.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            setTabPaneTitleLabel(MyList.class);
            centerTabPane.getSelectionModel().select(myListTab);
        });
        
        tbTitle.setOnAction((ActionEvent e) -> {
            resetLowerToggleButtonFonts();
            tbTitle.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            selectedOrderClass = Title.class;
            findExpandedPanes(ExpandedPanesOption.REFRESH,false);
        });        
        tbNewest.setOnAction((ActionEvent e) -> {
            resetLowerToggleButtonFonts();
            tbNewest.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            selectedOrderClass = PublicationDate.class;
            findExpandedPanes(ExpandedPanesOption.REFRESH,false);
        });    
        /*
        tbPopularity.setOnAction((ActionEvent e) -> {
            resetLowerToggleButtonFonts();
            tbPopularity.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            selectedOrderClass = DownloadsPerDay.class;
            findExpandedPanes(ExpandedPanesOption.REFRESH,false);
        });   
        */
        tbDownloads.setOnAction((ActionEvent e) -> {
            resetLowerToggleButtonFonts();
            tbDownloads.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
            selectedOrderClass = Downloads.class;
            findExpandedPanes(ExpandedPanesOption.REFRESH,false);
        });        
        return centerTabPane;
    }
    
    private void setTabPaneTitleLabel (Class submittedClass) {
        if (MyList.class.isAssignableFrom(submittedClass)) {
            tabPaneTitleLabel.setText("MY LIST");
        } else {
            tabPaneTitleLabel.setText("Audiobooks by " + submittedClass.getSimpleName());
        }
        tabPaneTitleLabel.setFont(BOLD_BIGGEST_FONT);
        tabPaneTitleLabel.setPadding(new Insets(0,0,0,10));
        tabPaneTitleLabel.setBackground(GRAY_BACKGROUND);
        tabPaneTitleLabel.setMaxWidth(Double.MAX_VALUE);
    }
    
    private void resetUpperToggleButtonFonts () {
        tbGenre.setFont(Font.getDefault());
        tbAuthor.setFont(Font.getDefault());
        tbReader.setFont(Font.getDefault());
        tbLanguage.setFont(Font.getDefault());
        tbMyList.setFont(Font.getDefault());
    }
    
    private void resetLowerToggleButtonFonts () {
        tbTitle.setFont(Font.getDefault());
        tbNewest.setFont(Font.getDefault());
        //tbPopularity.setFont(Font.getDefault());
        tbDownloads.setFont(Font.getDefault());
    }
    
    private int findExpandedPanes (ExpandedPanesOption option) {
        return findExpandedPanes (option, false, null);
    }
    
    private int findExpandedPanes (ExpandedPanesOption option, 
                                                    TitledPane excludeTitledPane) {
        return findExpandedPanes(option, false, excludeTitledPane);
    }
    
    private int findExpandedPanes
            (ExpandedPanesOption option, boolean myListTabOnly) {
        return findExpandedPanes(option, myListTabOnly, null);
    }
    
    /** finds and optionally refreshes (closes and reExpands) all expanded panes */
    private int findExpandedPanes
            (ExpandedPanesOption option, boolean myListTabOnly, 
                                                TitledPane excludeTitledPane) {
        int expandedPaneCount = 0;
        int simplePaneCount = 0;
        int innerPaneCount = 0;
        for (Tab tab : centerTabPane.getTabs()) {
            if (myListTabOnly 
                    && (tab.getId() == null
                            || !tab.getId().equals("myListTab"))) {
                continue;
            }
            ScrollPane scrollPane = (ScrollPane)tab.getContent();
            VBox vBox = (VBox)scrollPane.getContent();
            for (Object object : vBox.getChildren()) {
                if (!Accordion.class.isAssignableFrom(object.getClass())) {
                    continue;
                }
                Accordion accordion = (Accordion)object;
                TitledPane expandedPane = accordion.getExpandedPane();
                if (expandedPane == null
                        || expandedPane.equals(excludeTitledPane)) {
                    continue;
                }
                Node titledPaneContent = expandedPane.getContent();
                if (VBox.class.isAssignableFrom
                                    (titledPaneContent.getClass())) {
                    // following "if" is v1.5.1 fix for start and end titles appearing 
                    // inappropriately when "Order by" changed from title to something else
                    if (option.equals(ExpandedPanesOption.REFRESH)) {
                        // close and reExpand to refresh
                        expandedPane.setExpanded(false);
                        expandedPane.setExpanded(true);
                    }
                    VBox innerVBox = (VBox)titledPaneContent;
                    for (Object innerObj : innerVBox.getChildren()) {
                        Accordion innerAccordion = (Accordion)innerObj;
                        TitledPane innerExpandedPane 
                                = innerAccordion.getExpandedPane();
                        if (innerExpandedPane == null
                                || innerExpandedPane.equals(excludeTitledPane)) {
                            continue;
                        }
                        assert (StackPane.class.isAssignableFrom
                                    (innerExpandedPane.getContent().getClass()));
                        innerPaneCount++;
                        expandedPaneCount++;
                        if (option.equals(ExpandedPanesOption.REFRESH)) {
                            // close and reExpand to refresh
                            innerExpandedPane.setExpanded(false);
                            innerExpandedPane.setExpanded(true);
                        } else if (option.equals(ExpandedPanesOption.CLOSE)) {
                            innerExpandedPane.setExpanded(false);
                        }
                    }
                } else if (StackPane.class.isAssignableFrom
                                                (titledPaneContent.getClass())){
                    simplePaneCount++;
                    expandedPaneCount++;
                    if (option.equals(ExpandedPanesOption.REFRESH)) {
                        // close and reExpand the TitledPane to refresh
                        expandedPane.setExpanded(false);
                        expandedPane.setExpanded(true);
                    } else if (option.equals(ExpandedPanesOption.CLOSE)) {
                        expandedPane.setExpanded(false);
                    }
                }
            }
        }
        return expandedPaneCount;
    }
    
    private Node getPillButtons () {
        Label topLabel = new Label("CATEGORIZE BY");
        topLabel.setFont(BOLD_FONT);
        topLabel.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        HBox hBoxForTopLabel = new HBox();
        hBoxForTopLabel.setAlignment(Pos.CENTER);
        hBoxForTopLabel.setPadding(INSETS_10);
        hBoxForTopLabel.getChildren().add(topLabel);

        Label bottomLabel = new Label("ORDER ROWS BY");
        bottomLabel.setFont(BOLD_FONT);
        bottomLabel.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        HBox hBoxForBottomLabel = new HBox();
        hBoxForBottomLabel.setAlignment(Pos.CENTER);
        hBoxForBottomLabel.setPadding(INSETS_10);
        hBoxForBottomLabel.getChildren().add(bottomLabel);

        tbGenre = new ToggleButton("Genre");
        tbGenre.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbGenre.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbGenre.getStyleClass().add("left-pill");
        tbAuthor = new ToggleButton("Author");
        tbAuthor.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbAuthor.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbAuthor.getStyleClass().add("center-pill");
        tbReader = new ToggleButton("Reader");
        tbReader.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbReader.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbReader.getStyleClass().add("center-pill");
        tbReader.setTooltip
            (new Tooltip("Find audiobooks recorded by your favorite LibriVox readers."));
        tbLanguage = new ToggleButton("Language");
        tbLanguage.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbLanguage.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbLanguage.getStyleClass().add("center-pill");
        tbLanguage.setTooltip
            (new Tooltip("Find audiobooks recorded in many languages."));
        tbMyList = new ToggleButton("MY LIST");
        tbMyList.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbMyList.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbMyList.getStyleClass().add("right-pill");
        tbMyList.setTooltip
            (new Tooltip("MY LIST of Authors, Readers, and Audiobooks."));
        
        tbTitle = new ToggleButton("Title");
        tbTitle.setTooltip(new Tooltip("Alphabetical order by Title"));
        tbTitle.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbTitle.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbTitle.getStyleClass().add("left-pill");
        tbNewest = new ToggleButton("Newest");
        tbNewest.setTooltip
                (new Tooltip("NEWEST determined by Date of Publication"));
        tbNewest.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbNewest.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbNewest.getStyleClass().add("center-pill");
        /*
        tbPopularity = new ToggleButton("Popularity");
        tbPopularity.setTooltip
                (new Tooltip("POPULARITY determined by Downloads PER DAY"
                                + " (since audiobook published)"));
        tbPopularity.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbPopularity.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbPopularity.getStyleClass().add("center-pill");
        */
        tbDownloads = new ToggleButton("Downloads");
        tbDownloads.setTooltip
                (new Tooltip("Determined by Total Number of Downloads"));
        tbDownloads.setMinSize(BTN_WIDTH, BTN_HEIGHT);
        tbDownloads.setMaxSize(BTN_WIDTH, BTN_HEIGHT);
        tbDownloads.getStyleClass().add("right-pill");
 
        final ToggleGroup topGroup = new ToggleGroup();
        tbGenre.setToggleGroup(topGroup);
        tbAuthor.setToggleGroup(topGroup);
        tbReader.setToggleGroup(topGroup);
        tbLanguage.setToggleGroup(topGroup);
        tbMyList.setToggleGroup(topGroup);
        
        final ToggleGroup bottomGroup = new ToggleGroup();
        tbTitle.setToggleGroup(bottomGroup);
        tbNewest.setToggleGroup(bottomGroup);
        //tbPopularity.setToggleGroup(bottomGroup);
        tbDownloads.setToggleGroup(bottomGroup);
        
        // pre-select the first button for startup
        topGroup.selectToggle(tbGenre);
        tbGenre.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
        bottomGroup.selectToggle(tbTitle);
        tbTitle.setFont(BOLD_SLIGHTLY_BIGGER_FONT);
 
        // enforce rule that one ToggleButton MUST be selected at any time 
        topGroup.selectedToggleProperty().addListener
                ((ObservableValue<? extends Toggle> observable, 
                                Toggle oldValue, Toggle newValue) -> {
            if (newValue == null) {
                topGroup.selectToggle(oldValue);
            }
        });
        bottomGroup.selectedToggleProperty().addListener
                ((ObservableValue<? extends Toggle> observable, 
                                Toggle oldValue, Toggle newValue) -> {
            if (newValue == null) {
                bottomGroup.selectToggle(oldValue);
            }
        });
 
        HBox pillButtonHBoxTop = new HBox();
        pillButtonHBoxTop.setAlignment(Pos.CENTER);
        pillButtonHBoxTop.getChildren().addAll
            (hBoxForTopLabel, tbGenre, tbAuthor, tbReader, tbLanguage, tbMyList);
        pillButtonHBoxTop.getStylesheets()
            .add(LeBrowser.class.getResource(PILL_BUTTON_CSS_FILE).toExternalForm());
 
        HBox pillButtonHBoxBottom = new HBox();
        pillButtonHBoxBottom.setAlignment(Pos.CENTER);
        pillButtonHBoxBottom.getChildren().addAll
            (hBoxForBottomLabel, tbTitle, tbNewest, /*tbPopularity,*/ tbDownloads);
        pillButtonHBoxBottom.getStylesheets()
            .add(LeBrowser.class.getResource(PILL_BUTTON_CSS_FILE).toExternalForm());
        pillButtonHBoxBottom.setPadding(new Insets(0,8,0,0));

        VBox pillButtonVBox = new VBox();
        pillButtonVBox.getChildren().
                addAll(pillButtonHBoxTop, pillButtonHBoxBottom);
        pillButtonVBox.setStyle(SKYBLUE_CSS);
        pillButtonVBox.setAlignment(Pos.BASELINE_RIGHT);
        StackPane pillButtonStackPane = new StackPane(pillButtonVBox);
        pillButtonStackPane.setAlignment(Pos.CENTER);
        pillButtonStackPane.setPadding(new Insets(6, 45, 6, 30));
        return pillButtonStackPane;
    }

    private Node getQuickBrowseButtons() {
        Button newestWorksButton = new Button ("NEWEST\nWORKS");
        newestWorksButton.setTooltip
            (new Tooltip("Click here to see the latest offerings " 
                                + "from the entire LibriVox catalog."));
        newestWorksButton.setTextAlignment(TextAlignment.CENTER);
        newestWorksButton.setOnMouseClicked
            (getOverlayPaneHandler(OverlayPaneOption.NEWEST, null, 0, null, null));
        StackPane newestWorksStackPane = new StackPane(newestWorksButton);
        newestWorksStackPane.setPadding(new Insets(1));
        Button luckyButton = new Button("RANDOM\nBROWSE");
        luckyButton.setTooltip
            (new Tooltip("Click here to see a random selection of audiobooks."));
        luckyButton.setTextAlignment(TextAlignment.CENTER);
        luckyButton.setOnMouseClicked
            (getOverlayPaneHandler(OverlayPaneOption.RANDOM, null, 0, null, null));
        StackPane luckyStackPane = new StackPane(luckyButton);
        luckyStackPane.setPadding(new Insets(1));
        HBox quickBrowseHBox = new HBox(newestWorksStackPane,luckyStackPane);
        Label quickBrowseLabel = new Label("Quick Browse Options", quickBrowseHBox);
        quickBrowseLabel.setContentDisplay(ContentDisplay.BOTTOM);
        quickBrowseLabel.setPadding(new Insets(5));
        quickBrowseLabel.setFont(SMALLEST_FONT);
        quickBrowseLabel.setStyle
            ("-fx-border-color: black; -fx-border-width: 2px; " 
                                + "-fx-background-color: deepskyblue;");
        quickBrowseLabel.setMinWidth(140);
        StackPane quickBrowseStackPane = new StackPane(quickBrowseLabel);
        quickBrowseStackPane.setPadding(new Insets(6, 5, 6, 5));
        return quickBrowseStackPane;
    }
    
    private Node getMenuAndSearchButtons () {
        final double MB_SIDE_LENGTH = BTN_HEIGHT * 1.2;
        ImageView menuImageView = new ImageView();
        if (Catalog.getLatestVersion().isEmpty() 
                            || V_R_SM.equals(Catalog.getLatestVersion())) {
            menuImageView.setImage(MENU_BUTTON_IMAGE);
            menuImageView.setFitWidth(MB_SIDE_LENGTH * 0.4);
        } else {
            menuImageView.setImage(MENU_BUTTON_RED_ASTERISK_IMAGE);
            menuImageView.setFitWidth(MB_SIDE_LENGTH * 0.6);
        }
        menuImageView.setPreserveRatio(true);
        Button mb = new Button();
        mb.setGraphic(menuImageView);
        mb.setPadding(new Insets(2));
        mb.setMinSize(MB_SIDE_LENGTH, MB_SIDE_LENGTH);
        mb.setMaxSize(MB_SIDE_LENGTH, MB_SIDE_LENGTH);
        MenuItem aboutMenuItem = new MenuItem("About/Contact");
        aboutMenuItem.setStyle(BOLD_TEXT_CSS);
        aboutMenuItem.setOnAction((ActionEvent e) -> {
            showAboutWindow();
        });
        MenuItem tutorialMenuItem = new MenuItem("Tutorial");
        tutorialMenuItem.setStyle(BOLD_TEXT_CSS);
        tutorialMenuItem.setOnAction
            ((ActionEvent ae)->{getHostServices().showDocument(TUTORIAL_URL_STRING);});
        MenuItem settingsMenuItem = new MenuItem("Settings");
        settingsMenuItem.setStyle(BOLD_TEXT_CSS);
        settingsMenuItem.setOnAction
            ((ActionEvent e) -> { showSettingsWindow(); });
        MenuItem problemMenuItem = new MenuItem("Report problem");
        problemMenuItem.setStyle(BOLD_TEXT_CSS);
        problemMenuItem.setOnAction
            ((ActionEvent ae)->{
                getHostServices().showDocument
                    (Catalog.getProblemReportUrlString
                            (System.getProperty("os.name") + ",ver=" 
                                    + System.getProperty("os.version")
                                    + ",LE=" + V_R_SM));
            });
        MenuItem commentMenuItem = new MenuItem("Leave comment");
        commentMenuItem.setStyle(BOLD_TEXT_CSS);
        commentMenuItem.setOnAction
            ((ActionEvent ae)->{getHostServices().showDocument(COMMENTS_URL_STRING);});
        MenuItem latestReleaseMenuItem 
            = new MenuItem("*Get software update: v" + Catalog.getLatestVersion());
        latestReleaseMenuItem.setStyle(RED_BOLD_TEXT_CSS);
        latestReleaseMenuItem.setOnAction
            ((ActionEvent ae)->{getHostServices().showDocument
                                                (SOFTWARE_UPDATE_URL_STRING);});
        
        ContextMenu mbContextMenu 
            = new ContextMenu(aboutMenuItem, tutorialMenuItem, 
                    problemMenuItem, commentMenuItem, settingsMenuItem);
        if (!Catalog.getLatestVersion().isEmpty() 
                            && !V_R_SM.equals(Catalog.getLatestVersion())) {
            mbContextMenu.getItems().add(latestReleaseMenuItem);
        }
        // Note that mouse event registers on right click as well as left.
        mb.setOnMouseClicked((MouseEvent me) -> {
            // mouseClick toggles mbContextMenu on and off
            if (mbContextMenu.isShowing()) {
                mbContextMenu.hide();
            } else {
                mbContextMenu.show(mb, Side.LEFT, 0, 0);
            }
            me.consume(); // suppresses default ContextMenu behavior
        });
        
        ImageView searchImageView = new ImageView(SEARCH_BUTTON_IMAGE);
        searchImageView.setFitWidth(MB_SIDE_LENGTH * 0.5);
        searchImageView.setPreserveRatio(true);
        Button sb = new Button();
        sb.setGraphic(searchImageView);
        sb.setPadding(new Insets(2));
        sb.setMinSize(MB_SIDE_LENGTH, MB_SIDE_LENGTH);
        sb.setMaxSize(MB_SIDE_LENGTH, MB_SIDE_LENGTH);
        sb.setOnMouseClicked
            (getOverlayPaneHandler(OverlayPaneOption.SEARCH, null, 0, null, null));
        
        VBox menuSearchButtonsVBox = new VBox(mb, sb);
        menuSearchButtonsVBox.setPadding(new Insets (9,0,0,30));
        return menuSearchButtonsVBox;
    }
    
    private void showAboutWindow() {
        final double ABOUT_WINDOW_WIDTH = 620;
        BorderPane aboutBorderPane = new BorderPane();
        Scene aboutScene = new Scene(aboutBorderPane);
        
        HBox aboutTopHBox = new HBox(getLogoView(),getProductLabel());
        aboutTopHBox.setPadding(INSETS_10);
        aboutTopHBox.setStyle(SKYBLUE_CSS);
        Text aboutText1 
                = new Text("This is free/libre open-source software (FLOSS) from " 
                        + "CommonVox.org, for use by anyone who wishes to " 
                        + "explore the public domain M4B audiobook offerings " 
                        + "produced by the volunteers at LibriVox.org.\n\n"
                        + "LibriVox EXPLORER focuses on presenting the "
                        + "M4B-formatted (i.e., iTunes-compatible) audiobook "
                        + "offerings of LibriVox, and providing for easy, "
                        + "automatic importation of any audiobook's M4B file(s) "
                        + "into iTunes. Or if you prefer, M4B files may simply "
                        + "be downloaded directly to a folder of your choosing "
                        + "for playback using any other M4B-compatible media player.\n");
        aboutText1.setFont(BIGGER_FONT);
        aboutText1.setWrappingWidth(ABOUT_WINDOW_WIDTH);
        Label tutorialLinkLabel = new Label("Click here for a brief tutorial.");
        tutorialLinkLabel.setFont(BOLD_BIGGER_FONT);
        tutorialLinkLabel.setUnderline(true);
        tutorialLinkLabel.setOnMouseClicked
            ((MouseEvent me)->{getHostServices().showDocument(TUTORIAL_URL_STRING);});
        tutorialLinkLabel.setOnMouseEntered
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.HAND);});
        tutorialLinkLabel.setOnMouseExited
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.DEFAULT);});
        Label commentsLinkLabel = new Label("Click here to leave a comment.");
        commentsLinkLabel.setFont(BOLD_BIGGER_FONT);
        commentsLinkLabel.setUnderline(true);
        commentsLinkLabel.setOnMouseClicked
            ((MouseEvent me)->{getHostServices().showDocument(COMMENTS_URL_STRING);});
        commentsLinkLabel.setOnMouseEntered
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.HAND);});
        commentsLinkLabel.setOnMouseExited
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.DEFAULT);});
        Label problemsLinkLabel = new Label("Click here to report a problem.");
        problemsLinkLabel.setFont(BOLD_BIGGER_FONT);
        problemsLinkLabel.setUnderline(true);
        problemsLinkLabel.setOnMouseClicked
            ((MouseEvent me)->{
                getHostServices().showDocument
                    (Catalog.getProblemReportUrlString
                            (System.getProperty("os.name") + ", version=" 
                                    + System.getProperty("os.version")));
            });
        problemsLinkLabel.setOnMouseEntered
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.HAND);});
        problemsLinkLabel.setOnMouseExited
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.DEFAULT);});
        Text space = new Text("");
        Text contactText1 
                = new Text("For more information of any kind regarding this "
                        + "application*, please...");
        contactText1.setFont(ITALIC_BIGGER_FONT);
        Text websiteText = new Text("                -- go to the website:  ");
        websiteText.setFont(BIGGER_FONT);
        Label websiteLinkLabel = new Label(COMMONVOX_URL_STRING);
        websiteLinkLabel.setUnderline(true);
        websiteLinkLabel.setFont(BOLD_BIGGER_FONT);
        websiteLinkLabel.setOnMouseClicked
            ((MouseEvent me)->{getHostServices().showDocument(COMMONVOX_URL_STRING);});
        websiteLinkLabel.setOnMouseEntered
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.HAND);});
        websiteLinkLabel.setOnMouseExited
            ((MouseEvent me)->{aboutScene.setCursor(Cursor.DEFAULT);});
        HBox websiteLinkHBox = new HBox(websiteText,websiteLinkLabel);
        Text emailText = new Text("                -- or e-mail:  ");
        emailText.setFont(BIGGER_FONT);
        Label emailAddressLabel = new Label("info@commonvox.org");
        emailAddressLabel.setFont(BOLD_BIGGER_FONT);
        HBox emailAddressHBox = new HBox(emailText,emailAddressLabel);
        VBox contactVBox = new VBox(contactText1, websiteLinkHBox, emailAddressHBox);
        contactVBox.setMaxWidth(440);
        contactVBox.setPadding(new Insets(5,10,5,10));
        contactVBox.setStyle
            ("-fx-border-color: black; -fx-border-width: 2px; ");
        StackPane contactStackPane = new StackPane(contactVBox);
        Text aboutText2
                = new Text("\n*Please do NOT seek information about this software "
                        + "from LibriVox.org!  (The volunteer administrators and "
                        + "readers of LibriVox are busy creating new and "
                        + "exciting audiobook productions for your listening "
                        + "pleasure, and have nothing to do with any aspect of "
                        + "the production, distribution, or maintenance of "
                        + "this software!)\n");
        aboutText2.setFont(ITALIC_FONT);
        aboutText2.setWrappingWidth(ABOUT_WINDOW_WIDTH);
        Text aboutText3
                = new Text("This software is made freely available under the "
                        + "GNU General Public License, version 3, as published "
                        + "by the Free Software Foundation.\n"
                        + "iTunes is a registered trademark of Apple, Inc.\n"
                        + "All search facilities powered by Google & Bing.\n"
                        + "CEFR-Spoken_Production symbol (CommonVox logo) "
                        + "-- courtesy of Gnak, openclipart.org\n"
                        + "Background artwork during startup: \"A Reading\" by "
                        + "Thomas Wilmer Dewing, 1897 -- courtesy of "
                        + "Smithsonian American Art Museum.\n"
                        + "All software design and engineering by Daniel Vimont.");
        aboutText3.setFont(SMALLEST_FONT);
        aboutText3.setWrappingWidth(ABOUT_WINDOW_WIDTH);
        aboutText3.setTextAlignment(TextAlignment.CENTER);
        VBox aboutTextVBox 
                = new VBox(aboutText1, tutorialLinkLabel, commentsLinkLabel,
                        problemsLinkLabel, space,
                        contactStackPane, aboutText2,
                    getCenteredSeparator(ABOUT_WINDOW_WIDTH*0.65,ABOUT_WINDOW_WIDTH),
                                                        aboutText3);
        aboutTextVBox.setPadding(new Insets(20));
        
        aboutBorderPane.setTop(aboutTopHBox);
        aboutBorderPane.setCenter(aboutTextVBox);
        Stage aboutStage = new Stage();
        aboutStage.initStyle(StageStyle.UTILITY);
        aboutStage.setTitle("About/Contact");
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        aboutStage.setResizable(false);
        aboutStage.initOwner(mainStage);
        aboutStage.setScene(aboutScene);
        aboutStage.show();
    }
    
    private void showSettingsWindow() {
        final double OPTIONS_WINDOW_WIDTH = 620;
        BorderPane settingsBorderPane = new BorderPane();
        Scene optionsScene = new Scene(settingsBorderPane);
        
        HBox showTopHBox = new HBox(getLogoView(),getProductLabel());
        showTopHBox.setPadding(INSETS_10);
        showTopHBox.setStyle(SKYBLUE_CSS);
        
        Label settingsHeaderLabel = new Label("SETTINGS");
        settingsHeaderLabel.setFont(BOLD_BIGGEST_FONT);
        
        Label coverArtDisplaySettingsLabel = new Label("Cover Art Display Settings");
        coverArtDisplaySettingsLabel.setFont(BOLD_BIGGER_FONT);
        coverArtDisplaySettingsLabel.setUnderline(true);
        
        ToggleGroup tg = new ToggleGroup();
        RadioButton rb1 = new RadioButton
            ("Enable download & display of higher definition cover art (default).");
        rb1.setFont(BOLD_FONT);
        rb1.setToggleGroup(tg);
        rb1.setOnMouseClicked((MouseEvent me)->{
            PersistedAppSettings.setCoverArtDisplaySetting
                (PersistedAppSettings.CoverArtDisplaySetting.ENABLE_ALL);
        });

        RadioButton rb2 = new RadioButton
                ("Disable download & display of higher definition cover art.");
        rb2.setFont(BOLD_FONT);
        rb2.setToggleGroup(tg);
        rb2.setOnMouseClicked((MouseEvent me)->{
            PersistedAppSettings.setCoverArtDisplaySetting
                (PersistedAppSettings.CoverArtDisplaySetting.DISABLE_DOWNLOAD);
        });
        RadioButton rb3 = new RadioButton("Disable display of all cover art.");
        rb3.setFont(BOLD_FONT);
        rb3.setToggleGroup(tg);
        rb3.setOnMouseClicked((MouseEvent me)->{
            PersistedAppSettings.setCoverArtDisplaySetting
                (PersistedAppSettings.CoverArtDisplaySetting.DISABLE_ALL);
        });
        VBox radioButtonVBox = new VBox(rb1,rb2,rb3);
        radioButtonVBox.setSpacing(5);
        radioButtonVBox.setPadding(INSETS_10);
        
        Label rowSettingsLabel = new Label("Audiobook Row Settings");
        rowSettingsLabel.setFont(BOLD_BIGGER_FONT);
        rowSettingsLabel.setUnderline(true);
        CheckBox rowSettingsCB = new CheckBox("Automatically assure that only "
                                + "one Audiobook row (accordion pane) is open at a time.*");
        rowSettingsCB.setFont(BOLD_FONT);
        rowSettingsCB.setPadding(new Insets(10,10,0,10));
        Label rowSettingsCbFootnoteLabel 
            = new Label("*may be advisable if LibriVox EXPLORER is running slowly");
        rowSettingsCbFootnoteLabel.setFont(SMALLER_FONT);
        rowSettingsCbFootnoteLabel.setPadding(new Insets(0,10,10,0));
        rowSettingsCbFootnoteLabel.setMinWidth(OPTIONS_WINDOW_WIDTH);
        rowSettingsCbFootnoteLabel.setTextAlignment(TextAlignment.RIGHT);
        rowSettingsCbFootnoteLabel.setAlignment(Pos.CENTER_RIGHT);
        VBox rowSettingsCbVBox = new VBox(rowSettingsCB, rowSettingsCbFootnoteLabel);
        rowSettingsCB.setOnMouseClicked((MouseEvent me)->{
            if (rowSettingsCB.isSelected()) {
                PersistedAppSettings.setRowAutocloseSetting
                    (PersistedAppSettings.RowAutocloseSetting.AUTOCLOSE_TRUE);
            } else {
                PersistedAppSettings.setRowAutocloseSetting
                    (PersistedAppSettings.RowAutocloseSetting.AUTOCLOSE_FALSE);
            }
        });
        
        Label bookmarkSettingsLabel = new Label("Bookmarking Settings");
        bookmarkSettingsLabel.setFont(BOLD_BIGGER_FONT);
        bookmarkSettingsLabel.setUnderline(true);
        CheckBox bookmarkSettingsCB 
                    = new CheckBox("Disable bookmarking of current location when "
                                        + "listening to audio files.");
        bookmarkSettingsCB.setFont(BOLD_FONT);
        bookmarkSettingsCB.setPadding(new Insets(10,10,0,10));
        bookmarkSettingsCB.setOnMouseClicked((MouseEvent me)->{
            if (bookmarkSettingsCB.isSelected()) {
                PersistedAppSettings.setBookmarkSuppressSetting
                    (PersistedAppSettings.BookmarkSuppressSetting.BOOKMARKS_SUPPRESS);
            } else {
                PersistedAppSettings.setBookmarkSuppressSetting
                    (PersistedAppSettings.BookmarkSuppressSetting.BOOKMARKS_ALLOW);
            }
        });
        
        VBox settingsDetailsVBox 
                = new VBox(coverArtDisplaySettingsLabel, radioButtonVBox,
                                rowSettingsLabel, rowSettingsCbVBox,
                                bookmarkSettingsLabel, bookmarkSettingsCB);
        settingsDetailsVBox.setPadding(new Insets(15));
        settingsDetailsVBox.setStyle
            ("-fx-border-color: black; -fx-border-width: 2px; ");

        VBox settingsVBox 
                = new VBox(settingsHeaderLabel, settingsDetailsVBox);
        settingsVBox.setPadding(new Insets(20));
        settingsBorderPane.setTop(showTopHBox);
        settingsBorderPane.setCenter(settingsVBox);
        Stage settingsStage = new Stage();
        settingsStage.initStyle(StageStyle.UTILITY);
        settingsStage.setTitle("Settings");
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.setResizable(false);
        settingsStage.initOwner(mainStage);
        settingsStage.setScene(optionsScene);
        switch (PersistedAppSettings.getCoverArtDisplaySetting()) {
            case ENABLE_ALL:
                rb1.setSelected(true);
                rb1.requestFocus();
                break;
            case DISABLE_DOWNLOAD:
                rb2.setSelected(true);
                rb2.requestFocus();
                break;
            case DISABLE_ALL:
                rb3.setSelected(true);
                rb3.requestFocus();
                break;
        }
        switch (PersistedAppSettings.getRowAutocloseSetting()) {
            case AUTOCLOSE_FALSE:
                rowSettingsCB.setSelected(false);
                break;
            case AUTOCLOSE_TRUE:
                rowSettingsCB.setSelected(true);
                break;
        }
        switch (PersistedAppSettings.getBookmarkSuppressSetting()) {
            case BOOKMARKS_ALLOW:
                bookmarkSettingsCB.setSelected(false);
                break;
            case BOOKMARKS_SUPPRESS:
                bookmarkSettingsCB.setSelected(true);
                break;
        }
        
        settingsStage.show();
    }
    
    private Node getProductLabel() {
        Label productLabel = new Label(PRODUCT_NAME + " v" + V_R_SM);
        productLabel.setFont(BOLD_BIGGEST_FONT);
        productLabel.setPadding(new Insets(10,10,10,20));
        StackPane productLabelStackPane = new StackPane(productLabel);
        return productLabelStackPane;
    }
    
    private ScrollPane getAlphabeticAccordionsScrollPane
            (Class<? extends IndexedKey> indexedKeyClass) {
                
        VBox alphabetVBox = new VBox();
        try {
            List<IndexedKey> indexedKeyList 
                = catalog.getIndexedKeyValueList(indexedKeyClass);
            VBox audiobookVBox = new VBox();
            String letterAccordionTitle;
            String firstChar;
            String previousFirstChar = "";
            boolean startedAlphabet = false;
            boolean pastLetterZ = false;
            for (IndexedKey indexedKeyObject : indexedKeyList) {
                if (indexedKeyObject.getKeyItem().isEmpty()) {
                    continue;
                } else {
                    firstChar 
                        = indexedKeyObject.getKeyItem().substring(0,1).toUpperCase();
                }
                if (firstChar.compareTo("A") >= 0) {
                    startedAlphabet = true;
                }
                if (previousFirstChar.compareTo("Z") > 0) {
                    pastLetterZ = true;
                }

                if ( !previousFirstChar.isEmpty()
                        && !firstChar.equals(previousFirstChar) 
                        && startedAlphabet && !pastLetterZ) {
                    if (previousFirstChar.compareTo("A") < 0) {
                        letterAccordionTitle = "Before A";
                    } else {
                        letterAccordionTitle = previousFirstChar;
                    }
                    alphabetVBox.getChildren().add
                        (getLetterAccordion(letterAccordionTitle, audiobookVBox));

                    audiobookVBox = new VBox();
                }
                // ReaderWorksOption added v1.3.3
                if (Reader.class.isAssignableFrom(indexedKeyClass)) {
                    if (catalog.getWorks(Audiobook.class, indexedKeyObject, 
                            Catalog.ReaderWorksOption.SOLO_WORKS).size() > 0) {
                        audiobookVBox.getChildren().add
                            (getAudiobookAccordion
                                (new AudiobookRowParms(indexedKeyObject, 
                                            Catalog.ReaderWorksOption.SOLO_WORKS)));
                    }                    
                    if (catalog.getWorks(Audiobook.class, indexedKeyObject,
                            Catalog.ReaderWorksOption.GROUP_WORKS).size() > 0) {
                        audiobookVBox.getChildren().add
                            (getAudiobookAccordion
                                (new AudiobookRowParms(indexedKeyObject, 
                                            Catalog.ReaderWorksOption.GROUP_WORKS)));
                    }                    
                } else {
                    audiobookVBox.getChildren().add
                        (getAudiobookAccordion(new AudiobookRowParms(indexedKeyObject)));
                }
                previousFirstChar = firstChar;
            }
            if (pastLetterZ) {
                letterAccordionTitle = "On Beyond Z";
            } else {
                letterAccordionTitle = previousFirstChar;
            }
            alphabetVBox.getChildren().add
                    (getLetterAccordion(letterAccordionTitle, audiobookVBox));
        } 
        catch (InvalidIndexedCollectionQueryException e) {
            alphabetVBox.getChildren().add
                    (getExceptionLabel(e, indexedKeyClass.getSimpleName()));
        }
        
        ScrollPane alphabetScrollPane = new ScrollPane();
        alphabetScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        alphabetScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        alphabetScrollPane.setFitToWidth(true);
        alphabetScrollPane.setContent(alphabetVBox);
        return alphabetScrollPane;
    }
            
    private Accordion getLetterAccordion
            (String letterAccordionTitle, VBox audiobookVBox) {
        Accordion letterAccordion = new Accordion();
        TitledPane titledPane 
            = new TitledPane(letterAccordionTitle, 
                                new Label("VBox of Accordions goes here."));
        titledPane.setUserData(audiobookVBox);
        letterAccordion.getPanes().add(titledPane);
        /** The following listener defers graphical rendering of audiobookVBox 
         * until user clicks on letterAccordion. (Required to avoid over-lengthy
         * startup of application.) */
        letterAccordion.expandedPaneProperty().addListener
            ((ObservableValue<? extends TitledPane> ov, 
                    TitledPane old_val, TitledPane new_val) -> {
            if (new_val != null && new_val.isExpanded()) {
                new_val.setContent((VBox)new_val.getUserData());
            }
        });    
        return letterAccordion;
    }
            
    private ScrollPane getAudiobookAccordionsScrollPane
            (Class<? extends IndexedKey> indexedKeyClass) {
        VBox audiobookVBox = new VBox();
        try {        
            List<IndexedKey> indexedKeyList 
                = catalog.getIndexedKeyValueList(indexedKeyClass);
            for (IndexedKey indexedKeyObject : indexedKeyList) {
                audiobookVBox.getChildren().add
                    (getAudiobookAccordion(new AudiobookRowParms(indexedKeyObject)));
            }
        }
        catch (InvalidIndexedCollectionQueryException e) {
            audiobookVBox.getChildren().add
                    (getExceptionLabel(e, indexedKeyClass.getSimpleName()));
        }
        ScrollPane audiobookBrowser = new ScrollPane();
        audiobookBrowser.setHbarPolicy(ScrollBarPolicy.NEVER);
        audiobookBrowser.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        audiobookBrowser.setFitToWidth(true);
        audiobookBrowser.setContent(audiobookVBox);
        return audiobookBrowser;
    }
            
    private ScrollPane getMyListAccordionsScrollPane() {
        VBox myListVBox = new VBox();
        Accordion myAuthorsAccordion = new Accordion();
        Label noAuthorsLabel 
            = new Label("Please click here for a tutorial which includes "
                    + "instructions on how to add items to MY LIST.");
        noAuthorsLabel.setFont(BOLD_BIGGER_FONT);
        noAuthorsLabel.setUnderline(true);
        noAuthorsLabel.setOnMouseClicked
            ((MouseEvent me)->{getHostServices().showDocument(TUTORIAL_URL_STRING);});
        noAuthorsLabel.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        noAuthorsLabel.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        TitledPane myAuthorsPane = new TitledPane("MY AUTHORS", noAuthorsLabel);
        myAuthorsAccordion.getPanes().add(myAuthorsPane);
        myListVBox.getChildren().add(myAuthorsAccordion);
        /* // commented out v1.4.0
        if (myList.size(Author.class) == 0) {
            myAuthorsAccordion.setExpandedPane(myAuthorsPane);
        }
        */
        myAuthorsAccordion.expandedPaneProperty().addListener
            ((ObservableValue<? extends TitledPane> ov, 
                    TitledPane old_val, TitledPane new_val) -> {
                if (new_val != null && new_val.isExpanded()) {
                    if (myList.size(Author.class) == 0) {
                        myAuthorsPane.setContent(noAuthorsLabel);
                    } else {
                        VBox myAuthorsVBox = new VBox();
                        try {
                            List<IndexedKey> fullAuthorList 
                                = catalog.getIndexedKeyValueList(Author.class);
                            for (IndexedKey author : fullAuthorList) {
                                if (myList.contains((Author)author)) {
                                    myAuthorsVBox.getChildren().add
                                        (getAudiobookAccordion
                                                (new AudiobookRowParms(author)));
                                }
                            }
                        }
                        catch (InvalidIndexedCollectionQueryException e) {
                            myAuthorsVBox.getChildren().add
                                (getExceptionLabel(e, Author.class.getSimpleName()));
                        }
                        myAuthorsPane.setContent(myAuthorsVBox);
                    }
                }
        });    
        
        Accordion myReadersAccordion = new Accordion();
        Label noReadersLabel 
            = new Label("Please click here for a tutorial which includes "
                    + "instructions on how to add items to MY LIST.");
        noReadersLabel.setFont(BOLD_BIGGER_FONT);
        noReadersLabel.setUnderline(true);
        noReadersLabel.setOnMouseClicked
            ((MouseEvent me)->{getHostServices().showDocument(TUTORIAL_URL_STRING);});
        noReadersLabel.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        noReadersLabel.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        TitledPane myReadersPane 
            = new TitledPane("MY READERS", noReadersLabel);
        myReadersAccordion.getPanes().add(myReadersPane);
        /* // commented out v1.4.0
        if (myList.size(Reader.class) == 0) {
            myReadersAccordion.setExpandedPane(myReadersPane);
        }
        */
        myListVBox.getChildren().add(myReadersAccordion);
        myReadersAccordion.expandedPaneProperty().addListener
            ((ObservableValue<? extends TitledPane> ov, 
                    TitledPane old_val, TitledPane new_val) -> {
                if (new_val != null && new_val.isExpanded()) {
                    if (myList.size(Reader.class) == 0) {
                         myReadersPane.setContent(noReadersLabel);
                    } else {
                        VBox myReadersVBox = new VBox();
                        try {
                            List<IndexedKey> fullReaderList 
                                = catalog.getIndexedKeyValueList(Reader.class);
                            for (IndexedKey reader : fullReaderList) {
                                if (myList.contains((Reader)reader)) {
                                    if (catalog.getWorks(Audiobook.class, reader, 
                                            Catalog.ReaderWorksOption.SOLO_WORKS)
                                                                .size() > 0) {
                                        myReadersVBox.getChildren().add
                                            (getAudiobookAccordion
                                                (new AudiobookRowParms(reader, 
                                                    Catalog.ReaderWorksOption.SOLO_WORKS)));
                                    }
                                    if (catalog.getWorks(Audiobook.class, reader, 
                                            Catalog.ReaderWorksOption.GROUP_WORKS)
                                                                .size() > 0) {
                                        myReadersVBox.getChildren().add
                                            (getAudiobookAccordion
                                                (new AudiobookRowParms(reader, 
                                                    Catalog.ReaderWorksOption.GROUP_WORKS)));
                                    }
                                }
                            }
                        }
                        catch (InvalidIndexedCollectionQueryException e) {
                            myReadersVBox.getChildren().add
                                (getExceptionLabel(e, Reader.class.getSimpleName()));
                        }
                        myReadersPane.setContent(myReadersVBox);
                    }
                }
        });
        
        myListVBox.getChildren().add                        
                (getAudiobookAccordion(new AudiobookRowParms(myList)));
        myListVBox.getChildren().add                        
                (getAudiobookAccordion(new AudiobookRowParms(myBookmarks)));

        Button clearMyListBtn = new Button("Clear\nMY LIST");
        clearMyListBtn.setTextAlignment(TextAlignment.CENTER);
        clearMyListBtn.setOnAction(getClearMyListHandler());
        HBox clearMyListBtnHBox = new HBox(clearMyListBtn);
        clearMyListBtnHBox.setPadding(new Insets(20,10,10,10));
        clearMyListBtnHBox.setMinWidth(SCENE_WIDTH);
        clearMyListBtnHBox.setAlignment(Pos.BOTTOM_LEFT);
        myListVBox.getChildren().add(clearMyListBtnHBox);
        
        ScrollPane myListBrowser = new ScrollPane();
        myListBrowser.setHbarPolicy(ScrollBarPolicy.NEVER);
        myListBrowser.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        myListBrowser.setFitToWidth(true);
        myListBrowser.setContent(myListVBox);
        return myListBrowser;
    }
    
    private EventHandler<ActionEvent> getClearMyListHandler () {
        return (ActionEvent e) -> {
            Stage clearMyListStage = new Stage();
            clearMyListStage.initStyle(StageStyle.UTILITY);
            clearMyListStage.setTitle("Clear MY LIST");
            clearMyListStage.setResizable(false);
            clearMyListStage.initModality(Modality.APPLICATION_MODAL);
            clearMyListStage.initOwner(mainStage);
            
            CheckBox clearAuthorsCheckBox = new CheckBox("Clear MY AUTHORS list");
            CheckBox clearReadersCheckBox = new CheckBox("Clear MY READERS list");
            CheckBox clearAudiobooksCheckBox = new CheckBox("Clear MY AUDIOBOOKS list");
            VBox clearCheckBoxesVBox 
                    = new VBox(clearAuthorsCheckBox, clearReadersCheckBox,
                                                        clearAudiobooksCheckBox);
            clearCheckBoxesVBox.setSpacing(10);

            /* setup bottom pane */
            Button okButton = new Button("OK");
            okButton.setMinWidth(DIALOG_BUTTON_WIDTH);
            okButton.setOnAction((ActionEvent ae) -> {
                if (clearAuthorsCheckBox.isSelected()) {
                    myList.clear(Author.class);
                }
                if (clearReadersCheckBox.isSelected()) {
                    myList.clear(Reader.class);
                }
                if (clearAudiobooksCheckBox.isSelected()) {
                    myList.clear(Audiobook.class);
                }
                //myList.persist();
                clearMyListStage.close(); 
                findExpandedPanes(ExpandedPanesOption.REFRESH,true);
            });
            Button cancelButton = new Button("Cancel");
            cancelButton.setMinWidth(DIALOG_BUTTON_WIDTH);
            cancelButton.setOnAction((ActionEvent ae) -> {
                clearMyListStage.close(); });
            GridPane dialogButtonGridPane = new GridPane();
            dialogButtonGridPane.addRow(0,okButton,cancelButton);
            dialogButtonGridPane.setHgap(10);
            dialogButtonGridPane.setPadding(new Insets(20,10,0,10));
            HBox dialogBottomHBox = new HBox(dialogButtonGridPane);
            dialogBottomHBox.setAlignment(Pos.BOTTOM_CENTER);

            clearMyListStage.addEventFilter(KeyEvent.KEY_PRESSED, 
                (KeyEvent ke) -> { 
                    if (ke.getCode().equals(KeyCode.ESCAPE)) {
                        clearMyListStage.close(); 
                        ke.consume();
                    }
                    if (ke.getCode().equals(KeyCode.ENTER)) {
                        if (okButton.isFocused()) {
                            okButton.fire();
                        }
                        if (cancelButton.isFocused()) {
                            cancelButton.fire();
                        }
                    }
                });
            
            BorderPane clearMyListBorderPane = new BorderPane();
            clearMyListBorderPane.setBackground(DOWNLOAD_PANES_BACKGROUND);
            clearMyListBorderPane.setCenter(clearCheckBoxesVBox);
            clearMyListBorderPane.setBottom(dialogBottomHBox);
            clearMyListBorderPane.setPadding(new Insets(30));
            Scene clearMyListScene = new Scene(clearMyListBorderPane);
            clearMyListStage.setScene(clearMyListScene);
            clearMyListStage.show();
        };
    }
            
    private Accordion getAudiobookAccordion(AudiobookRowParms rowParms) {
        final int MAX_AUDIOBOOK_ROW_SIZE = 50;
        Accordion audiobookAccordion = new Accordion();
        FinalInteger audiobookCount = new FinalInteger(0);
        String audiobookCountString = "";
        String audiobookValueRangeString = "";
        if (rowParms.upperIndex > 0) {
            audiobookCount.set(rowParms.upperIndex - rowParms.lowerIndex);
            if (selectedOrderClass.equals(Title.class)) {
                try {
                    List<Work> audiobooks = catalog.getWorks
                        (Audiobook.class, rowParms.indexedKeyObject, 
                                rowParms.readerWorksOption, selectedOrderClass)
                            .subList(rowParms.lowerIndex, rowParms.upperIndex);
                    Work lowAudiobook = audiobooks.get(0);
                    Work highAudiobook = audiobooks.get(audiobooks.size()-1);
                    String audiobookLowValueString = "";
                    String audiobookHighValueString = "";
                    final int TITLE_SUBSTRING_LENGTH = 40;
                    String title = lowAudiobook.getTitleKey().getKeyItem();
                    if (title.length() <= TITLE_SUBSTRING_LENGTH) {
                        audiobookLowValueString = title;
                    } else {
                        audiobookLowValueString = title.substring
                                (0,title.substring(0,TITLE_SUBSTRING_LENGTH)
                                            .lastIndexOf(' ')) + "...";
                    }
                    title = highAudiobook.getTitleKey().getKeyItem();
                    if (title.length() <= TITLE_SUBSTRING_LENGTH) {
                        audiobookHighValueString = title;
                    } else {
                        audiobookHighValueString = title.substring
                                (0,title.substring(0,TITLE_SUBSTRING_LENGTH)
                                            .lastIndexOf(' ')) + "...";
                    }
                    audiobookValueRangeString 
                        = "          ---->>  from <" 
                            + audiobookLowValueString.toUpperCase() + ">"
                            + " -- through <" 
                            + audiobookHighValueString.toUpperCase() + ">";
                } catch (InvalidIndexedCollectionQueryException e) {}
            } else {
                    audiobookCountString = " [" + (rowParms.lowerIndex + 1) 
                                                + "-" + rowParms.upperIndex + "]";
            }
        } else { 
            try { audiobookCount.set(catalog.getWorks
                    (Audiobook.class, rowParms.indexedKeyObject, 
                            rowParms.readerWorksOption, Title.class).size());
            } catch (InvalidIndexedCollectionQueryException e) {}
            /* Count of audiobooks in MyList is dynamic, so Title of TitlePane
               cannot include it. */
            if (MyList.class.isAssignableFrom(rowParms.indexedKeyObject.getClass())
                    || MyBookmarks.class.isAssignableFrom(rowParms.indexedKeyObject.getClass())) {
                audiobookCountString = "";
            } else {
                audiobookCountString 
                    = (audiobookCount.get() > 0)? " [" + audiobookCount + "]" : "";
            }
        }
        TitledPane titledPane 
            = new TitledPane(rowParms.indexedKeyObject.toString()
                            + rowParms.readerWorksOption.getTitle() // v1.3.3
                            + audiobookCountString 
                            + audiobookValueRangeString, 
                        HIDDEN_DUMMY_LABEL);
        titledPane.setUserData(rowParms);
        audiobookAccordion.getPanes().add(titledPane);
        /** The following listener defers building & rendering of audiobook 
         * ListView(s) until user clicks on audiobookAccordion. (Required to  
         * avoid over-lengthy startup and likely heap overrun.) */
        audiobookAccordion.expandedPaneProperty().addListener
            ((ObservableValue<? extends TitledPane> ov, 
                    TitledPane old_val, TitledPane new_val) -> {
            if (new_val != null) {
                if (new_val.isExpanded()) {
                    if (audiobookCount.get() <= MAX_AUDIOBOOK_ROW_SIZE
                            || MyList.class.isAssignableFrom
                                    (rowParms.indexedKeyObject.getClass())
                            || MyBookmarks.class.isAssignableFrom
                                    (rowParms.indexedKeyObject.getClass())) {
                        if (PersistedAppSettings.getRowAutocloseSetting().equals
                                (PersistedAppSettings.RowAutocloseSetting.AUTOCLOSE_TRUE)) {
                            findExpandedPanes(ExpandedPanesOption.CLOSE,new_val);
                        }
                        if (findExpandedPanes(ExpandedPanesOption.COUNT,false) 
                                                        <= MAX_EXPANDED_PANES) {
                            new_val.setContent
                                (getAudiobookRowWithHoverScroll
                                    ((AudiobookRowParms)new_val.getUserData()));
                        } else {
                            showExpandedPanesWarningStage();
                            new_val.setExpanded(false);
                        }
                    } else {
                        int subgroupIndex 
                            = (audiobookCount.get() / MAX_AUDIOBOOK_ROW_SIZE);
                        VBox subgroupVBox = new VBox();
                        for (int i=0; i <= subgroupIndex; i++) {
                            int rowParmUpperIndex = 
                                (((i + 1) * MAX_AUDIOBOOK_ROW_SIZE) 
                                                    < audiobookCount.get()) ?
                                    ((i + 1) * MAX_AUDIOBOOK_ROW_SIZE) :
                                                        audiobookCount.get();
                            subgroupVBox.getChildren().add
                                (getAudiobookAccordion
                                    (new AudiobookRowParms
                                        (rowParms.indexedKeyObject, 
                                            i * MAX_AUDIOBOOK_ROW_SIZE,
                                            rowParmUpperIndex,
                                            audiobookCount.get(),
                                            rowParms.readerWorksOption
                                        )));
                        }
                        new_val.setContent(subgroupVBox);
                    }
                }
            } else {
                // upon accordion closure, reset TitledPane to conserve memory
                audiobookAccordion.getPanes().clear();
                titledPane.setContent(HIDDEN_DUMMY_LABEL);
                audiobookAccordion.getPanes().add(titledPane);
            }
        });    
        return audiobookAccordion;
    }
    
    private void showExpandedPanesWarningStage () {
        Stage expandedPanesWarningStage = new Stage();
        expandedPanesWarningStage.initStyle(StageStyle.UTILITY);
        expandedPanesWarningStage.setTitle
                        ("Max expanded accordion panes (audiobook rows) reached");
        expandedPanesWarningStage.setResizable(false);
        expandedPanesWarningStage.initOwner(mainStage);
        expandedPanesWarningStage.initModality(Modality.APPLICATION_MODAL);

        BorderPane warningBorderPane = new BorderPane();
        Scene warningScene = new Scene(warningBorderPane);
        warningBorderPane.setBackground(DOWNLOAD_PANES_BACKGROUND);
        
        ImageView oopsSmileyView = new ImageView(OOPS_SMILEY_IMAGE);
        oopsSmileyView.setFitHeight(BTN_HEIGHT * 4);
        oopsSmileyView.setPreserveRatio(true);

        Label warningLabel 
                = new Label("Maximum number of open accordion panes "
                        + "(i.e., rows) of Audiobooks has been reached!\n\n" 
                        + "You cannot open any more until you close some "
                        + "that are open!");
        warningLabel.setGraphic(oopsSmileyView);
        warningLabel.setContentDisplay(ContentDisplay.LEFT);
        warningLabel.setTextAlignment(TextAlignment.LEFT);
        warningLabel.setFont(BOLD_BIGGER_FONT);
        warningLabel.setPadding(INSETS_10);
        warningLabel.setWrapText(true);
        warningLabel.setMaxWidth(DIALOG_WIDTH);
        warningBorderPane.setTop(warningLabel);

        Button closeAutomaticallyButton = new Button("CLOSE all\nopen rows");
        closeAutomaticallyButton.setTextAlignment(TextAlignment.CENTER);
        closeAutomaticallyButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        closeAutomaticallyButton.setOnAction((ActionEvent e) -> {
            findExpandedPanes(ExpandedPanesOption.CLOSE,false);
            expandedPanesWarningStage.close();
        });
        Button closeManuallyButton = new Button("I'll close\nsome manually");
        closeManuallyButton.setTextAlignment(TextAlignment.CENTER);
        closeManuallyButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        closeManuallyButton.setOnAction((ActionEvent e) -> {
            expandedPanesWarningStage.close();
        });

        GridPane dialogButtonGridPane = new GridPane();
        dialogButtonGridPane.addRow(0,closeAutomaticallyButton,closeManuallyButton);
        dialogButtonGridPane.setHgap(10);
        dialogButtonGridPane.setPadding(INSETS_10);
        HBox dialogBottomHBox = new HBox(dialogButtonGridPane);
        dialogBottomHBox.setMinWidth(DIALOG_WIDTH);
        dialogBottomHBox.setAlignment(Pos.BOTTOM_CENTER);
        warningBorderPane.setBottom(dialogBottomHBox);
        
        expandedPanesWarningStage.setScene(warningScene);
        expandedPanesWarningStage.show();
    }
    
    private Node getAudiobookRowWithHoverScroll (AudiobookRowParms rowParms) {
        ObservableList<StackPane> audiobookStackPaneList 
            = getAudiobookStackPaneList
                (selectedOrderClass, rowParms, OverlayPaneOption.DETAIL, 0, null);
        HBox leftFillerBox = new HBox();
        leftFillerBox.setPrefSize(45, IMAGE_SIDE_LENGTH);
        HBox rightFillerBox = new HBox();
        rightFillerBox.setPrefSize(45, IMAGE_SIDE_LENGTH);
        HBox audiobookListHBox = new HBox(leftFillerBox);
        audiobookListHBox.getChildren().addAll(audiobookStackPaneList);
        audiobookListHBox.getChildren().add(rightFillerBox);
        audiobookListHBox.setPadding(Insets.EMPTY);
        audiobookListHBox.setSpacing(5);
        ScrollPane audiobookListScrollPane = new ScrollPane(audiobookListHBox);
        audiobookListScrollPane.setPadding(new Insets(5,0,0,0));
        audiobookListScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        
        // begin hover-scroll infrastructure
        final double MILLISECONDS_PER_KEYFRAME = 10; // 100 frames/second
        final double SCROLL_INCREMENT = 0.015 / audiobookStackPaneList.size();
        
        StackPane leftArrowStackPane = getArrowStackPane(ArrowType.LEFT_ARROW);
        final Timeline leftScrollTimeline = new Timeline();
        leftScrollTimeline.setCycleCount(Timeline.INDEFINITE);
        leftScrollTimeline.getKeyFrames().add
            (new KeyFrame(Duration.millis(MILLISECONDS_PER_KEYFRAME),
                (ActionEvent event) -> 
                    { audiobookListScrollPane.setHvalue
                        (audiobookListScrollPane.getHvalue() - SCROLL_INCREMENT); }));
        leftArrowStackPane.setOnMouseEntered((MouseEvent me) -> { 
            leftScrollTimeline.play();
        });
        leftArrowStackPane.setOnMouseExited((MouseEvent me) -> {
            leftScrollTimeline.stop();
        }); 
        
        StackPane rightArrowStackPane = getArrowStackPane(ArrowType.RIGHT_ARROW);
        final Timeline rightScrollTimeline = new Timeline();
        rightScrollTimeline.setCycleCount(Timeline.INDEFINITE);
        rightScrollTimeline.getKeyFrames().add
            (new KeyFrame(Duration.millis(MILLISECONDS_PER_KEYFRAME),
                (ActionEvent event) -> 
                    { audiobookListScrollPane.setHvalue
                        (audiobookListScrollPane.getHvalue() + SCROLL_INCREMENT); }));
        rightArrowStackPane.setOnMouseEntered((MouseEvent me) -> { 
            rightScrollTimeline.play();
        });
        rightArrowStackPane.setOnMouseExited((MouseEvent me) -> {
            rightScrollTimeline.stop();
        }); 
        // end hover-scroll infrastructure
        
        HBox placeHolder = new HBox();
        placeHolder.setPickOnBounds(false);
        placeHolder.setMinSize(45, 15);
        placeHolder.setMaxSize(45, 15);
        VBox leftArrowVBox = new VBox(leftArrowStackPane,placeHolder);
        VBox rightArrowVBox = new VBox(rightArrowStackPane,placeHolder);
        leftArrowVBox.setPickOnBounds(false);
        rightArrowVBox.setPickOnBounds(false);
        BorderPane hoverScrollBorderPane = new BorderPane();
        hoverScrollBorderPane.setLeft(leftArrowVBox);
        hoverScrollBorderPane.setRight(rightArrowVBox);
        // http://stackoverflow.com/questions/24607969
        hoverScrollBorderPane.setPickOnBounds(false);

        StackPane audiobookRowWithHoverScroll 
            = new StackPane(audiobookListScrollPane, hoverScrollBorderPane);
        audiobookRowWithHoverScroll.setPadding(Insets.EMPTY);
        return audiobookRowWithHoverScroll;
    }
    
    private ObservableList<StackPane> getAudiobookStackPaneList 
            (Class<? extends Key> orderClass, AudiobookRowParms rowParms, 
                    OverlayPaneOption paneOption, int numberRequested, 
                    List<Work> searchResults) {
        ObservableList<StackPane> audiobookStackPanes 
                        = FXCollections.observableList(new ArrayList<>());
        try {
            List<Work> audiobooks = null;
            String audiobookSetIdentifier = null;
            switch (paneOption) {
                case DETAIL:
                    if (rowParms.upperIndex > 0) {
                        audiobooks = catalog.getWorks
                            (Audiobook.class, rowParms.indexedKeyObject, 
                                    rowParms.readerWorksOption, orderClass)
                                .subList(rowParms.lowerIndex, rowParms.upperIndex);
                    } else {
                        audiobooks = catalog.getWorks
                            (Audiobook.class, rowParms.indexedKeyObject,
                                    rowParms.readerWorksOption, orderClass);
                    }
                    if (PersistedUserSelectedCollection.class.isAssignableFrom
                                        (rowParms.indexedKeyObject.getClass())) {
                        audiobookSetIdentifier 
                                = rowParms.indexedKeyObject.toString();
                    } else {
                        audiobookSetIdentifier 
                            = rowParms.indexedKeyObject.getClass().getSimpleName() 
                                    + ": " + rowParms.indexedKeyObject.toString();
                    }
                    break;
                case NEWEST:
                    audiobooks = catalog.getWorks(Audiobook.class, orderClass)
                                                .subList(0, numberRequested);
                    audiobookSetIdentifier = "Newest Offerings";
                    break;
                case RANDOM:
                    audiobooks = catalog.getRandomAudiobooks(numberRequested);
                    audiobookSetIdentifier = "Random Selection";
                    break;
                case SEARCH:
                    audiobooks = searchResults;
                    audiobookSetIdentifier = "Search results";
                    break;
            }
            int audiobookIndex = 0;
            for (Work audiobook : audiobooks) {
                StackPane stackPane = new StackPane(getAudiobookImageNode
                        ((Audiobook)audiobook, IMAGE_SIDE_LENGTH, true, false));
                stackPane.setAlignment(Pos.TOP_CENTER);
                stackPane.setPadding(Insets.EMPTY);
                stackPane.setOnMouseClicked
                    (getOverlayPaneHandler
                        (OverlayPaneOption.DETAIL, audiobooks, audiobookIndex++, 
                                            audiobookSetIdentifier, rowParms));
                audiobookStackPanes.add(stackPane);
                if (numberRequested > 0 && audiobookIndex >= numberRequested ) {
                    break;
                }
            }
        }
        catch (InvalidIndexedCollectionQueryException e) {
            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(getExceptionLabel(e,"audiobook"));
            audiobookStackPanes.add(stackPane);
        }
        return audiobookStackPanes;
    }
    
    private Node getAudiobookTextNode (Work audiobook, double titleBoxSideLength) {
        final double WIDTH_FACTOR = 1.135;
        Text titleText = new Text(audiobook.getTitleForDisplay());
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetY(3.0f);
        dropShadow.setColor(Color.color(0.4f, 0.4f, 0.4f));
        titleText.setEffect(dropShadow);
        if (audiobook.getTitleForDisplay().length() > 35) {
            titleText.setFont(BOLD_BIGGER_FONT);
        } else {
            titleText.setFont(BOLD_BIGGEST_FONT);
        }
        titleText.setWrappingWidth(titleBoxSideLength);
        titleText.setTextAlignment(TextAlignment.CENTER);
        HBox titleBox = new HBox(titleText);
        titleBox.setMinSize(titleBoxSideLength * WIDTH_FACTOR, titleBoxSideLength);
        titleBox.setMaxSize(titleBoxSideLength * WIDTH_FACTOR, titleBoxSideLength);
        titleBox.setPadding(new Insets(2));
        titleBox.setAlignment(Pos.CENTER);
        StringBuilder authorStringBuilder = new StringBuilder("by ");
        if (audiobook.getAuthors() != null && audiobook.getAuthors().size() > 1) {
            authorStringBuilder.append("multiple authors");
        } else if (audiobook.getAuthors() != null && audiobook.getAuthors().size() == 1) {
            authorStringBuilder.append(audiobook.getAuthors().get(0).toString());
        } else {
            authorStringBuilder.append("unknown author");
        }
        Label authorLabel = new Label(authorStringBuilder.toString());
        authorLabel.setFont(Font.font(12));
        authorLabel.setMinSize
            (titleBoxSideLength * WIDTH_FACTOR, titleBoxSideLength * 0.25);
        authorLabel.setMaxSize
            (titleBoxSideLength * WIDTH_FACTOR, titleBoxSideLength * 0.25);
        authorLabel.setPadding(new Insets(0,2,0,2));
        authorLabel.setAlignment(Pos.CENTER);
        authorLabel.setWrapText(true);

        if (alternateLabelColor) {
            alternateLabelColor = false;
            titleText.setFill(Color.DARKRED);
            titleBox.setStyle(AUDIOBOOK_TEXT_LABEL_PINK_CSS);
            authorLabel.setStyle(AUDIOBOOK_TEXT_LABEL_PINK_CSS);
        } else {
            alternateLabelColor = true;
            titleText.setFill(Color.DARKBLUE);
            titleBox.setStyle(AUDIOBOOK_TEXT_LABEL_BLUE_CSS);
            authorLabel.setStyle(AUDIOBOOK_TEXT_LABEL_BLUE_CSS);
        }
        VBox audiobookTextVBox = new VBox (titleBox, authorLabel);
        audiobookTextVBox.setMinSize
                (titleBoxSideLength * WIDTH_FACTOR, titleBoxSideLength * 1.25);
        audiobookTextVBox.setMaxSize
                (titleBoxSideLength * WIDTH_FACTOR, titleBoxSideLength * 1.25);
        return audiobookTextVBox;
    }

    private Node getAudiobookImageNode 
            (Audiobook audiobook, double imageSideLength, 
                    boolean includeTitle, boolean hiDef) {
        Image image = getAudiobookImage(audiobook, hiDef);
        if (image == null) {
            return new StackPane(getAudiobookTextNode(audiobook, imageSideLength * 0.90));
        }
        Label imageLabel = configureImageLabel(image, audiobook, 
                                                imageSideLength, includeTitle);
        StackPane imageStackPane;
        if (image.getProgress() == 1) {
            if (image.isError()) {
                imageStackPane 
                    = new StackPane(getAudiobookTextNode(audiobook, imageSideLength * 0.90));
            } else {
                imageStackPane = new StackPane(imageLabel);
            }
        } else {
            imageLabel.setVisible(false); // set to visible after image loaded
            Node underNode;
            if (hiDef) {
                Image lowDefImage = getAudiobookImage(audiobook, false);
                underNode = configureImageLabel(lowDefImage, audiobook, 
                                                    imageSideLength, includeTitle);
            } else {
                underNode = getAudiobookTextNode(audiobook, imageSideLength * 0.90);
            }
            ProgressIndicator imageLoadProgress = new ProgressIndicator();
            imageLoadProgress.setMaxSize(50,50);
            image.progressProperty().addListener((ov, old_val, new_val) -> {
                if (new_val.doubleValue() == 1){
                    if (!image.isError()) {
                        imageLabel.setVisible(true);
                        underNode.setVisible(false);
                    }
                    imageLoadProgress.setDisable(true);
                    imageLoadProgress.setVisible(false);
                }
            });
            imageStackPane 
                = new StackPane(underNode, imageLoadProgress, imageLabel);
        }
        return imageStackPane;
    }
            
    private Label configureImageLabel(Image image, Audiobook audiobook, 
            double imageSideLength, boolean includeTitle) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(imageSideLength);
        imageView.setFitHeight(imageSideLength);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true); 
        Label imageLabel = new Label();
        if (includeTitle) {
            imageLabel.setText(audiobook.getTitleForDisplay());
            if (audiobook.getTitleForDisplay().length() > 30) {
                imageLabel.setFont(Font.font(9));
            } else {
                imageLabel.setFont(Font.font(10));
            }
        }
        imageLabel.setMaxWidth(imageSideLength);
        imageLabel.setGraphic(imageView);
        imageLabel.setContentDisplay(ContentDisplay.TOP);
        imageLabel.setAlignment(Pos.CENTER);
        imageLabel.setStyle(AUDIOBOOK_IMAGE_LABEL_CSS);
        return imageLabel;
    }

    private Image getAudiobookImage (Audiobook audiobook, boolean hiDef) {
        PersistedAppSettings.CoverArtDisplaySetting coverArtDisplaySetting
                = PersistedAppSettings.getCoverArtDisplaySetting();
        if (coverArtDisplaySetting.equals
                    (PersistedAppSettings.CoverArtDisplaySetting.DISABLE_ALL)) {
            return null;
        }
        Image audiobookImage = null;
        if (!hiDef) {
            audiobookImage = audiobook.getLocalCoverArtImage();
        }
        if (hiDef || audiobookImage == null) {
            if (!coverArtDisplaySetting.equals
                    (PersistedAppSettings.CoverArtDisplaySetting.DISABLE_DOWNLOAD)) {
                audiobookImage = audiobook.getCoverArtImage(); 
            }
            if (hiDef && audiobookImage == null) {
                audiobookImage = audiobook.getLocalCoverArtImage();
            }
        }
        return audiobookImage;
    }

    private EventHandler<MouseEvent> getOverlayPaneHandler 
                (OverlayPaneOption paneOption, 
                        List<Work> audiobooks, int audiobookIndex,
                        String audiobookSetIdentifier, AudiobookRowParms rowParms) {
        return (MouseEvent me) -> {
            switch (paneOption) {
                case DETAIL: 
                    if (detailPaneActive) {
                        return;
                    } 
                    detailPaneActive = true;
                    break;
                case NEWEST: case RANDOM: 
                    if (quickBrowsePaneActive) {
                        return;
                    } 
                    quickBrowsePaneActive = true; 
                    break;
            }
            BorderPane overlayBorderPane = new BorderPane();
            Node exitPane = getOverlayExitPane(paneOption, overlayBorderPane);
            switch (paneOption) {
                case DETAIL: 
                    overlayBorderPane.setTop
                        (new StackPane
                            (exitPane, getOverlayShareControls(), 
                                    getOverlayMediaPlayerControls(),
                                    getOverlaySlideshowControls (audiobooks, 
                                        audiobookIndex, rowParms,
                                        audiobookSetIdentifier, overlayBorderPane)));
                    overlayBorderPane.setCenter
                        (getDetailBottomPane((Audiobook)audiobooks.get(audiobookIndex)));
                    // addEventFilter -- see: http://stackoverflow.com/questions/2166976
                    mainStackPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent ke) -> {
                        switch (ke.getCode()) {
                            case ESCAPE:
                                closeOverlayBorderPane(paneOption, overlayBorderPane);
                                ke.consume();
                                break;
                            case LEFT: case KP_LEFT:
                                leftDetailSlideshowButton.fireEvent(new ActionEvent());
                                break;
                            case RIGHT: case KP_RIGHT:
                                rightDetailSlideshowButton.fireEvent(new ActionEvent());
                                break;
                        }
                    }); 
                    break;
                case NEWEST: case RANDOM: 
                    overlayBorderPane.setTop(exitPane);
                    overlayBorderPane.setCenter
                        (getQuickBrowseBottomPane(paneOption, null));
                    break;
                case SEARCH:
                    overlayBorderPane.setTop(new StackPane
                            (exitPane, getOverlaySearchControls(overlayBorderPane)));
                    // addEventFilter -- see: http://stackoverflow.com/questions/2166976
                    mainStackPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent ke) -> {
                        if (searchTextField != null && !searchTextField.isFocused() 
                                && ke.getCode().isLetterKey() 
                                && searchTextField.getText().isEmpty()) {
                            searchTextField.requestFocus();
                            if (searchTextField.isFocused()) {
                                searchTextField.setText(ke.getCharacter());
                            }
                        }
                    }); 
                    break;
            }
            overlayBorderPane.setStyle
                    ("-fx-background-radius: 10; -fx-background-color: black;");
            overlayBorderPane.setMinWidth(DETAIL_WINDOW_WIDTH);
            overlayBorderPane.setMaxWidth(DETAIL_WINDOW_WIDTH);
            overlayBorderPane.setMaxHeight(DETAIL_WINDOW_HEIGHT);
            overlayBorderPane.setBackground(BLACK_BACKGROUND);
            topPaneHBox.setDisable(true);
            mainStackPane.getChildren().add(overlayBorderPane);
        };
    }
                        
    private Node getOverlayExitPane
            (OverlayPaneOption paneOption, BorderPane overlayBorderPane) {
        Button closeButton = new Button("X");
        closeButton.setPadding(new Insets(0, 30, 0, 30));
        closeButton.setTextFill(Color.WHITE);
        closeButton.setBackground(RED_BACKGROUND);
        closeButton.setFont(BOLD_BIGGEST_FONT);
        DropShadow closeButtonDropShadow = new DropShadow();
        closeButtonDropShadow.setOffsetY(1.0f);
        closeButtonDropShadow.setOffsetX(1.0f);
        closeButtonDropShadow.setColor(Color.CORAL);
        closeButton.setOnMouseEntered
            ((MouseEvent me1)->{closeButton.setEffect(closeButtonDropShadow);});
        closeButton.setOnMouseExited
            ((MouseEvent me1)->{closeButton.setEffect(null);});
        closeButton.setOnAction((ActionEvent e) -> {
            closeOverlayBorderPane(paneOption, overlayBorderPane);
        });
        HBox topHBox = new HBox(closeButton);
        topHBox.setAlignment(Pos.TOP_RIGHT);
        topHBox.setMinWidth(DETAIL_WINDOW_WIDTH);
        topHBox.setMaxWidth(DETAIL_WINDOW_WIDTH);
        topHBox.setPadding(new Insets(0, 10, 0, 0));
        topHBox.setMinHeight(DETAIL_WINDOW_HEIGHT * 0.07);
        topHBox.setMaxHeight(DETAIL_WINDOW_HEIGHT * 0.07);
        return topHBox;
    }
            
    private Node getOverlaySlideshowControls 
            (List<Work> audiobooks, int audiobookIndex,
                    AudiobookRowParms rowParms,
                    String audiobookSetIdentifier, BorderPane overlayBorderPane) {
        int indexOffset = (rowParms == null) ? 0 : rowParms.lowerIndex;
        int displayUpperIndex 
            = (rowParms == null || rowParms.supersetSize == 0)? 
                ((audiobooks == null)? 0 : audiobooks.size() )
                    : rowParms.supersetSize;
        String audiobookSetSubstring;
        if (audiobookSetIdentifier.length() < 40) {
            audiobookSetSubstring = audiobookSetIdentifier;
        } else {
            audiobookSetSubstring = audiobookSetIdentifier.substring(0, 37) + "...";
        }
        Label slideshowLabel 
                = new Label(audiobookSetSubstring + " [" 
                    + (audiobookIndex + 1 + indexOffset) 
                    + " of " + displayUpperIndex + "]");
        slideshowLabel.setFont(BOLD_SMALLER_FONT);
        slideshowLabel.setTextFill(Color.WHITESMOKE);
        slideshowLabel.setPadding(new Insets(5,5,0,5));
        final int SS_ARROW_SIZE = 8; 
        final String LEFT_BTN_STYLE 
            = "-fx-shape: 'M" + SS_ARROW_SIZE + ",-" 
                + SS_ARROW_SIZE + " L0,0 L" + SS_ARROW_SIZE + "," 
                + SS_ARROW_SIZE + " Z';" + STYLE_SCALE_SHAPE_FALSE;
        final String RIGHT_BTN_STYLE 
            = "-fx-shape: 'M0,-" + SS_ARROW_SIZE + " L" 
                + SS_ARROW_SIZE + ",0 L0," + SS_ARROW_SIZE + " Z';" 
                + STYLE_SCALE_SHAPE_FALSE;
        leftDetailSlideshowButton = new Button();
        rightDetailSlideshowButton = new Button();
        leftDetailSlideshowButton.setStyle (LEFT_BTN_STYLE);
        rightDetailSlideshowButton.setStyle (RIGHT_BTN_STYLE);
        FinalInteger audiobookIndexInteger = new FinalInteger(audiobookIndex);
        leftDetailSlideshowButton.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        leftDetailSlideshowButton.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        leftDetailSlideshowButton.setOnAction((ActionEvent e) -> {
            getBookmarkConfirmation(false, mainStage, currentDetailAudiobook,
                                                    mediaCurrentSectionIndex);
            audiobookIndexInteger.decrement();
            if (audiobookIndexInteger.get() < 0) {
                audiobookIndexInteger.set(audiobooks.size() - 1);
            }
            slideshowLabel.setText(audiobookSetSubstring + " [" 
                    + (audiobookIndexInteger.get() + 1 + indexOffset) 
                    + " of " + displayUpperIndex + "]");
            overlayBorderPane.setCenter
                (getDetailBottomPane
                    ((Audiobook)audiobooks.get(audiobookIndexInteger.get())));
        });
        rightDetailSlideshowButton.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        rightDetailSlideshowButton.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        rightDetailSlideshowButton.setOnAction((ActionEvent e) -> {
            getBookmarkConfirmation(false, mainStage, currentDetailAudiobook,
                                                    mediaCurrentSectionIndex);
            audiobookIndexInteger.increment();
            if (audiobookIndexInteger.get() >= audiobooks.size()) {
                audiobookIndexInteger.set(0);
            }
            slideshowLabel.setText(audiobookSetSubstring + " [" 
                    + (audiobookIndexInteger.get() + 1 + indexOffset) 
                    + " of " + displayUpperIndex + "]");
            overlayBorderPane.setCenter
                (getDetailBottomPane
                    ((Audiobook)audiobooks.get(audiobookIndexInteger.get())));
        });
        HBox slideshowButtonsHBox 
            = new HBox(leftDetailSlideshowButton, slideshowLabel, rightDetailSlideshowButton);
        slideshowButtonsHBox.setPickOnBounds(false);
        slideshowButtonsHBox.setPadding(new Insets(0,5,0,5));
        return slideshowButtonsHBox;
    }
            
    private Node getOverlayMediaPlayerControls () {
        mediaPlayerContainerPane = new HBox();
        mediaPlayerContainerPane.setPickOnBounds(false);
        // set visible in playMedia method
        mediaPlayerContainerPane.setVisible(false); 
        mediaPlayerContainerPane.setPadding
                        (new Insets(0,0,0,(DETAIL_WINDOW_WIDTH * 0.35)));
        
        ImageView playingImageView = new ImageView(ITEM_PLAYING_IMAGE);
        playingImageView.setPreserveRatio(true);
        playingImageView.setFitHeight(17);
        mediaCurrentlyPlayingLabel.setPadding(new Insets(0,5,0,5));
        mediaCurrentlyPlayingLabel.setBackground(GRAY_BACKGROUND);
        mediaCurrentlyPlayingLabel.setFont(SUPERSMALL_ITALIC_FONT);
        mediaCurrentlyPlayingLabel.setMinWidth(90);
        mediaCurrentlyPlayingLabel.setTextAlignment(TextAlignment.RIGHT);
        mediaCurrentlyPlayingLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox mediaCurrentlyPlayingPane 
                = new HBox(playingImageView, mediaCurrentlyPlayingLabel);
        mediaCurrentlyPlayingPane.setAlignment(Pos.CENTER_RIGHT);
        mediaPlayerContainerPane.getChildren().add(mediaCurrentlyPlayingPane);

        mediaPlayerControlBox = new HBox();
        mediaPlayerControlBox.setPickOnBounds(false);
        mediaPlayerControlBox.setStyle
                ("-fx-background-color: black; " 
                    + "-fx-border-color: white; -fx-border-width: 2");
        mediaPlayerControlBox.setPadding(new Insets(5,0,0,5));
        
        final double PLAY_BUTTON_SIZE = 20;
        playButtonImageView.setImage(PAUSE_BUTTON_IMAGE);
        playButtonImageView.setPreserveRatio(true);
        playButtonImageView.setFitHeight(PLAY_BUTTON_SIZE);
        playButtonImageView.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        playButtonImageView.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        playButtonImageView.setOnMouseClicked(getPlayButtonHandler());
        mediaPlayerControlBox.getChildren().add(playButtonImageView);
        
        mediaCurrentTimeLabel = new Label();
        mediaCurrentTimeLabel.setMinWidth(45);
        mediaCurrentTimeLabel.setMaxWidth(45);
        mediaCurrentTimeLabel.setFont(SUPERSMALL_FONT);
        mediaCurrentTimeLabel.setAlignment(Pos.CENTER_RIGHT);
        mediaCurrentTimeLabel.setTextAlignment(TextAlignment.RIGHT);
        mediaCurrentTimeLabel.setTextFill(Color.WHITE);
        mediaCurrentTimeLabel.setPadding(new Insets(3,1,0,5));
        mediaPlayerControlBox.getChildren().add(mediaCurrentTimeLabel);

        mediaTimeSlider = new Slider();
        mediaTimeSlider.setMinWidth(150);
        mediaTimeSlider.setMaxWidth(150);
        mediaTimeSlider.setPadding(new Insets(3,0,0,0));
        mediaTimeSlider.valueProperty().addListener((Observable ov) -> {
            if (mediaTimeSlider.isValueChanging()) {
                mediaPlayer.seek
                    (mediaFullDuration.multiply(mediaTimeSlider.getValue()/100.0));
            }
        });
        mediaPlayerControlBox.getChildren().add(mediaTimeSlider);
        
        mediaFullDurationLabel = new Label();
        mediaFullDurationLabel.setMinWidth(45);
        mediaFullDurationLabel.setMaxWidth(45);
        mediaFullDurationLabel.setFont(SUPERSMALL_FONT);
        mediaFullDurationLabel.setTextFill(Color.WHITE);
        mediaFullDurationLabel.setAlignment(Pos.CENTER_LEFT);
        mediaFullDurationLabel.setPadding(new Insets(3,0,0,1));
        mediaPlayerControlBox.getChildren().add(mediaFullDurationLabel);
        
        mp3LoadProgressIndicator = new ProgressIndicator();
        mp3LoadProgressIndicator.setMaxSize(25,25);
        mp3LoadProgressLabel = new Label(LOADING_MEDIA);
        mp3LoadProgressLabel.setPadding(new Insets(0,0,0,3));
        mp3LoadProgressLabel.setFont(SMALLEST_BOLD_FONT);
        mp3LoadProgressLabel.setMaxHeight(25);
        mp3LoadProgressHBox = new HBox(mp3LoadProgressIndicator, mp3LoadProgressLabel);
        mp3LoadProgressHBox.setBackground(WHITESMOKE_BACKGROUND);
        mp3LoadProgressHBox.setOpacity(0.8);
        mp3LoadProgressHBox.setAlignment(Pos.CENTER);
        
        StackPane mediaPlayerControlStackPane 
                = new StackPane(mediaPlayerControlBox, mp3LoadProgressHBox);
        
        mediaPlayerContainerPane.getChildren().add(mediaPlayerControlStackPane);
        
        return mediaPlayerContainerPane;
    }
    
    private EventHandler<MouseEvent> getPlayButtonHandler() {
        return (MouseEvent e) -> {
            switch (mediaPlayer.getStatus()) {
                case PAUSED: case READY: case STOPPED:
                    playButtonImageView.setImage(PAUSE_BUTTON_IMAGE);
                    mediaPlayer.play();
                    break;
                case PLAYING: case STALLED:
                    playButtonImageView.setImage(PLAY_BUTTON_IMAGE);
                    mediaPlayer.pause();
                    break;
            }
        };
    }
    
    private Node getOverlayShareControls () {
        MenuItem fbMenuItem = new MenuItem(null, new ImageView(FB_BUTTON_IMAGE));
        fbMenuItem.setOnAction((ActionEvent e) -> {
            String urlString = "https://www.facebook.com/sharer/sharer.php?u="
                            + currentDetailAudiobook.getUrlLibrivoxUriEncoded();
            getHostServices().showDocument(urlString);
        });
        MenuItem twitterMenuItem = new MenuItem(null, new ImageView(TWITTER_BUTTON_IMAGE));
        twitterMenuItem.setOnAction((ActionEvent e) -> {
            String urlString = "https://twitter.com/home?status="
                            + currentDetailAudiobook.getUrlLibrivoxUriEncoded();
            getHostServices().showDocument(urlString);
        });
        MenuItem gPlusMenuItem = new MenuItem(null, new ImageView(GPLUS_BUTTON_IMAGE));
        gPlusMenuItem.setOnAction((ActionEvent e) -> {
            String urlString = "https://plus.google.com/share?url="
                            + currentDetailAudiobook.getUrlLibrivoxUriEncoded();
            getHostServices().showDocument(urlString);
        });
        MenuItem tumblrMenuItem = new MenuItem(null, new ImageView(TUMBLR_BUTTON_IMAGE));
        tumblrMenuItem.setOnAction((ActionEvent e) -> {
            String urlString = "http://www.tumblr.com/share/link?url="
                            + currentDetailAudiobook.getUrlLibrivoxUriEncoded();
            getHostServices().showDocument(urlString);
        });
        ContextMenu contextMenu 
                = new ContextMenu(fbMenuItem, twitterMenuItem, 
                                    gPlusMenuItem, tumblrMenuItem);

        ImageView shareButtonImageView = new ImageView(SHARE_BUTTON_IMAGE);
        shareButtonImageView.setFitWidth(80);
        shareButtonImageView.setPreserveRatio(true);
        shareButtonImageView.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        shareButtonImageView.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        shareButtonImageView.setOnMouseClicked
            ((MouseEvent me)->{ 
                // mouseClick toggles mbContextMenu on and off
                if (contextMenu.isShowing()) {
                    contextMenu.hide();
                } else {
                    contextMenu.show(shareButtonImageView, Side.BOTTOM, 0, 0);
                }
                me.consume();
            });
        
        HBox shareButtonContainerPane = new HBox();
        shareButtonContainerPane.setPickOnBounds(false);
        shareButtonContainerPane.setPadding
                        (new Insets(2,0,0,(DETAIL_WINDOW_WIDTH * 0.8)));
        shareButtonContainerPane.getChildren().add(shareButtonImageView);
        
        return shareButtonContainerPane;
    }
    
    private Node getOverlaySearchControls (BorderPane overlayBorderPane) {
        final String REMOTE_GOOGLE_URL_STRING = "https://www.google.com/search?q=";
        ToggleGroup searchOptionToggleGroup = new ToggleGroup();
        RadioButton searchHereRadioButton = new RadioButton("Search here");
        searchHereRadioButton.setFont(SMALLEST_FONT);
        searchHereRadioButton.setTextFill(Color.WHITE);
        searchHereRadioButton.setToggleGroup(searchOptionToggleGroup);
        searchHereRadioButton.setSelected(true);
        //searchHereRadioButton.setDisable(true); // pending Google API account setup
        RadioButton searchLvSiteButton = new RadioButton("Search LibriVox website");
        searchLvSiteButton.setFont(SMALLEST_FONT);
        searchLvSiteButton.setTextFill(Color.WHITE);
        searchLvSiteButton.setToggleGroup(searchOptionToggleGroup);
        //searchLvSiteButton.setSelected(true); // pending Google API account setup
        VBox searchOptionToggleVBox 
                        = new VBox(searchHereRadioButton, searchLvSiteButton);
        searchOptionToggleVBox.setSpacing(3);
        
        searchTextField = new TextField();
        searchTextField.setPromptText("Search");
        searchTextField.setMinWidth(DETAIL_WINDOW_WIDTH * 0.4);
        searchTextField.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.4);
        ImageView searchImageView = new ImageView(SEARCH_GOOGLE_IMAGE);
        searchImageView.setFitWidth(47);
        searchImageView.setPreserveRatio(true);
        searchImageView.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        searchImageView.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        searchImageView.setOnMouseClicked
            ((MouseEvent me)->{
                if (searchTextField.getText().isEmpty()) {
                    return;
                }
                if (searchLvSiteButton.isSelected()) {
                    try { getHostServices().showDocument(GOOGLE_QUERY_URL
                                + URLEncoder.encode((searchTextField.getText() 
                                + Catalog.GOOGLE_QUERY_FILTER), Catalog.UTF8_CHARSET));
                    } catch (UnsupportedEncodingException e) {}
                } else {
                    assert (searchHereRadioButton.isSelected());
                    overlayBorderPane.setCenter
                        (getQuickBrowseBottomPane(OverlayPaneOption.SEARCH, 
                                                        searchTextField.getText()));
                }
            });
        HBox searchTextFieldHBox = new HBox(searchTextField,searchImageView);
        searchTextFieldHBox.setPadding(new Insets(4,5,0,0));
        
        HBox searchHBox 
                = new HBox(searchTextFieldHBox,searchOptionToggleVBox);
        searchHBox.setPickOnBounds(false);
        searchHBox.setPadding(new Insets(1,0,0,(DETAIL_WINDOW_WIDTH * 0.22)));
        searchHBox.addEventFilter(KeyEvent.KEY_PRESSED, 
            (KeyEvent ke) -> { 
                /*
                if (!searchTextField.isFocused() && ke.getCode().isLetterKey() 
                        && searchTextField.getText().isEmpty()) {
                    searchTextField.requestFocus();
                    if (searchTextField.isFocused()) {
                        searchTextField.setText(ke.getCharacter());
                    }
                }
                */
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    if (!searchTextField.getText().isEmpty()) {
                        // http://stackoverflow.com/questions/11552176
                        Event.fireEvent(searchImageView, new MouseEvent
                            (MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, 
                                MouseButton.PRIMARY, 1, true, true, true, true, 
                                true, true, true, true, true, true, null));
                        ke.consume();
                    }
                }
            });
        return searchHBox;
    }
    
    private void updateMediaValues(MyBookmarks.Bookmark bookmark) {
        if (mediaCurrentTimeLabel != null && mediaTimeSlider != null) {
            Platform.runLater(() -> {
                Duration mediaCurrentTime;
                if (bookmark == null) {
                    mediaCurrentTime = mediaPlayer.getCurrentTime();
                } else {
                    if (mediaPlayer.getStartTime().toMillis()
                                > mediaPlayer.getCurrentTime().toMillis()) {
                        mediaCurrentTime = mediaPlayer.getStartTime();
                    } else {
                        mediaCurrentTime = mediaPlayer.getCurrentTime();
                    }
                }
                mediaCurrentTimeLabel.setText
                        (formatTime(mediaCurrentTime, mediaFullDuration));
                mediaTimeSlider.setDisable(mediaFullDuration.isUnknown());
                if (!mediaTimeSlider.isDisabled()
                        && mediaFullDuration.greaterThan(Duration.ZERO)
                        && !mediaTimeSlider.isValueChanging()) {
                    mediaTimeSlider.setValue
                        (mediaCurrentTime.divide
                            (mediaFullDuration.toMillis()).toMillis()*100.0);
                }
            });
        }
    }    
    
    private void closeOverlayBorderPane 
                (OverlayPaneOption paneOption, BorderPane overlayBorderPane) {
        if (paneOption.equals(OverlayPaneOption.DETAIL)) {
            detailPaneActive = false; 
            getBookmarkConfirmation(false, mainStage, currentDetailAudiobook,
                                                    mediaCurrentSectionIndex);
        } else {
            quickBrowsePaneActive = false;
        }
        if (searchTask != null && searchTask.isRunning()) {
            searchTask.cancel();
        }
        // NOTE: disabling all Nodes of mainStackPane causes unwanted shifting
        topPaneHBox.setDisable(false);
        mainStackPane.getChildren().remove(overlayBorderPane);
    }
            
    private Node getDetailBottomPane(Audiobook audiobook) {
        currentDetailAudiobook = audiobook;
        mediaPlayerContainerPane.setVisible(false);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        final double DETAIL_WINDOW_PADDING = 20;
        VBox leftVBox = new VBox();
        leftVBox.setPadding(new Insets(DETAIL_WINDOW_PADDING));
        leftVBox.setBackground(BLACK_BACKGROUND);
        leftVBox.getChildren().addAll
            (getAudiobookImageNode(audiobook,IMAGE_DETAIL_SIDE_LENGTH,false,true),
                getDownloadButtons(audiobook),
                getMiscInfoPane(audiobook), 
                getCenteredSeparator(IMAGE_DETAIL_SIDE_LENGTH * 0.65, 
                                        IMAGE_DETAIL_SIDE_LENGTH),
                getDatesPane(audiobook),
                getCenteredSeparator(IMAGE_DETAIL_SIDE_LENGTH * 0.65, 
                                        IMAGE_DETAIL_SIDE_LENGTH),
                getLinksPane(audiobook) );
        
        VBox rightVBox = new VBox();
        rightVBox.setPadding(new Insets(DETAIL_WINDOW_PADDING));
        rightVBox.setMinWidth((DETAIL_WINDOW_WIDTH * 0.65) + 40);
        rightVBox.setMaxWidth((DETAIL_WINDOW_WIDTH * 0.65) + 40);
        rightVBox.setBackground(BLACK_BACKGROUND);
        Label titleLabel = new Label(audiobook.getTitleForDisplay());
        titleLabel.setTextFill(Color.GREY);
        titleLabel.setFont(BOLD_ENORMOUS_FONT);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.60); //0.65);
        HBox titleLabelHBox 
                = new HBox(titleLabel,getMyListSelectionButton(audiobook,20,7));
        rightVBox.getChildren().add(titleLabelHBox);
        rightVBox.getChildren().add(getAuthorsFlowPane(audiobook));
        rightVBox.getChildren().add(getReadersFlowPane(audiobook));

        WebView descriptionWebView = new WebView();
        // remove all href attributes from description html
        String trimmedDescription 
                = audiobook.getDescription().replaceAll(REGEX_HREF, "");
        descriptionWebView.getEngine().loadContent(trimmedDescription);
        descriptionWebView.getEngine().setUserStyleSheetLocation
            (LeBrowser.class.getResource(WEBVIEW_CSS_FILE).toExternalForm());
        descriptionWebView.setMinWidth((DETAIL_WINDOW_WIDTH * 0.65) - 10);
        descriptionWebView.setMaxWidth((DETAIL_WINDOW_WIDTH * 0.65) - 10);
        double webViewHeight = (trimmedDescription.length() < 350)?  125 : 200;
        descriptionWebView.setMinHeight(webViewHeight);
        descriptionWebView.setMaxHeight(webViewHeight);
        rightVBox.getChildren().add(descriptionWebView);
        
        final Node sectionsPane = getSectionsPane(audiobook);
        rightVBox.getChildren().add(sectionsPane);
        
        if (myBookmarks.contains(audiobook)) {
            playMedia(myBookmarks.get(audiobook));
        }

        HBox centerHBox = new HBox(leftVBox, rightVBox);
        centerHBox.setPadding(Insets.EMPTY);
        centerHBox.setBackground(BLACK_BACKGROUND);
        centerHBox.setMinHeight(DETAIL_WINDOW_HEIGHT * 0.5);
        ScrollPane centerScrollPane = getCenterScrollPane(0.9);
        centerScrollPane.setContent(centerHBox);
        return centerScrollPane;
    }
    
    private Node getMyListSelectionButton 
            (HasLibrivoxId object, double sideLength, double padding) {
        ImageView checkmarkImageView = new ImageView();
        checkmarkImageView.setPreserveRatio(true);
        if (myList.contains(object)) {
            checkmarkImageView.setImage(CHECKMARK_GREEN_IMAGE); 
            checkmarkImageView.setFitWidth(sideLength);
            checkmarkImageView.setFitHeight(sideLength);
        } else {
            checkmarkImageView.setImage(CHECKMARK_BLACK_IMAGE); 
            checkmarkImageView.setFitWidth(sideLength - 4);
            checkmarkImageView.setFitHeight(sideLength - 4);
        }
        Tooltip tooltip = new Tooltip();
        String tooltipOnMyList 
                = new String("Click to remove " 
                        + object.getClass().getSimpleName() + " from MY LIST.");
        String tooltipNotOnMyList
                = new String("Click to add " 
                        + object.getClass().getSimpleName() + " to MY LIST.");
        if (myList.contains(object)) {
          tooltip.setText(tooltipOnMyList);
        } else {
          tooltip.setText(tooltipNotOnMyList);
        }
        Button userSelectedButton = new Button("", checkmarkImageView);
        userSelectedButton.setTooltip(tooltip);
        userSelectedButton.setMinSize(sideLength, sideLength);
        userSelectedButton.setMaxSize(sideLength, sideLength);
        userSelectedButton.setUserData(object);
        userSelectedButton.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        userSelectedButton.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        userSelectedButton.setOnMouseClicked
            ((MouseEvent me)->{
                if (myList.contains(object)) {
                    checkmarkImageView.setImage(CHECKMARK_BLACK_IMAGE);
                    checkmarkImageView.setFitWidth(sideLength - 4);
                    checkmarkImageView.setFitHeight(sideLength - 4);
                    tooltip.setText(tooltipNotOnMyList);
                    myList.remove(object);
                } else {
                    checkmarkImageView.setImage(CHECKMARK_GREEN_IMAGE);
                    checkmarkImageView.setFitWidth(sideLength);
                    checkmarkImageView.setFitHeight(sideLength);
                    tooltip.setText(tooltipOnMyList);
                    myList.add(object);
                }
                findExpandedPanes(ExpandedPanesOption.REFRESH,true);
            });
        HBox userSelectedHBox = new HBox(userSelectedButton);
        userSelectedHBox.setPadding(new Insets(padding));
        return userSelectedHBox;
    }
    
    private ScrollPane getCenterScrollPane(double heightFactor) {
        ScrollPane centerScrollPane = new ScrollPane();
        centerScrollPane.setPadding(Insets.EMPTY);
        centerScrollPane.setBackground(BLACK_BACKGROUND);
        centerScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        centerScrollPane.setMinHeight(DETAIL_WINDOW_HEIGHT * heightFactor);
        centerScrollPane.setMaxHeight(DETAIL_WINDOW_HEIGHT * heightFactor);
        return centerScrollPane;
    }
                        
    private Node getCenteredSeparator (double width, double spaceWidth) {
        Separator separator = new Separator();
        separator.setMinWidth(width);
        separator.setMaxWidth(width);
        HBox separatorBox = new HBox(separator);
        separatorBox.setMinWidth(spaceWidth);
        separatorBox.setMaxWidth(spaceWidth);
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setPadding(new Insets(2,0,2,0));
        return separatorBox;
    }
    /*
    private Tooltip getAudiobookTooltip(Work audiobook) {
        String authorString = getAuthorsString(audiobook);
        StringBuilder orderingValue 
                = new StringBuilder("\n-- " + selectedOrderClass.getSimpleName()
                                                                    + " = ");
        if (selectedOrderClass == PublicationDate.class) {
            orderingValue.append
                (audiobook.getPublicationDateInternetArchive().substring(0,10));
        } else if (selectedOrderClass == DownloadsPerDay.class) {
            orderingValue.append(String.format("%,.2f",audiobook.getDownloadsPerDay()));
        } else if (selectedOrderClass == Downloads.class) {
            orderingValue.append 
                (String.format("%,d",audiobook.getDownloadCountInternetArchive()));
        } else {
            orderingValue = new StringBuilder();
        }
        Tooltip tooltip
                = new Tooltip(audiobook.getTitleForDisplay() + orderingValue
                                + "\n-- " + authorString);
        tooltip.setMaxWidth(400); 
        tooltip.setWrapText(true); 
        // IMPORTANT: Do NOT set the tooltip font! If you do, a bug in 
        //  tooltips may cause tooltip font to shrink with each mouseover.
        //  See: http://stackoverflow.com/questions/23688306/
        //tooltip.setFont(Font.font(Font.getDefault().getName(), 14));
        return tooltip;
    }
    */
    private String getAuthorsString (Work work) {
        StringBuilder authorString = new StringBuilder();
        boolean firstAppended = false;
        boolean skipVariousLiteral = false;
        final String VARIOUS_LITERAL = "Various";
        if (work.getAuthors() != null && work.getAuthors().size() > 1) {
            skipVariousLiteral = true;
        }
        if (work.getAuthors() != null) {
            for (Author author : work.getAuthors()) {
                if (skipVariousLiteral 
                        && author.getLastName().equalsIgnoreCase(VARIOUS_LITERAL)) {
                    continue;
                }
                if (firstAppended) {
                    authorString.append(", ");
                } else {
                    authorString.append("by ");
                    firstAppended = true;
                }
                if (!author.getFirstName().isEmpty()) {
                    authorString.append(author.getFirstName()).append(" ");
                }
                authorString.append(author.getLastName());
            }
        }
        return authorString.toString();
    }
    
    private Node getAuthorsFlowPane (Work work) {
        String authorsList = getAuthorsString(work);
        Font authorFont;
        double buttonSize;
        double buttonPadding;
        authorFont = ITALIC_BIGGER_FONT;
        buttonSize = 13;
        buttonPadding = 2;
        FlowPane authorFlowPane = new FlowPane();
        authorFlowPane.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.65);
        boolean firstAppended = false;
        boolean skipVariousLiteral = false;
        final String VARIOUS_LITERAL = "Various";
        if (work.getAuthors() != null && work.getAuthors().size() > 1) {
            skipVariousLiteral = true;
        }
        int authorCount = 0;
        if (work.getAuthors() != null) {
            for (Author author : work.getAuthors()) {
                if (skipVariousLiteral 
                        && author.getLastName().equalsIgnoreCase(VARIOUS_LITERAL)) {
                    continue;
                }
                authorCount++;
                StringBuilder authorString = new StringBuilder();
                if (!firstAppended) {
                    authorString.append("by ");
                    firstAppended = true;
                }
                if (!author.getFirstName().isEmpty()) {
                    authorString.append(author.getFirstName()).append(" ");
                }
                authorString.append(author.getLastName());
                Label authorLabel = new Label(authorString.toString());
                authorLabel.setTextFill(Color.GREY);
                authorLabel.setFont(authorFont);
                HBox authorLabelHBox = new HBox(authorLabel);
                if (!author.getLastName().equalsIgnoreCase(VARIOUS_LITERAL)) {
                    authorLabelHBox.getChildren().add
                            (getMyListSelectionButton(author,buttonSize,buttonPadding));
                }
                if (authorCount < work.getAuthors().size()) {
                    Label commaLabel = new Label(", ");
                    commaLabel.setTextFill(Color.GREY);
                    commaLabel.setFont(authorFont);
                    authorLabelHBox.getChildren().add(commaLabel);
                }
                authorFlowPane.getChildren().add(authorLabelHBox);
            }
        }
        return authorFlowPane;
    }
    
    private String getReadersString (Work work) {
        StringBuilder readerString = new StringBuilder();
        boolean firstAppended = false;
        int readerCount = 0;
        if (work.getReaders() != null) {
            for (Reader reader : work.getReaders()) {
                readerCount++;
                if (firstAppended) {
                    readerString.append(", ");
                } else {
                    readerString.append("READ BY ");
                    firstAppended = true;
                }
                readerString.append(reader.getDisplayName());
            }
        }
        return readerString.toString();
    }
    
    private Node getReadersFlowPane (Work work) {
        FlowPane readerFlowPane = new FlowPane();
        readerFlowPane.setMinWidth(DETAIL_WINDOW_WIDTH * 0.65);
        readerFlowPane.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.65);
        boolean firstAppended = false;
        int readerCount = 0;
        if (work.getReaders() != null) {
            for (Reader reader : work.getReaders()) {
                readerCount++;
                StringBuilder readerString = new StringBuilder();
                if (!firstAppended) {
                    readerString.append("READ BY ");
                    firstAppended = true;
                }
                readerString.append(reader.getDisplayName());
                Label readerLabel = new Label(readerString.toString());
                readerLabel.setTextFill(Color.WHITE);
                readerLabel.setFont(BIGGER_FONT);
                HBox readerLabelHBox = new HBox(readerLabel);
                readerLabelHBox.getChildren().add(getMyListSelectionButton(reader,13,2));
                if (readerCount < work.getReaders().size()) {
                    Label commaLabel = new Label(", ");
                    commaLabel.setTextFill(Color.WHITE);
                    commaLabel.setFont(BIGGER_FONT);
                    readerLabelHBox.getChildren().add(commaLabel);
                }
                readerFlowPane.getChildren().add(readerLabelHBox);
            }
        }
        HBox readerFlowPaneHBox = new HBox(readerFlowPane);
        readerFlowPaneHBox.setPadding(new Insets(5,0,15,0));
        return readerFlowPaneHBox;
    }
    
    private Node getSectionsPane (Audiobook audiobook) {
        VBox sectionsVBox = new VBox();
        sectionsVBox.setPadding(INSETS_10);
        sectionMediaImageViews
                    = FXCollections.observableList(new ArrayList<>());

        boolean nonZeroSectionNumbers = false;
        if (audiobook.getSections() != null) {
            for (Section section : audiobook.getSections()) {
                if (section.getSectionNumber() > 0) {
                    nonZeroSectionNumbers = true;
                    break;
                }
            }
        }
        int sectionCount = 0;
        if (audiobook.getSections() != null) {
            for (Section section : audiobook.getSections()) {
                sectionCount++;
                StringBuilder sectionNumberString = new StringBuilder("Section ");
                if (nonZeroSectionNumbers) {
                    sectionNumberString.append(section.getSectionNumber());
                } else {
                    sectionNumberString.append(sectionCount);
                }
                sectionNumberString.append(":  ");
                Label sectionNumberLabel 
                    = new Label(sectionNumberString.toString());
                sectionNumberLabel.setAlignment(Pos.CENTER_RIGHT);
                sectionNumberLabel.setTextFill(SECTION_TEXT_COLOR);
                if (audiobook.getSections().size() > 100) {
                    sectionNumberLabel.setMinWidth(DETAIL_WINDOW_WIDTH * 0.09);
                    sectionNumberLabel.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.09);
                } else {
                    sectionNumberLabel.setMinWidth(DETAIL_WINDOW_WIDTH * 0.08);
                    sectionNumberLabel.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.08);
                }
                sectionNumberLabel.setFont(ITALIC_FONT);
                Label sectionTitleLabel = new Label(section.getTitle());
                sectionTitleLabel.setAlignment(Pos.CENTER_LEFT);
                sectionTitleLabel.setTextFill(SECTION_TEXT_COLOR);
                sectionTitleLabel.setFont(BOLD_FONT);
                sectionTitleLabel.setWrapText(true);
                ImageView launchMediaPlayerImage = new ImageView(PLAY_BUTTON_IMAGE);
                launchMediaPlayerImage.setPreserveRatio(true);
                launchMediaPlayerImage.setFitHeight(17);
                if (section.getUrlForListening() == null
                        || section.getUrlForListening().isEmpty()) {
                    launchMediaPlayerImage.setVisible(false);
                }
                launchMediaPlayerImage.setUserData(new Integer(sectionCount - 1));
                launchMediaPlayerImage.setOnMouseEntered
                    ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
                launchMediaPlayerImage.setOnMouseExited
                    ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
                sectionMediaImageViews.add(launchMediaPlayerImage);

                launchMediaPlayerImage.setOnMouseClicked
                    ((MouseEvent me)-> {
                        mediaCurrentSectionIndex 
                                =  (Integer)launchMediaPlayerImage.getUserData();
                        playMedia(null);
                    });
                HBox sectionHBox 
                    = new HBox(launchMediaPlayerImage,sectionNumberLabel,sectionTitleLabel);
                sectionHBox.setPadding(new Insets(5,0,0,0));
                sectionHBox.setAlignment(Pos.TOP_LEFT);
                sectionsVBox.getChildren().add(sectionHBox);
                final Insets MORE_INFO_INSETS = new Insets(0,0,0,90);
                if (section.getAuthors() != null 
                        && section.getAuthors().size() > 0
                        && audiobook.getAuthors().size() > 1 
                        && section.getAuthors().size() != audiobook.getAuthors().size()){
                    Label authorLabel = new Label(getAuthorsString(section));
                    authorLabel.setPadding(MORE_INFO_INSETS);
                    authorLabel.setTextFill(SECTION_TEXT_COLOR);
                    authorLabel.setFont(ITALIC_FONT);
                    authorLabel.setWrapText(true);
                    sectionsVBox.getChildren().add(authorLabel);
                }
                if (section.getReaders() != null
                        && section.getReaders().size() > 0
                        && audiobook.getReaders().size() > 1 
                        && section.getReaders().size() != audiobook.getReaders().size()){
                    Label readerLabel = new Label(getReadersString(section));
                    readerLabel.setPadding(MORE_INFO_INSETS);
                    readerLabel.setTextFill(SECTION_TEXT_COLOR);
                    readerLabel.setWrapText(true);
                    sectionsVBox.getChildren().add(readerLabel);
                }
                if (section.getLanguage() != null
                        && section.getLanguage().getLanguage() != null
                        && !section.getLanguage().getLanguage().isEmpty()
                        && audiobook.getAllLanguages().size() > 1 ) {
                    Label languageLabel 
                            = new Label("Language: " + section.getLanguage());
                    languageLabel.setPadding(MORE_INFO_INSETS);
                    languageLabel.setTextFill(SECTION_TEXT_COLOR);
                    languageLabel.setWrapText(true);
                    sectionsVBox.getChildren().add(languageLabel);
                }
                if (section.getDurationInSeconds() > 0) {
                    Label durationLabel 
                            = new Label("Duration: " + getDurationString(section));
                    durationLabel.setPadding(MORE_INFO_INSETS);
                    durationLabel.setTextFill(SECTION_TEXT_COLOR);
                    sectionsVBox.getChildren().add(durationLabel);
                }
                if (section.getCopyrightYear() != null
                        && !section.getCopyrightYear().isEmpty()
                        && !section.getCopyrightYear().equals("0")
                        && !section.getCopyrightYear().equals
                                (audiobook.getCopyrightYear())) {
                    Label copyrightLabel 
                            = new Label("Year originally published: " 
                                                + section.getCopyrightYear());
                    copyrightLabel.setPadding(MORE_INFO_INSETS);
                    copyrightLabel.setTextFill(SECTION_TEXT_COLOR);
                    sectionsVBox.getChildren().add(copyrightLabel);
                }
            }
        }
        return sectionsVBox;
    }
    
    private void playMedia (MyBookmarks.Bookmark bookmark) {
        List<Section> sections = currentDetailAudiobook.getSections();
        if (bookmark == null) {
            mp3LoadProgressLabel.setText(LOADING_MEDIA);
        } else {
            mp3LoadProgressLabel.setText(RESUMING_BOOKMARK);
            mediaCurrentSectionIndex = bookmark.getSectionIndex();
        }
        mediaCurrentTimeLabel.setVisible(false);
        mediaFullDurationLabel.setVisible(false);
        for (ImageView imageView : sectionMediaImageViews) {
            imageView.setImage(PLAY_BUTTON_IMAGE);
        }
        sectionMediaImageViews.get(mediaCurrentSectionIndex).setImage
                                                        (ITEM_PLAYING_IMAGE);
        boolean nonZeroSectionNumbers = false;
        for (Section section : sections) {
            if (section.getSectionNumber() > 0) {
                nonZeroSectionNumbers = true;
                break;
            }
        }
        if (nonZeroSectionNumbers) {
            mediaCurrentlyPlayingLabel.setText
                ("Section " 
                    + sections.get(mediaCurrentSectionIndex).getSectionNumber() 
                    + " of " + sections.get(sections.size() - 1).getSectionNumber());
        } else {
            mediaCurrentlyPlayingLabel.setText
                ("Section " + (mediaCurrentSectionIndex + 1) 
                        + " of " + sections.size());
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        // NOTE: MediaPlayer does NOT support https!!
        mediaPlayer 
            = new MediaPlayer(new Media
                (sections.get(mediaCurrentSectionIndex).getUrlForListening()
                                                .replace("https", "http")));
        setMediaPlayerListeners(bookmark);
        mp3LoadProgressIndicator.setDisable(false);
        mp3LoadProgressHBox.setVisible(true);
        mediaPlayerContainerPane.setVisible(true);
    }
    
    private void setMediaPlayerListeners(MyBookmarks.Bookmark bookmark) {
        mediaPlayer.currentTimeProperty().addListener((Observable ov) -> {
            updateMediaValues(bookmark);
        });
        mediaPlayer.setOnPlaying(() -> {
            playButtonImageView.setImage(PAUSE_BUTTON_IMAGE);
        });
        mediaPlayer.setOnPaused(() -> {
            playButtonImageView.setImage(PLAY_BUTTON_IMAGE);
        });
        mediaPlayer.setOnReady(() -> {
            mediaFullDuration = mediaPlayer.getMedia().getDuration();
            mediaFullDurationLabel.setText
                    (formatTime(mediaFullDuration, mediaFullDuration));
            updateMediaValues(null);
            mp3LoadProgressIndicator.setDisable(true);
            mp3LoadProgressHBox.setVisible(false);
            mediaCurrentTimeLabel.setVisible(true);
            mediaFullDurationLabel.setVisible(true);
            if (bookmark == null) {
                mediaPlayer.play();
            } else {
                mediaPlayer.setStartTime(bookmark.getCurrentTime());
                mediaPlayer.pause();
                playButtonImageView.setImage(PLAY_BUTTON_IMAGE);
                updateMediaValues(bookmark);
                Utilities.showTooltip
                    (mainStage, mediaCurrentlyPlayingLabel, 
                            "CLICK PLAY\nTO RESUME AT\nBOOKMARK", null);
            }
        });
        mediaPlayer.setCycleCount(1);
        mediaPlayer.setOnEndOfMedia(() -> {
            if (mediaCurrentSectionIndex 
                    < currentDetailAudiobook.getSections().size() - 1) {
                mediaCurrentSectionIndex++;
                playMedia(null);
            } else {
                mediaPlayerContainerPane.setVisible(false);
                for (ImageView imageView : sectionMediaImageViews) {
                    imageView.setImage(PLAY_BUTTON_IMAGE);
                }
                playButtonImageView.setImage(PLAY_BUTTON_IMAGE);
            }
        });        
    }

    private Node getDownloadButtons(Audiobook audiobook) {
        boolean m4bFilesUnavailable = false;
        if (audiobook.getUrlM4bFiles() == null
                || audiobook.getUrlM4bFiles().isEmpty()
                || audiobook.getUrlM4bFiles().get(0).isEmpty()) {
            m4bFilesUnavailable = true;
        }
        Button downloadButton = new Button ("Download");
        downloadButton.setTooltip
            (new Tooltip("Download M4B audiobook file(s) from LibriVox."));
        downloadButton.setTextAlignment(TextAlignment.CENTER);
        downloadButton.setFont(BOLD_FONT);
        if (m4bFilesUnavailable) {
            downloadButton.setDisable(true);
        } else {
            downloadButton.setOnMouseEntered
                ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
            downloadButton.setOnMouseExited
                ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        }
        downloadButton.setOnAction((ActionEvent event) -> {
            showDownloadDialogStage(false, audiobook);
        });
        StackPane downloadBtnStackPane = new StackPane(downloadButton);
        downloadBtnStackPane.setPadding(new Insets(5));
        downloadBtnStackPane.setMinWidth(IMAGE_DETAIL_SIDE_LENGTH/2 - 5);
        downloadBtnStackPane.setAlignment(Pos.CENTER_RIGHT);
        Button downloadImportButton = new Button("Download &\nimport to iTunes");
        downloadImportButton.setTooltip
            (new Tooltip("Download M4B audiobook file(s) from LibriVox " 
                            + "and import into iTunes."));
        downloadImportButton.setTextAlignment(TextAlignment.CENTER);
        downloadImportButton.setFont(SMALLEST_BOLD_FONT);
        if (m4bFilesUnavailable) {
            downloadImportButton.setDisable(true);
        } else {
            downloadImportButton.setOnMouseEntered
                ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
            downloadImportButton.setOnMouseExited
                ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        }
        if (iTunesImportDirectory == null) {
            downloadImportButton.setDisable(true);
        }
        downloadImportButton.setOnAction((ActionEvent event) -> {
            showDownloadDialogStage(true, audiobook);
        });
        StackPane downloadImportBtnStackPane 
                                = new StackPane(downloadImportButton);
        downloadImportBtnStackPane.setPadding(new Insets(5));
        downloadImportBtnStackPane.setMinWidth(IMAGE_DETAIL_SIDE_LENGTH/2 + 5);
        downloadImportBtnStackPane.setAlignment(Pos.CENTER_LEFT);
        HBox downloadHBox 
                = new HBox(downloadBtnStackPane,downloadImportBtnStackPane);
        downloadHBox.setPadding(new Insets(10,0,10,0));
        return downloadHBox;
    }
    
    private void showDownloadDialogStage 
                (boolean importToItunes, Audiobook audiobook) {
        downloadDialogStage = new Stage();
        downloadDialogStage.initStyle(StageStyle.UTILITY);
        downloadDialogStage.initModality(Modality.APPLICATION_MODAL);
        downloadDialogStage.setResizable(false);
        downloadDialogStage.initOwner(mainStage);
        BorderPane downloadImportBorderPane = new BorderPane();
        
        /* setup top pane */
        downloadImportBorderPane.setTop
            (getAudiobookImageNode(audiobook,IMAGE_DETAIL_SIDE_LENGTH,false,true));
        
        /* setup center pane */
        String importString = importToItunes? "/import" : "";
        Label waitText = new Label("Preparing for download" 
                                    + importString + ". Please wait...");
        waitText.setTextAlignment(TextAlignment.CENTER);
        waitText.setFont(BOLD_FONT);
        waitText.setPadding(INSETS_10);
        ProgressIndicator waitPI = new ProgressIndicator();
        waitPI.setPrefSize(35, 35);
        VBox centerVBox = new VBox(waitText,waitPI);
        centerVBox.setAlignment(Pos.CENTER);
        downloadImportBorderPane.setCenter(centerVBox);
        
        /* setup bottom pane */
        Button okButton = new Button("OK");
        okButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        okButton.setOnAction(getDownloadHandler(importToItunes,audiobook));
        Button cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        cancelButton.setOnAction((ActionEvent e) -> {
            downloadDialogStage.close(); });
        GridPane dialogButtonGridPane = new GridPane();
        dialogButtonGridPane.addRow(0,okButton,cancelButton);
        dialogButtonGridPane.setHgap(10);
        dialogButtonGridPane.setPadding(INSETS_10);
        dialogButtonGridPane.setVisible(false);
        HBox dialogBottomHBox = new HBox(dialogButtonGridPane);
        dialogBottomHBox.setMinWidth(DIALOG_WIDTH);
        dialogBottomHBox.setAlignment(Pos.BOTTOM_RIGHT);
        downloadImportBorderPane.setBottom(dialogBottomHBox);
        
        downloadImportBorderPane.setBackground(DOWNLOAD_PANES_BACKGROUND);
        Scene downloadImportScene 
            = new Scene(downloadImportBorderPane, DIALOG_WIDTH, DIALOG_HEIGHT);
        downloadDialogStage.setScene(downloadImportScene);
        
        final Task<String> downloadPrepTask = getDownloadPrepTask(audiobook);
        downloadPrepTask.setOnSucceeded((WorkerStateEvent t) -> {
            String m4bSizeString = downloadPrepTask.getValue();
            Text downloadConfirmationText = new Text();
            downloadConfirmationText.setTextAlignment(TextAlignment.CENTER);
            downloadConfirmationText.setLineSpacing(5);
            downloadConfirmationText.setFont(BOLD_FONT);
            if (m4bSizeString.isEmpty()) {
                downloadConfirmationText.setText("M4B file(s) inaccessible!");
                okButton.setDisable(true);
            } else {
                downloadConfirmationText.setText
                    (m4bSizeString + "\nProceed with download" 
                                            + importString + "?");
            }
            downloadImportBorderPane.setCenter(downloadConfirmationText);
            dialogButtonGridPane.setVisible(true);
            // addEventFilter -- see: http://stackoverflow.com/questions/2166976
            downloadDialogStage.addEventFilter(KeyEvent.KEY_PRESSED, 
                (KeyEvent ke) -> { 
                    if (ke.getCode().equals(KeyCode.ESCAPE)) {
                        downloadDialogStage.close(); 
                        ke.consume();
                    }
                    if (ke.getCode().equals(KeyCode.ENTER)) {
                        if (okButton.isFocused()) {
                            okButton.fire();
                        }
                        if (cancelButton.isFocused()) {
                            cancelButton.fire();
                        }
                    }
                });
        });
        downloadDialogStage.show();
        new Thread(downloadPrepTask).start();
    }
                
    private Task<String> getDownloadPrepTask (Audiobook audiobook) {
        return new Task<String>() { @Override protected String call() {
            String m4bSizeString = "";
            if (audiobook.getUrlM4bFiles() != null 
                    && !audiobook.getUrlM4bFiles().isEmpty()) {
                long m4bSize = audiobook.getM4bSize();
                if (m4bSize > 0) {
                    long m4bMB = m4bSize / 1000000;
                    if (m4bSize % 1000000 >= 500000) {
                        m4bMB++;
                    }
                    m4bSizeString 
                        = "This work consists of " 
                            + audiobook.getUrlM4bFiles().size()
                            + " audiobook file" 
                            + ((audiobook.getUrlM4bFiles().size() > 1) ? "s" : "")
                            + " totaling " + String.valueOf(m4bMB) + "MB.";
                }
            }
            return m4bSizeString;
        }};
    }

    private EventHandler<ActionEvent> getDownloadHandler 
                        (boolean importToItunes, Audiobook audiobook) {
        return (ActionEvent e) -> {
            if (importToItunes) {
                targetDirectory = iTunesImportDirectory;
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(userDownloadDirectory);
                directoryChooser.setTitle("Select target folder for audiobook");
                File selectedDirectory 
                        = directoryChooser.showDialog(downloadDialogStage);
                if (selectedDirectory == null) {
                    downloadDialogStage.close();
                    return;
                } else {
                    userDownloadDirectory = selectedDirectory;
                    targetDirectory = selectedDirectory;
                }
            }
            Stage downloadProgressStage = new Stage();
            if (importToItunes) {
                downloadProgressStage.setTitle("Download & import to iTunes");
            } else {
                downloadProgressStage.setTitle("Download audiobook");
            }
            downloadProgressStage.setResizable(false);
            downloadProgressStage.initOwner(mainStage);
            
            Label downloadProgressLabel = new Label("Download\nin Progress");
            downloadProgressLabel.setWrapText(true);
            downloadProgressLabel.setFont(BOLD_BIGGEST_FONT);
            downloadProgressLabel.setAlignment(Pos.CENTER);
            downloadProgressLabel.setPadding(new Insets(20));
            downloadProgressLabel.setMinWidth(145);
            downloadProgressLabel.setMaxWidth(145);

            // set up circles to show subtask progress
            List<Circle> subtaskProgressCircles = new ArrayList<>();
            List<HBox> subtaskProgressHBoxes = new ArrayList<>();
            if (audiobook.getUrlM4bFiles() != null) {
                for (int i=0; i < audiobook.getUrlM4bFiles().size(); i++) {
                    Circle circle = new Circle(8,Color.WHITE);
                    circle.setStroke(Color.DARKGREEN);
                    subtaskProgressCircles.add(circle);
                    HBox hBox = new HBox(circle);
                    hBox.setPadding(new Insets(2));
                    subtaskProgressHBoxes.add(hBox);
                }
            }
            HBox subtaskProgressHBox = new HBox();
            if (subtaskProgressCircles.size() > 1) {
                subtaskProgressHBox.getChildren().addAll(subtaskProgressHBoxes);
            }
            subtaskProgressHBox.setPrefWidth(PROGRESS_BAR_WIDTH);
            subtaskProgressHBox.setAlignment(Pos.CENTER);

            /* setup bottom (cancel) pane */
            Button cancelButton = new Button("Cancel");
            cancelButton.setMinWidth(DIALOG_BUTTON_WIDTH);
            // following statement: http://stackoverflow.com/questions/24483686/
            cancelButton.setOnAction((ActionEvent ae) -> {
                downloadProgressStage.fireEvent(new WindowEvent
                    (downloadProgressStage,WindowEvent.WINDOW_CLOSE_REQUEST));});
            GridPane dialogButtonGridPane = new GridPane();
            dialogButtonGridPane.addRow(0,cancelButton);
            dialogButtonGridPane.setHgap(10);
            dialogButtonGridPane.setPadding(INSETS_10);
            HBox dialogBottomHBox = new HBox(dialogButtonGridPane);
            dialogBottomHBox.setMinWidth(DIALOG_WIDTH);
            dialogBottomHBox.setAlignment(Pos.BOTTOM_RIGHT);
            
            final Task<Void> downloadM4bFilesTask 
                = new Task<Void>() {
                    @Override protected Void call() 
                        throws MalformedURLException, IOException, InterruptedException {
                        try { audiobook.downloadM4bFiles
                            (targetDirectory,
                            new CatalogCallback () {
                                @Override
                                public void updateTaskProgress
                                                (long workDone, long max) {
                                    updateProgress(workDone,max);
                                }
                                @Override
                                public void updateTaskMessage(String message) {
                                    updateMessage(message);
                                }
                                @Override
                                public void updateSubtasks
                                        (int subtasksDone, int subtasks) {
                                    Platform.runLater(() -> {
                                        int subtaskCount = 0;
                                        for (Circle subtaskCircle 
                                                        : subtaskProgressCircles) {
                                            if (++subtaskCount <= subtasksDone) {
                                                subtaskCircle.setFill(Color.GREEN);
                                            } else {
                                                subtaskCircle.setFill(Color.YELLOW);
                                                break;
                                            }
                                        }
                                    }); 
                                }
                            });
                        } catch (InterruptedException e) {
                            if (this.isCancelled()) {
                                downloadShutdownCompleted.set(true);
                                return null;
                            } else {
                                throw e;
                            }
                        }
                        downloadShutdownCompleted.set(true);
                        return null;
                    }
                };
            downloadM4bFilesTask.setOnSucceeded((WorkerStateEvent t) -> {
                Platform.runLater(() -> {
                    cancelButton.setText("Close");
                    String importString = importToItunes? 
                        "\n\nRestart iTunes to complete importation." : "";
                    downloadProgressLabel.setText
                                    ("Download is complete." + importString);
                    downloadProgressLabel.setFont(BOLD_BIGGER_FONT);
                });
            });
            /** If user closes this window while the download task
             * is running, user confirmation needed for task cancellation. */
            downloadProgressStage.setOnCloseRequest((WindowEvent we) -> { 
                if (downloadM4bFilesTask.isRunning()) {
                    we.consume();
                    getCancelDownloadConfirmation
                        (downloadM4bFilesTask, downloadProgressStage);
                } 
            });
            // set up progress bar and progress text for download
            Label downloadProgressText = new Label();
            downloadProgressText.setFont(SMALLEST_FONT);
            downloadProgressText.setTextFill(Color.DARKGREEN);
            downloadProgressText.textProperty().bind
                                        (downloadM4bFilesTask.messageProperty());
            ProgressBar downloadProgressBar = new ProgressBar();
            downloadProgressBar.setPrefSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
            downloadProgressBar.setStyle("-fx-accent: lightgreen;");
            downloadProgressBar.progressProperty().bind
                                        (downloadM4bFilesTask.progressProperty());
            StackPane progressStackPane = new StackPane();
            progressStackPane.getChildren().addAll
                                    (downloadProgressBar, downloadProgressText);
            progressStackPane.setPrefSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT+5);
            HBox progressBarHBox = new HBox(progressStackPane); 
            progressBarHBox.setAlignment(Pos.CENTER);
            progressBarHBox.setMaxHeight(PROGRESS_BAR_HEIGHT+5);
            progressBarHBox.setStyle("-fx-padding: 3; ");
            
            HBox eyeCandyHBox = new HBox
                    (getAudiobookImageNode
                                (audiobook,IMAGE_DETAIL_SIDE_LENGTH,false,true),
                                        downloadProgressLabel);
            eyeCandyHBox.setAlignment(Pos.CENTER);
            
            VBox downloadProgressVBox 
                = new VBox(eyeCandyHBox,progressBarHBox,
                                subtaskProgressHBox,dialogBottomHBox);
            downloadProgressVBox.setAlignment(Pos.CENTER);
            downloadProgressVBox.setBackground(DOWNLOAD_PANES_BACKGROUND);
            
            Scene downloadProgressScene = new Scene(downloadProgressVBox);
            downloadProgressStage.setScene(downloadProgressScene);
            downloadDialogStage.close();
            downloadProgressStage.show();
            downloadExecutor.execute(downloadM4bFilesTask);
        };
    }

    private void getCancelDownloadConfirmation
            (Task<Void> downloadM4bFilesTask, Stage ownerStage) {
        final boolean shutdownApplication
                            = (downloadM4bFilesTask == null)? true : false;
        Stage cancelDownloadConfirmationStage = new Stage();
        cancelDownloadConfirmationStage.initStyle(StageStyle.UTILITY);
        cancelDownloadConfirmationStage.setTitle("Confirm download cancellation");
        cancelDownloadConfirmationStage.setResizable(false);
        cancelDownloadConfirmationStage.initOwner(ownerStage);
        cancelDownloadConfirmationStage.initModality(Modality.APPLICATION_MODAL);
        
        BorderPane cancelDownloadConfirmationPane = new BorderPane();
        cancelDownloadConfirmationPane.setBackground(DOWNLOAD_PANES_BACKGROUND);
        
        Label confirmCancelDownloadLabel 
                = new Label("Download(s) in progress.\nCancel or wait?");
        confirmCancelDownloadLabel.setTextAlignment(TextAlignment.CENTER);
        confirmCancelDownloadLabel.setFont(BOLD_BIGGEST_FONT);
        cancelDownloadConfirmationPane.setCenter(confirmCancelDownloadLabel);
        
        /* setup bottom pane */
        Button cancelAndExitButton = new Button("Cancel\n& Exit");
        cancelAndExitButton.setTextAlignment(TextAlignment.CENTER);
        cancelAndExitButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        cancelAndExitButton.setOnAction((ActionEvent e) -> {
            if (shutdownApplication) {
                if (downloadExecutor.getActiveCount() > 0) {
                    downloadExecutor.shutdownNow();
                    while (!downloadExecutor.isTerminated()) {
                        try { Thread.sleep(200);
                        } catch(InterruptedException ex) 
                            { Thread.currentThread().interrupt(); } 
                    }
                }
                cancelDownloadConfirmationStage.close();
            } else {
                if (downloadM4bFilesTask.isRunning()) {
                    downloadShutdownCompleted.set(false);
                    downloadM4bFilesTask.cancel();
                    while (downloadShutdownCompleted.isFalse()) {
                        try { Thread.sleep(200);
                        } catch(InterruptedException ex) 
                            { Thread.currentThread().interrupt(); } 
                    }
                }
                cancelDownloadConfirmationStage.close();
                ownerStage.close();
            }
            if (shutdownApplication) {
                doShutdownSequence();
            }
        });
        Button waitButton = new Button("Wait for\nDownload");
        waitButton.setTextAlignment(TextAlignment.CENTER);
        waitButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        waitButton.setOnAction((ActionEvent e) -> {
            cancelDownloadConfirmationStage.close(); });
        GridPane dialogButtonGridPane = new GridPane();
        dialogButtonGridPane.addRow(0,cancelAndExitButton,waitButton);
        dialogButtonGridPane.setHgap(10);
        dialogButtonGridPane.setPadding(INSETS_10);
        HBox dialogBottomHBox = new HBox(dialogButtonGridPane);
        dialogBottomHBox.setMinWidth(DIALOG_WIDTH);
        dialogBottomHBox.setAlignment(Pos.BOTTOM_CENTER);
        cancelDownloadConfirmationPane.setBottom(dialogBottomHBox);
        
        cancelDownloadConfirmationStage.setScene
                                (new Scene(cancelDownloadConfirmationPane));
        cancelDownloadConfirmationStage.show();
    }
                        
    private void getBookmarkConfirmation 
            (boolean shutdownApplication, Stage ownerStage, 
                                    Audiobook audiobook, int sectionIndex) {
        if (mediaPlayer == null) {
            return;
        }
        Duration currentTime;
        if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)
                || mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)){
            mediaPlayer.pause();
            currentTime = mediaPlayer.getCurrentTime();
        } else {
            return;
        }
        mediaPlayer.stop();
        if (PersistedAppSettings.getBookmarkSuppressSetting().equals
                (PersistedAppSettings.BookmarkSuppressSetting.BOOKMARKS_SUPPRESS)) {
            /* remove any pre-existing bookmarks for this audiobook */
            myBookmarks.remove(audiobook);
            findExpandedPanes(ExpandedPanesOption.REFRESH,true);
            return;
        }

        Stage bookmarkConfirmationStage = new Stage();
        bookmarkConfirmationStage.initStyle(StageStyle.UTILITY);
        bookmarkConfirmationStage.setTitle("Bookmark it?");
        bookmarkConfirmationStage.setResizable(false);
        bookmarkConfirmationStage.initOwner(ownerStage);
        bookmarkConfirmationStage.initModality(Modality.APPLICATION_MODAL);
        
        BorderPane bookmarkConfirmationPane = new BorderPane();
        bookmarkConfirmationPane.setBackground(GRAY_BACKGROUND);
        
        /* setup top pane */
        bookmarkConfirmationPane.setTop
            (getAudiobookImageNode(audiobook,IMAGE_DETAIL_SIDE_LENGTH,false,true));
        
        Label bookmarkItBigLabel = new Label("BOOKMARK IT?");
        bookmarkItBigLabel.setAlignment(Pos.CENTER);
        bookmarkItBigLabel.setTextAlignment(TextAlignment.CENTER);
        bookmarkItBigLabel.setFont(BOLD_BIGGEST_FONT);
        bookmarkItBigLabel.setPadding(new Insets(20,20,0,20));
        StackPane bookmarkItBigLabelStackPane = new StackPane(bookmarkItBigLabel);
        Label bookmarkConfirmationLabel 
                = new Label("Bookmark your current location in the audio file?");
        bookmarkConfirmationLabel.setFont(BOLD_BIGGER_FONT);
        bookmarkConfirmationLabel.setPadding(new Insets(10,20,0,20));
        StackPane bookmarkConfirmationLabelStackPane 
                                    = new StackPane(bookmarkConfirmationLabel);
        VBox bookmarkLabelVBox 
                = new VBox(bookmarkItBigLabelStackPane, 
                                        bookmarkConfirmationLabelStackPane);
        bookmarkConfirmationPane.setCenter(bookmarkLabelVBox);
        
        Button yesButton = new Button("Yes*");
        yesButton.setTextAlignment(TextAlignment.CENTER);
        yesButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        yesButton.setOnAction((ActionEvent e) -> {
            myBookmarks.add(audiobook, sectionIndex, currentTime);
            bookmarkConfirmationStage.close();
            findExpandedPanes(ExpandedPanesOption.REFRESH,true);
            if (shutdownApplication) doShutdownSequence();
        });
        Button noButton = new Button("No");
        noButton.setTextAlignment(TextAlignment.CENTER);
        noButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        noButton.setOnAction((ActionEvent e) -> {
            /* remove any pre-existing bookmarks for this audiobook */
            myBookmarks.remove(audiobook);
            bookmarkConfirmationStage.close();
            findExpandedPanes(ExpandedPanesOption.REFRESH,true);
            if (shutdownApplication) doShutdownSequence();
        });
        /* close request considered to be synonymous with clicking "No" button */
        bookmarkConfirmationStage.setOnCloseRequest((WindowEvent we) -> { 
            noButton.fire();
        });
        GridPane dialogButtonGridPane = new GridPane();
        dialogButtonGridPane.addRow(0,yesButton,noButton);
        dialogButtonGridPane.setHgap(10);
        dialogButtonGridPane.setPadding(INSETS_10);
        HBox dialogBottomHBox = new HBox(dialogButtonGridPane);
        dialogBottomHBox.setMinWidth(DIALOG_WIDTH);
        dialogBottomHBox.setAlignment(Pos.BOTTOM_CENTER);
        Label bookmarkInfoLabel 
                = new Label("*All bookmarked audiobooks may be accessed by "
                        + "clicking on the \"MY LIST\" button and then "
                        + "selecting the \"MY BOOKMARKED AUDIOBOOKS\" "
                        + "accordion pane.");
        bookmarkInfoLabel.setPadding(new Insets(0,20,20,20));
        bookmarkInfoLabel.setMaxWidth(DIALOG_WIDTH + 60);
        bookmarkInfoLabel.setWrapText(true);
        bookmarkInfoLabel.setFont(SMALLER_FONT);
        StackPane bookmarkInfoLabelStackPane = new StackPane(bookmarkInfoLabel);
         
        CheckBox bookmarkSettingsCB 
                    = new CheckBox("Disable bookmarking of current location when "
                                    + "listening to audio files.");
        bookmarkSettingsCB.setFont(BOLD_FONT);
        bookmarkSettingsCB.setPadding(new Insets(10));
        bookmarkSettingsCB.setOnMouseClicked((MouseEvent me)->{
            if (bookmarkSettingsCB.isSelected()) {
                //USER_OPTION_PREFERENCES.put
                //        (BOOKMARK_PREFERENCE_KEY, BOOKMARKS_SUPPRESS);
                PersistedAppSettings.setBookmarkSuppressSetting
                    (PersistedAppSettings.BookmarkSuppressSetting.BOOKMARKS_SUPPRESS);
                yesButton.setDisable(true);
            } else {
                //USER_OPTION_PREFERENCES.put
                //        (BOOKMARK_PREFERENCE_KEY, BOOKMARKS_ALLOW);
                PersistedAppSettings.setBookmarkSuppressSetting
                    (PersistedAppSettings.BookmarkSuppressSetting.BOOKMARKS_ALLOW);
                yesButton.setDisable(false);
            }
        });
       
        VBox dialogBottomVBox 
                = new VBox(dialogBottomHBox,bookmarkInfoLabelStackPane,
                                            bookmarkSettingsCB);
        bookmarkConfirmationPane.setBottom(dialogBottomVBox);
        
        bookmarkConfirmationStage.setScene
                                (new Scene(bookmarkConfirmationPane));
        bookmarkConfirmationStage.show();
    }
                        
    private Node getMiscInfoPane(Audiobook audiobook) {
        VBox infoVBox = new VBox();
        final double INFO_ID_WIDTH_FACTOR = 0.3;
        infoVBox.setAlignment(Pos.TOP_CENTER);
        infoVBox.setMinWidth(IMAGE_DETAIL_SIDE_LENGTH + 10);
        infoVBox.setMaxWidth(IMAGE_DETAIL_SIDE_LENGTH + 10);
        if (audiobook.getGenres() != null && !audiobook.getGenres().isEmpty()) {
            List<String> genreStringList = new ArrayList<>();
            for (Genre genre : audiobook.getGenres()) {
                genreStringList.add(genre.getName());
            }
            infoVBox.getChildren().add(getInfoItemPane
                (genreStringList, "Genre", INFO_ID_WIDTH_FACTOR));
        }
        if (audiobook.getAllLanguages() != null 
                && !audiobook.getAllLanguages().isEmpty()) {
            List<String> languageStringList = new ArrayList<>();
            for (Language language : audiobook.getAllLanguages()) {
                languageStringList.add(language.getLanguage());
            }
            infoVBox.getChildren().add(getInfoItemPane
                (languageStringList, "Language", INFO_ID_WIDTH_FACTOR));
        }
        if (audiobook.getTranslators() != null
                && !audiobook.getTranslators().isEmpty()) {
            List<String> translatorStringList = new ArrayList<>();
            for (Translator translator : audiobook.getTranslators()) {
                StringBuilder translatorString = new StringBuilder();
                if (!translator.getFirstName().isEmpty()) {
                    translatorString.append
                                    (translator.getFirstName()).append(" ");
                }
                translatorString.append(translator.getLastName());
                translatorStringList.add(translatorString.toString());
            }
            infoVBox.getChildren().add(getInfoItemPane
                (translatorStringList, "Translator", INFO_ID_WIDTH_FACTOR));
        }
        if (audiobook.getDurationInSeconds() > 0) {
            infoVBox.getChildren().add(getInfoItemPane
                (getDurationString(audiobook), "Duration", INFO_ID_WIDTH_FACTOR));
        }
        if (audiobook.getDownloadCountInternetArchive() > 0) {
            String downloadString = String.format
                            ("%,d",audiobook.getDownloadCountInternetArchive());
            infoVBox.getChildren().add(getInfoItemPane
                    (downloadString, "Downloads", INFO_ID_WIDTH_FACTOR));
        }
        return infoVBox;
    }
    
    private String getDurationString (Work work) {
        int totalSeconds = work.getDurationInSeconds();
        int totalMinutes = totalSeconds / 60;
        if (totalSeconds % 60 >= 30) {
            totalMinutes++;
        }
        int minutes = totalMinutes % 60;
        int hours = totalMinutes / 60;
        StringBuilder hoursTemplate = new StringBuilder("%d hour");
        StringBuilder minutesTemplate = new StringBuilder("%d minute");
        if (hours != 1) { 
            hoursTemplate.append("s"); }
        if (minutes != 1) { 
            minutesTemplate.append("s"); }
        String durationTemplate;
        String durationString; 
        if (hours == 0) {
            durationTemplate = minutesTemplate.toString();
            durationString = String.format(durationTemplate, minutes);
        } else {
            if (minutes == 0) {
                durationTemplate = hoursTemplate.toString();
            } else {
                durationTemplate = hoursTemplate.toString() + ", " 
                                        + minutesTemplate.toString();
            }
            durationString = String.format(durationTemplate, hours, minutes);
        }
        return durationString;
    }

    private Node getInfoItemPane 
            (String item, String identifier, double identifierWidth) {
        List<String> items = new ArrayList<>();
        items.add(item);
        return getInfoItemPane(items,identifier,identifierWidth);
    }
            
    private Node getInfoItemPane 
            (List<String> items, String identifier, double identifierWidthFactor) {
        String labelString = "";
        if (!identifier.isEmpty()) {
            labelString = (items.size() == 1) ? 
                                    (identifier + ":") : (identifier + "s:");
        }
        Label identifierLabel = getIdentifierLabel(labelString, identifierWidthFactor);

        StringBuilder commaDelimitedList = new StringBuilder();
        boolean firstAppended = false;
        for (String item : items) {
            if (firstAppended) {
                commaDelimitedList.append(", ");
            } else {
                firstAppended = true;
            }
            commaDelimitedList.append(item);
        }
        Label itemsLabel = new Label(commaDelimitedList.toString());
        itemsLabel.setAlignment(Pos.CENTER_LEFT);
        itemsLabel.setFont(SUPERSMALL_BOLD_FONT);
        itemsLabel.setMinWidth
            ((IMAGE_DETAIL_SIDE_LENGTH * (1 - identifierWidthFactor)) + 10);
        itemsLabel.setMaxWidth
            ((IMAGE_DETAIL_SIDE_LENGTH * (1 - identifierWidthFactor)) + 10);
        itemsLabel.setTextFill(Color.WHITE);
        itemsLabel.setWrapText(true);
        return new HBox(identifierLabel,itemsLabel);
    }
    
    private Label getIdentifierLabel(String labelString, double widthFactor) {
        Label identifierLabel = new Label(labelString);
        identifierLabel.setAlignment(Pos.CENTER_RIGHT);
        identifierLabel.setMinWidth(IMAGE_DETAIL_SIDE_LENGTH * widthFactor);
        identifierLabel.setMaxWidth(IMAGE_DETAIL_SIDE_LENGTH * widthFactor);
        identifierLabel.setFont(SUPERSMALL_ITALIC_FONT);
        identifierLabel.setTextFill(Color.WHITE);
        identifierLabel.setPadding(new Insets(0,5,0,0));
        return identifierLabel;
    }
            
    private Node getDatesPane(Audiobook audiobook) {
        VBox datesVBox = new VBox();
        final double DATES_ID_WIDTH_FACTOR = 0.6;
        if (audiobook.getCopyrightYear() != null
                && !audiobook.getCopyrightYear().isEmpty()
                && !audiobook.getCopyrightYear().equals("0")) {
            datesVBox.getChildren().add(getInfoItemPane
                (audiobook.getCopyrightYear(), 
                    "Year originally published", DATES_ID_WIDTH_FACTOR));
        }
        if (audiobook.getPublicationDateInternetArchive() != null
                && !audiobook.getPublicationDateInternetArchive().isEmpty()) {
            datesVBox.getChildren().add(getInfoItemPane
                (audiobook.getPublicationDateInternetArchive().substring(0,10), 
                    "Date audiobook released", DATES_ID_WIDTH_FACTOR));
        }
        return datesVBox;
    }
    
    private Node getLinksPane(Audiobook audiobook) {
        final double LINKS_ID_WIDTH_FACTOR = 0.4;
        Label identifierLabel 
            = getIdentifierLabel("Go to this work's:", LINKS_ID_WIDTH_FACTOR);
        VBox linksVBox = new VBox();
        Label urlTextLabel = null;
        if (audiobook.getUrlTextSource() != null
                && !audiobook.getUrlTextSource().isEmpty()) {
            urlTextLabel = getLinkLabel("Public domain text", 
                        audiobook.getUrlTextSource(), LINKS_ID_WIDTH_FACTOR);
            linksVBox.getChildren().add(urlTextLabel);
        }
        if (audiobook.getUrlLibrivox() != null
                && !audiobook.getUrlLibrivox().isEmpty()) {
            urlTextLabel = getLinkLabel("LibriVox page", 
                        audiobook.getUrlLibrivox(), LINKS_ID_WIDTH_FACTOR);
            linksVBox.getChildren().add(urlTextLabel);
        }
        if (audiobook.getUrlInternetArchive() != null
                && !audiobook.getUrlInternetArchive().isEmpty()) {
            urlTextLabel = getLinkLabel("Internet Archive page", 
                    audiobook.getUrlInternetArchive(), LINKS_ID_WIDTH_FACTOR);
            linksVBox.getChildren().add(urlTextLabel);
        }
        HBox linksHBox = new HBox();
        if (urlTextLabel != null) {
            linksHBox.getChildren().addAll(identifierLabel, linksVBox);
        }
        return linksHBox;
    }
    
    private Label getLinkLabel 
            (String labelText, String urlString, double identifierWidthFactor) {
        Label linkLabel = new Label(labelText);
        linkLabel.setTooltip(new Tooltip(urlString));
        linkLabel.setAlignment(Pos.CENTER_LEFT);
        linkLabel.setFont(SUPERSMALL_BOLD_FONT);
        linkLabel.setMinWidth
            ((IMAGE_DETAIL_SIDE_LENGTH * (1 - identifierWidthFactor)) + 10);
        linkLabel.setMaxWidth
            ((IMAGE_DETAIL_SIDE_LENGTH * (1 - identifierWidthFactor)) + 10);
        linkLabel.setTextFill(Color.POWDERBLUE);
        linkLabel.setBackground(BLACK_BACKGROUND);
        linkLabel.setWrapText(true);
        linkLabel.setUnderline(true);
        linkLabel.setOnMouseClicked
            ((MouseEvent me)->{getHostServices().showDocument(urlString);});
        linkLabel.setOnMouseEntered
            ((MouseEvent me)->{mainScene.setCursor(Cursor.HAND);});
        linkLabel.setOnMouseExited
            ((MouseEvent me)->{mainScene.setCursor(Cursor.DEFAULT);});
        return linkLabel;
    }
    
    private StackPane getArrowStackPane (ArrowType arrowType) {
        Polygon arrow;
        if (arrowType.equals(ArrowType.RIGHT_ARROW)) {
            arrow = new Polygon(new double[]{0,0 , 0,60 , 25,30 });
        } else {
            assert arrowType.equals(ArrowType.LEFT_ARROW);
            arrow = new Polygon(new double[]{0,30 , 25,0 , 25,60 });
        }
        arrow.setFill(Color.WHITE);
        HBox arrowBox = new HBox();
        arrowBox.setPrefSize(45, IMAGE_SIDE_LENGTH + 35);
        arrowBox.setBackground(BLACK_BACKGROUND);
        arrowBox.setOpacity(0.3);
        return new StackPane(arrowBox,arrow);
    }
 
    private Node getQuickBrowseBottomPane 
            (OverlayPaneOption paneOption, String searchString) {
        ScrollPane centerScrollPane = getCenterScrollPane(0.80);
        final int ITEM_CAPACITY = 50;
        final int SEARCH_ITEM_CAPACITY = 20;
        FinalInteger searchStartIndex = new FinalInteger(0);
        List<Work> searchResultList = new ArrayList<>();
        String titleText = paneOption.getTitle();
        Label titleLabel = new Label(titleText);
        titleLabel.setTextFill(Color.GREY);
        titleLabel.setFont(BOLD_ENORMOUS_FONT);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.8);
        titleLabel.setPadding(new Insets(0,0,0,30));
        ImageView libriVoxLogo = new ImageView
            (new Image(getClass().getResourceAsStream(LIBRIVOX_LOGO_JPG_FILE)));
        libriVoxLogo.setPreserveRatio(true);
        libriVoxLogo.setFitHeight(55);
        titleLabel.setGraphic(libriVoxLogo);
        titleLabel.setContentDisplay(ContentDisplay.LEFT);
        
        FlowPane quickBrowseFlowPane = new FlowPane(Orientation.HORIZONTAL, 5, 10);
        if (paneOption != OverlayPaneOption.SEARCH) {
            quickBrowseFlowPane.getChildren().addAll
                (getAudiobookStackPaneList (PublicationDate.class, null, 
                        paneOption, ITEM_CAPACITY, null).subList(0,ITEM_CAPACITY));
        }
        quickBrowseFlowPane.setBackground(BLACK_BACKGROUND);
        StackPane stackPaneForCentering = new StackPane(quickBrowseFlowPane);
        stackPaneForCentering.setPadding(new Insets(5,5,20,5));
        stackPaneForCentering.setMinSize(DETAIL_WINDOW_WIDTH, 
                                            DETAIL_WINDOW_HEIGHT * 0.75);
        stackPaneForCentering.setBackground(BLACK_BACKGROUND);
            
        /* setup buttons pane */
        final FinalInteger screenLoadCount = new FinalInteger(1);
        Button prevButton = new Button("Previous");
        prevButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        prevButton.setVisible(false);
        
        Button nextButton = new Button();
        switch (paneOption) {
            case NEWEST: case SEARCH:
                nextButton.setText("Next");
                break;
            case RANDOM: 
                nextButton.setText("MORE");
                break;
        }
        nextButton.setMinWidth(DIALOG_BUTTON_WIDTH);
        
        GridPane buttonGridPane = new GridPane();
        buttonGridPane.addRow(0,prevButton,nextButton);
        buttonGridPane.setHgap(10);
        buttonGridPane.setPadding(new Insets(0,10,10,10));
        HBox buttonHBox = new HBox(buttonGridPane);
        buttonHBox.setPadding(new Insets(0,20,0,0));
        buttonHBox.setMinWidth(DETAIL_WINDOW_WIDTH);
        buttonHBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonHBox.setBackground(BLACK_BACKGROUND);

        ProgressIndicator searchPI = new ProgressIndicator();
        searchPI.setMaxSize(250,250);
        searchPI.setBackground(BLACK_BACKGROUND);
        ProgressBar searchProgressBar = new ProgressBar();
        searchProgressBar.setMaxSize(200, PROGRESS_BAR_HEIGHT * 0.5);
        searchProgressBar.setStyle("-fx-accent: lightgreen;");
        StackPane searchPIStackPane = new StackPane(searchPI, searchProgressBar);
        searchPIStackPane.setMinSize(DETAIL_WINDOW_WIDTH, 
                                            DETAIL_WINDOW_HEIGHT * 0.80);
        searchPIStackPane.setBackground(BLACK_BACKGROUND);
        
        String noResultsText 
                = "Your search did not match any audiobooks.\n\n" +
                    "Suggestions:\n" +
                    "    -- Make sure that all words are spelled correctly.\n" +
                    "    -- Try different keywords.\n" +
                    "    -- Try more general keywords.\n" +
                    "    -- Try fewer keywords.";
        String searchInaccessibleText 
                = "The search engine is currently inaccessible.\n\n" +
                    "Suggestions:\n" +
                    "    -- Click blue search button above to resubmit query.\n" +
                    "    -- Select 'Search LibriVox website' option above and " +
                    "click blue search button to open search in external browser.";
        String librivoxInaccessibleText 
                = "The LibriVox webpages are currently inaccessible.\n\n" +
                    "Suggestions:\n" +
                    "    -- Click blue search button above to resubmit query.\n" +
                    "    -- Select 'Search LibriVox website' option above and " +
                    "click blue search button to open search in external browser.";
        Label noResultsLabel = new Label();
        noResultsLabel.setFont(BOLD_BIGGEST_FONT);
        noResultsLabel.setBackground(BLACK_BACKGROUND);
        noResultsLabel.setTextFill(Color.WHITE);
        noResultsLabel.setWrapText(true);
        noResultsLabel.setMaxWidth(DETAIL_WINDOW_WIDTH * 0.7);
        StackPane noResultsStackPane = new StackPane(noResultsLabel);
        noResultsStackPane.setAlignment(Pos.CENTER);
        noResultsStackPane.setBackground(BLACK_BACKGROUND);
        noResultsStackPane.setMinSize(DETAIL_WINDOW_WIDTH, 
                                            DETAIL_WINDOW_HEIGHT * 0.80);
        
        final List<SearchParameters> searchParametersContainer = new ArrayList<>();
        if (paneOption == OverlayPaneOption.SEARCH) {
            centerScrollPane.setContent(searchPIStackPane);
            searchTask = getSearchTask(new SearchParameters(searchString, 
                                                        SEARCH_ITEM_CAPACITY));
            searchProgressBar.progressProperty().bind(searchTask.progressProperty());
            searchTask.setOnSucceeded((WorkerStateEvent t) -> {
                SearchParameters returnedSearchParms = searchTask.getValue();
                Platform.runLater(() -> { 
                    searchResultList.addAll(returnedSearchParms.returnedWorks);
                    searchParametersContainer.clear();
                    searchParametersContainer.add(returnedSearchParms);
                    quickBrowseFlowPane.getChildren().setAll
                        (getAudiobookStackPaneList
                            (null, null, paneOption, 
                                SEARCH_ITEM_CAPACITY, searchResultList));
                     if (returnedSearchParms.endOfSearchEngineResultSet) {
                        nextButton.setVisible(false);
                    }
                    if (returnedSearchParms.responseStatus
                                == SearchParameters.RESPONSE_STATUS.SEARCH_IO_ERROR) {
                        noResultsLabel.setText(searchInaccessibleText);
                        centerScrollPane.setContent(noResultsStackPane);
                    } else if (returnedSearchParms.responseStatus
                                == SearchParameters.RESPONSE_STATUS.LIBRIVOX_IO_ERROR) {
                        noResultsLabel.setText(librivoxInaccessibleText);
                        centerScrollPane.setContent(noResultsStackPane);
                    } else if (returnedSearchParms.returnedWorks.isEmpty()) {
                        noResultsLabel.setText(noResultsText);
                        centerScrollPane.setContent(noResultsStackPane);
                    } else {
                        centerScrollPane.setContent
                            (new VBox(stackPaneForCentering,buttonHBox));
                    }
                });
            });
            new Thread(searchTask).start();
        } else {
            centerScrollPane.setContent(new VBox(stackPaneForCentering,buttonHBox));
        }
        
        prevButton.setOnAction((ActionEvent e) -> { 
            nextButton.setVisible(true);
            if (screenLoadCount.decrement() <= 1) {
                screenLoadCount.set(1);
            }
            int startIndex;
            int listSize;
            switch (paneOption) {
                case NEWEST:
                    startIndex = ITEM_CAPACITY * (screenLoadCount.get() - 1);
                    listSize = ITEM_CAPACITY * screenLoadCount.get();
                    quickBrowseFlowPane.getChildren().setAll
                        (getAudiobookStackPaneList
                            (PublicationDate.class, null, paneOption, listSize, 
                                            null).subList(startIndex, listSize));
                    break;
                case SEARCH:
                    startIndex = SEARCH_ITEM_CAPACITY * (screenLoadCount.get() - 1);
                    listSize = SEARCH_ITEM_CAPACITY * screenLoadCount.get();
                    quickBrowseFlowPane.getChildren().setAll
                        (getAudiobookStackPaneList
                            (null, null, paneOption, listSize, searchResultList)
                                                .subList(startIndex, listSize));
                    break;
            }
            centerScrollPane.setVvalue(centerScrollPane.getVmin());
            if (screenLoadCount.get() == 1) {
                /* Note that Button#setVisible somehow interferes with 
                        ScrollPane#setVvalue. */
                    prevButton.setVisible(false); 
            }
        });
        
        nextButton.setOnAction((ActionEvent e) -> { 
            int startIndex;
            int listSize;
            switch (paneOption) {
                case NEWEST:
                    prevButton.setVisible(true);
                    startIndex = ITEM_CAPACITY * screenLoadCount.get();
                    listSize = ITEM_CAPACITY * screenLoadCount.increment();
                    quickBrowseFlowPane.getChildren().setAll
                        (getAudiobookStackPaneList
                            (PublicationDate.class, null, paneOption, listSize, null)
                                                .subList(startIndex, listSize));
                    break;
                case RANDOM:
                    quickBrowseFlowPane.getChildren().setAll
                        (getAudiobookStackPaneList
                            (null, null, paneOption, ITEM_CAPACITY, null));
                    break;
                case SEARCH:
                    prevButton.setVisible(true);
                    startIndex = SEARCH_ITEM_CAPACITY * screenLoadCount.get();
                    listSize = SEARCH_ITEM_CAPACITY * screenLoadCount.increment();
                    if (listSize <= searchResultList.size()) {
                        quickBrowseFlowPane.getChildren().setAll
                            (getAudiobookStackPaneList
                                (null, null,paneOption,listSize,
                                    searchResultList).subList(startIndex, listSize));
                    } else {
                        centerScrollPane.setContent(searchPIStackPane);
                        searchTask = getSearchTask(searchParametersContainer.get(0));
                        searchProgressBar.progressProperty().bind
                                        (searchTask.progressProperty());
                        searchTask.setOnSucceeded((WorkerStateEvent t) -> {
                            SearchParameters returnedSearchParms 
                                                    = searchTask.getValue();
                            Platform.runLater(() -> { 
                                searchResultList.addAll
                                            (returnedSearchParms.returnedWorks);
                                searchParametersContainer.set(0,returnedSearchParms);
                                if (searchParametersContainer.get(0)
                                                .endOfSearchEngineResultSet) {
                                    nextButton.setVisible(false);
                                }
                                quickBrowseFlowPane.getChildren().setAll
                                    (getAudiobookStackPaneList
                                        (null,null,paneOption,listSize,
                                            searchResultList).subList(startIndex, listSize));
                                centerScrollPane.setContent
                                    (new VBox(stackPaneForCentering,buttonHBox));
                            });
                        });
                        new Thread(searchTask).start();
                    }
                    break;
            }
            centerScrollPane.setVvalue(centerScrollPane.getVmin());
        });
        
        VBox quickBrowseVBox = new VBox(titleLabel,centerScrollPane);
        return quickBrowseVBox;
    }
            
    private Task<SearchParameters> getSearchTask 
                (SearchParameters submittedSearchParms) {
       return new Task<SearchParameters>() { 
                CatalogCallback callback
                    = new CatalogCallback () {
                        @Override
                        public void updateTaskProgress(long workDone, long max) {
                            updateProgress(workDone,max);
                        }
                        @Override
                        public void updateTaskMessage(String message) {
                            updateMessage(message);
                        }
                    };
                @Override 
                protected SearchParameters call() throws InterruptedException {
                    try {
                        return catalog.searchForWorks
                                            (submittedSearchParms, callback);
                    } catch (InterruptedException e) {
                        if (!this.isCancelled()) {
                            throw e;
                        }
                    }
                    return null;
        }}; 
    }

    private Label getExceptionLabel (Exception e, String lookupType) {
        Label exceptionLabel  
            = new Label("Internal error in " + lookupType + " lookup: " 
                                                    + e.getMessage());
        Tooltip exceptionTooltip = new Tooltip(e.getMessage());
        exceptionTooltip.setWrapText(true); // might not work
        exceptionLabel.setTooltip(exceptionTooltip);
        return exceptionLabel;
    }

    private class AudiobookRowParms {
        public final IndexedKey indexedKeyObject;
        public final int lowerIndex;
        public final int upperIndex;
        public final int supersetSize;
        public final Catalog.ReaderWorksOption readerWorksOption; // v1.3.3
        
        public AudiobookRowParms
                (IndexedKey indexedKeyObject, int lowerIndex, 
                        int upperIndex, int supersetSize,
                        Catalog.ReaderWorksOption readerWorksOption) {
            this.indexedKeyObject = indexedKeyObject;
            this.lowerIndex = lowerIndex;
            this.upperIndex = upperIndex;
            this.supersetSize = supersetSize;
            this.readerWorksOption = readerWorksOption;
        }
        public AudiobookRowParms (IndexedKey indexedKeyObject) {
            this(indexedKeyObject, 0, 0, 0, Catalog.ReaderWorksOption.ALL_WORKS);
        }
        public AudiobookRowParms (IndexedKey indexedKeyObject,
                                    Catalog.ReaderWorksOption readerWorksOption) {
            this(indexedKeyObject, 0, 0, 0, readerWorksOption);
        }
    }
    
    private static String formatTime(Duration duration, Duration fullDuration) {
        int intFullDuration = (int)Math.floor(fullDuration.toSeconds());
        int fullDurationHours = intFullDuration / (60 * 60);

        int intDuration = (int)Math.floor(duration.toSeconds());
        int durationHours = intDuration / (60 * 60);
        if (durationHours > 0) {
           intDuration -= durationHours * 60 * 60;
        }
        int durationMinutes = intDuration / 60;
        int durationSeconds = intDuration - durationMinutes * 60;
        if (fullDurationHours > 0) {
           return String.format("%d:%02d:%02d", 
                        durationHours, durationMinutes, durationSeconds);
        } else {
            return String.format("%02d:%02d",
                        durationMinutes, durationSeconds);
        }
    }
    
    private class FinalBoolean {
        boolean value;
        public FinalBoolean () {
            value = false;
        }
        public FinalBoolean (boolean initValue) {
            value = initValue;
        }
        public void set (boolean newValue) {
            value = newValue;
        }
        public boolean get () {
            return value;
        }
        public boolean isTrue () {
            return value;
        }
        public boolean isFalse () {
            return !value;
        }
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    private class FinalInteger {
        int value;
        public FinalInteger () {
            value = 0;
        }
        public FinalInteger (int initValue) {
            value = initValue;
        }
        public void set (int newValue) {
            value = newValue;
        }
        public int get () {
            return value;
        }
        public int increment () {
            return ++value;
        }
        public int decrement () {
            return --value;
        }
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
