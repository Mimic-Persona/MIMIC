package com.codecool.dungeoncrawl.gui;

import com.codecool.dungeoncrawl.APIs.AgentAPI;

import com.codecool.dungeoncrawl.APIs.status.Environment;
import com.codecool.dungeoncrawl.APIs.status.Status;
import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.agent.GameServer;
import com.codecool.dungeoncrawl.logic.actors.Actor;
import com.codecool.dungeoncrawl.logic.actors.Player;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.GameMap;
import com.codecool.dungeoncrawl.logic.map.MapLoader;
import com.codecool.dungeoncrawl.logic.map.OutOfMapCell;
import com.codecool.dungeoncrawl.agent.monkey.MonkeyAPI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codecool.dungeoncrawl.APIs.status.Environment.newLevelRefresh;
import static com.codecool.dungeoncrawl.APIs.status.Environment.updateVisitedMap;
import static com.codecool.dungeoncrawl.agent.monkey.MonkeyAPI.MAX_ACTIONS;
import static com.codecool.dungeoncrawl.agent.reporter.JacocoReporter.*;
import static com.codecool.dungeoncrawl.agent.reporter.ReporterClient.IS_IN_EXP;
import static com.codecool.dungeoncrawl.agent.utils.JsonUtils.jsonFileToDictionary;

public class Main extends Application {
    // TODO: Define the constants before running the experiment
    public static JSONObject config = jsonFileToDictionary("./MIMIC_Dungeon_Adventures/config.json");
    public static final int PORT;

    static {
        assert config != null;
        try {
            PORT = config.getInt("PORT");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static final long DURATION = Long.MAX_VALUE; // Define the duration for running the monkey (in milliseconds)
    public static boolean BY_ACTION = true;

    public static GameServer gameServer;
    public static boolean isAgentNext = true;
    public static boolean isInMonkeyMode = false;
    public static long startTime = -1;
    public static boolean toReport = false;

    public static int actionCounter = -1;
    public static String prevAction = "";
    public static String prevEnv = "";

    MapLoader mapFromFileLoader = new MapLoader();
    List<GameMap> maps = new ArrayList<>();
    List<String> nameMaps = Arrays.asList("/map.txt","/map2.txt","/map3.txt","/win.txt");

    public static int level;
    public static GameMap map;

    int FONT_SIZE = 16;
    String FONT_COLOR = "white";
    String BOLD_FONT = "-fx-font-weight: bold";

    static final int WIDTH = 21;
    static final int HEIGHT = 21;

    Canvas canvas = new Canvas(
            WIDTH * Tiles.TILE_WIDTH,
            HEIGHT * Tiles.TILE_WIDTH);
    GraphicsContext context = canvas.getGraphicsContext2D();
    GraphicsContext context2 = canvas.getGraphicsContext2D();
    Label healthLabelText = new Label("Health: ");
    Label healthLabel = new Label();
    Label attackLabelText = new Label("Attack: ");
    Label attackLabel = new Label();
    Label firstSeparatorLabel = new Label();
    Label secondSeparatorLabel = new Label();
    private Button pickUpButton = new Button("Pick up item");
    private Button endButton = new Button("Back to manu");
    private GridPane mainLootGrid = new GridPane();
    Stage stage;


    public Main() {
        maps.add(mapFromFileLoader.loadMap(this,nameMaps.get(level)));
        this.map = maps.get(level);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        mainMenu(primaryStage);
    }

    public void preGameSettings(Stage primaryStage) throws FileNotFoundException {
        ImageView selectedImage = new ImageView();
        Image image1 = new Image(Main.class.getResourceAsStream("/logo.png"));
        selectedImage.setImage(image1);
        HBox gameLogo = new HBox(selectedImage);
        Button startButton = new Button("Start the Game");
        Button backButton = new Button("Back to Menu");
        startButton.setId("allbtn");
        backButton.setId("allbtn");
        HBox buttons = new HBox(backButton, startButton);
        Text championNameLabel = new Text("Enter Your Name");
        championNameLabel.setId("text");
        TextField textField = new TextField();
        textField.setId("input");
        VBox settingsLayout = new VBox(championNameLabel, textField, buttons);
        settingsLayout.setAlignment(Pos.CENTER);
        startButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            try {
                map.getPlayer().setName(textField.getText());
                gameStart(primaryStage);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        backButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            try {
                mainMenu(primaryStage);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });
        gameLogo.setAlignment(Pos.CENTER);
        HBox.setMargin(selectedImage, new Insets(50, 0, 0, 0));
        settingsLayout.setSpacing(25);
        BorderPane menuLayout = new BorderPane();
        menuLayout.setBackground(new Background(new BackgroundFill(Color.rgb(71, 45, 60), CornerRadii.EMPTY, Insets.EMPTY)));
        menuLayout.setPrefWidth(1000);
        menuLayout.setPrefHeight(672);
        menuLayout.setTop(gameLogo);
        menuLayout.setCenter(settingsLayout);
        HBox.setMargin(backButton, new Insets(10, 10, 10, 10));
        buttons.setAlignment(Pos.CENTER);
        Scene scene = new Scene(menuLayout);
        scene.getStylesheets().add("style.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dungeon Adventures");
        primaryStage.show();
    }

    public void mainMenu(Stage primaryStage) throws FileNotFoundException, RuntimeException {
        ImageView selectedImage = new ImageView();
        Image image1 = new Image(Main.class.getResourceAsStream("/logo.png"));
        selectedImage.setImage(image1);
        HBox gameLogo = new HBox(selectedImage);
        Button startGameButton = new Button("New Adventure");
        Button exitGameButton = new Button("Exit Game");
        startGameButton.setId("allbtn");
        exitGameButton.setId("allbtn");
        startGameButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            try {
                preGameSettings(primaryStage);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });
        exitGameButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            System.exit(0);
        });

        VBox buttons = new VBox(startGameButton,exitGameButton);
        gameLogo.setAlignment(Pos.CENTER);
        HBox.setMargin(selectedImage, new Insets(50, 0, 0, 0));
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        BorderPane menuLayout = new BorderPane();
        menuLayout.setCenter(buttons);
        menuLayout.setTop(gameLogo);
        menuLayout.setBackground(new Background(new BackgroundFill(Color.rgb(71, 45, 60), CornerRadii.EMPTY, Insets.EMPTY)));
        menuLayout.setPrefWidth(1000);
        menuLayout.setPrefHeight(672);
        Scene scene = new Scene(menuLayout);
        scene.getStylesheets().add("style.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("Dungeon Adventures");
        primaryStage.show();
    }

    public Stage EndGame(Stage primaryStage,String stage) throws FileNotFoundException, RuntimeException {
        ImageView selectedImage = new ImageView();
        Image image1 = new Image(Main.class.getResourceAsStream(stage));
        selectedImage.setImage(image1);
        HBox gameLogo = new HBox(selectedImage);
        Button restartGameButton = new Button("Restart");
        restartGameButton.setId("allbtn");
        restartGameButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            try {
                maps = new ArrayList<>();
                level = 0;
                maps.add(mapFromFileLoader.loadMap(this,nameMaps.get(level)));
                map = maps.get(level);
                mainMenu(primaryStage);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });
        VBox buttons = new VBox(restartGameButton);
        gameLogo.setAlignment(Pos.CENTER);
        HBox.setMargin(selectedImage, new Insets(50, 0, 0, 0));
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        BorderPane menuLayout = new BorderPane();
        menuLayout.setCenter(buttons);
        menuLayout.setTop(gameLogo);
        menuLayout.setBackground(new Background(new BackgroundFill(Color.rgb(71, 45, 60), CornerRadii.EMPTY, Insets.EMPTY)));
        menuLayout.setPrefWidth(1000);
        menuLayout.setPrefHeight(672);
        Scene scene = new Scene(menuLayout);
        scene.getStylesheets().add("style.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("Frost Dungeon Crawl");
        primaryStage.show();
        return primaryStage;
    }

    public void gameStart(Stage primaryStage) throws Exception{

        GridPane ui = new GridPane();
        ui.setPrefWidth(200);
        ui.setPadding(new Insets(10));
        ui.setBackground(new Background(new BackgroundFill(Color.rgb(0, 59, 59), CornerRadii.EMPTY, Insets.EMPTY)));


        ui.add(healthLabelText, 0, 0);
        healthLabelText.setTextFill(Color.web(FONT_COLOR));
        healthLabelText.setFont(new Font(FONT_SIZE));
        healthLabelText.setStyle(BOLD_FONT);
        ui.add(healthLabel, 1, 0);
        healthLabel.setTextFill(Color.web(FONT_COLOR));
        healthLabel.setFont(new Font(FONT_SIZE));
        healthLabel.setStyle(BOLD_FONT);
        ui.add(attackLabelText, 0, 1);
        attackLabelText.setTextFill(Color.web(FONT_COLOR));
        attackLabelText.setFont(new Font(FONT_SIZE));
        attackLabelText.setStyle(BOLD_FONT);
        ui.add(attackLabel, 1, 1);
        attackLabel.setTextFill(Color.web(FONT_COLOR));
        attackLabel.setFont(new Font(FONT_SIZE));
        attackLabel.setStyle(BOLD_FONT);
        ui.add(firstSeparatorLabel, 0, 4);
        ui.add(pickUpButton, 0, 7);
        ui.add(secondSeparatorLabel, 0, 11);
        lootLayout();
        ui.add(mainLootGrid, 0, 14, 3, 1);


        pickUpButton.setOnAction(actionEvent ->  {
            map.getPlayer().pickUpItem();
            refresh();
        });


        canvas = new Canvas(
                WIDTH * Tiles.TILE_WIDTH,
                HEIGHT * Tiles.TILE_WIDTH);
        context = canvas.getGraphicsContext2D();
        context2 = canvas.getGraphicsContext2D();

        maps = new ArrayList<>();
        level = 0;
        maps.add(mapFromFileLoader.loadMap(this,nameMaps.get(level)));
        map = maps.get(level);

        newLevelRefresh();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(canvas);
        borderPane.setRight(ui);

        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        refresh();
        scene.setOnKeyPressed(this::onKeyPressed);
        primaryStage.setTitle("Dungeon Adventures");
        primaryStage.show();


        if (gameServer == null) {
            GLog.c("MIMIC Mode Enabled!");
            gameServer = new GameServer(PORT);
            gameServer.setConnectionLostTimeout(0);
            gameServer.start();
            startTime = System.currentTimeMillis();
        }
    }

    private void lootLayout() {
        mainLootGrid.setPrefSize(5 * Tiles.TILE_WIDTH, 200);
        mainLootGrid.setPadding(new Insets(5));
        mainLootGrid.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        mainLootGrid.setBackground(new Background(new BackgroundFill(Color.valueOf("#472D3C"), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case UP -> map.getPlayer().move(0, -1);
            case DOWN -> map.getPlayer().move(0, 1);
            case LEFT -> map.getPlayer().move(-1, 0);
            case RIGHT -> map.getPlayer().move(1,0);
            case M -> {
                try {
                    AgentAPI.handle("m");
                } catch (JSONException e) {
                    GLog.e("Error in handle m: " + e.getMessage());
                }
            }
            case S -> {
                try {
                    AgentAPI.handle("s");
                } catch (JSONException e) {
                    GLog.e("Error in handle s: " + e.getMessage());
                }
            }
            case B -> {
                try {
                    GLog.c("MIMIC Mode Started with " + DURATION + " milliseconds!");
                    startTime = System.currentTimeMillis();
                    AgentAPI.handle("b");
                } catch (JSONException e) {
                    GLog.e("Error in handle b: " + e.getMessage());
                }
            }
            case L -> {
                try {
                    GLog.c("MIMIC-P Mode Started with " + DURATION + " milliseconds!");
                    startTime = System.currentTimeMillis();
                    AgentAPI.handle("l");
                } catch (JSONException e) {
                    GLog.e("Error in handle l: " + e.getMessage());
                }
            }
            case K -> {
                try {
                    GLog.c("MIMIC-P+S Mode Started with " + DURATION + " milliseconds!");
                    startTime = System.currentTimeMillis();
                    AgentAPI.handle("k");
                } catch (JSONException e) {
                    GLog.e("Error in handle k: " + e.getMessage());
                }
            }
            case R -> {
                Main.isInMonkeyMode = true;
                GLog.c("Monkey Mode Started with " + DURATION + " milliseconds!");
                startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < DURATION && map.getPlayer().getHealth() > 0 && Main.isInMonkeyMode && actionCounter < MAX_ACTIONS) {
                    MonkeyAPI.handle();
                    refresh();
                }
                Main.isInMonkeyMode = false;
                GLog.h("Monkey Mode Ended!");
            }

            case O -> {
                try {
                    gameStart(stage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            case DIGIT1 -> {
                try {
                    GLog.h(Status.getStatus().toString());
                } catch (JSONException e) {
                    GLog.e("Error in handle DIGIT1: " + e.getMessage());
                }
            }
            case DIGIT2 -> GLog.h(Environment.getMapStr());
            case DIGIT3 -> GLog.h(Environment.getMapVisibleMapStr());
            case DIGIT4 -> GLog.h(Environment.getMapVisibleMapWithMemoryStr());
            case DIGIT5 -> GLog.h(Environment.getImportantCellsJSON().toString());
        }
        refresh();
    }

    private void showAndHidePickUpButton() {
        if (map.getPlayer().getCell().isItemOnCell()) {
            showPickButton();
            pickUpButton.setOnAction(actionEvent ->  {
                map.getPlayer().pickUpItem();
                refresh();
                    }
            );
        } else {
            hideButton();
        }
    }

    public void refresh() {
        if (IS_IN_EXP && startTime != -1 && (System.currentTimeMillis() - startTime >= DURATION || actionCounter >= MAX_ACTIONS)) {
            try {
                stage = EndGame(stage,"/gameover.png");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        map.getMobs().forEach(Actor::move);
        showAndHidePickUpButton();
        map.removeDeadMobs();
        checkPlayerIsDead();
        pickUpButton.setFocusTraversable(false);
        reLoadCanvas();
        drawLoot();
        updateVisitedMap();
        isAgentNext = true;

        if (Main.BY_ACTION && Main.toReport && !IS_IN_EXP) {
            JSONObject report = new JSONObject();

            try {
                report.put("msgType", "report");
                report.put("actionCounter", Main.actionCounter);
                report.put("action", Main.prevAction);
                report.put("env", Main.prevEnv);

                Main.prevAction = "";
                Main.prevEnv = "";

                Main.gameServer.broadcast(report.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Main.toReport = false;

        } else if (Main.BY_ACTION && Main.toReport && IS_IN_EXP) {
            try {
                dumpData(Main.actionCounter, Main.prevAction, Main.prevEnv);

                Main.prevAction = "";
                Main.prevEnv = "";

            } catch (IOException  | JSONException e) {
                e.printStackTrace();
            }

            Main.toReport = false;
        }
    }

    private void reLoadCanvas() {
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                drawTile(x, y);
            }
        }
        healthLabel.setText("" + map.getPlayer().getHealth());
        attackLabel.setText("" + map.getPlayer().getDamage());
    }

    private void drawTile(int x, int y) {
        int mapX = x + map.getPlayer().getX() - WIDTH / 2;
        int mapY = y + map.getPlayer().getY() - HEIGHT / 2;
        if (0 <= mapX && mapX < map.getWidth() && 0 <= mapY && mapY < map.getHeight()){
            Cell cell = map.getCell(mapX, mapY);
            if (cell.getActor() != null) Tiles.drawTile(context, cell.getActor(), x, y);
            else if (cell.getItem() != null) Tiles.drawTile(context, cell.getItem(), x, y);
            else Tiles.drawTile(context, cell, x, y);
        } else Tiles.drawTile(context, new OutOfMapCell(), x, y);
    }

    private void checkPlayerIsDead() {
        if (map.getPlayer().getHealth() <= 0) {
            try {

                if (!IS_IN_EXP || (IS_IN_EXP && System.currentTimeMillis() - startTime >= DURATION))
                    stage = EndGame(stage,"/gameover.png");
                else {
                    try {
                        gameStart(stage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!IS_IN_EXP) {
                    gameServer.stop();
                }

            } catch (FileNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void win() {
        try {
            if (!IS_IN_EXP || (IS_IN_EXP && System.currentTimeMillis() - startTime >= DURATION))
                stage = EndGame(stage,"/win.png");
            else {
                    Stage stage = this.stage;

                    Platform.runLater(() -> {
                        try {
                            gameStart(stage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            }

            if (!IS_IN_EXP) {
                gameServer.stop();
            }

        } catch (FileNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawLoot() {
        int counter = 0;
        int row = 0;
        int column = 0;
        int TILES_IN_ROW = 4;
        mainLootGrid.getChildren().clear(); //clear the grid
        for (int i = 0; i < map.getPlayer().getInventory().size(); i++) {
            this.canvas = new Canvas(Tiles.TILE_WIDTH, Tiles.TILE_WIDTH);
            this.context2 = canvas.getGraphicsContext2D();

            Tiles.drawTile(context2, map.getPlayer().getInventory().get(i), 0, 0);
            mainLootGrid.add(canvas, column, row);
            counter += 1;
            row = counter / TILES_IN_ROW;
            column = counter % TILES_IN_ROW;
        }
    }

    private void showPickButton() {
        pickUpButton.setVisible(true);
    }

    private void hideButton() {
        pickUpButton.setVisible(false);
    }

    public void addMap(GameMap map) {
        maps.add(map);
    }

    public void nextLevel(){
        this.level ++;
        if(level >=maps.size()){
            GameMap newmap = mapFromFileLoader.loadMap(this, nameMaps.get(level));
            addMap(newmap);
        }
        this.map =maps.get(level);
        Player player = maps.get(level-1).getPlayer();
        map.getPlayer().setAttributes(player.getInventory(), player.getHealth(), player.getDamage(), player.getName());
        newLevelRefresh();
    }

    public void previousLevel(){
        this.level --;
        this.map =maps.get(level);
        Player player = maps.get(level+1).getPlayer();
        map.getPlayer().setAttributes(player.getInventory(), player.getHealth(), player.getDamage(), player.getName());
        newLevelRefresh();
    }
}
