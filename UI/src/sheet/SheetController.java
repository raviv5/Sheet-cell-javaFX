package sheet;

import app.AppController;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import modelUI.api.EffectiveValuesPoolPropertyReadOnly;
import modelUI.impl.TextFieldDesign;
import modelUI.impl.VersionDesignManager;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import sheet.layout.api.LayoutGetters;
import sheet.range.api.Range;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;


public class SheetController {

    private AppController mainController;

    private ScrollPane scrollPane;
    private GridPane gridPane;
    private final Map<Coordinate, TextField> cellsTextFieldMap = new HashMap<>();
    private final Map<Coordinate, Background> previousBackgrounds = new HashMap<>();
    private int defaultRowHeight;
    private int defaultColWidth;

    public SheetController() {
        scrollPane = new ScrollPane();
        gridPane = new GridPane();
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public ScrollPane getInitializedSheet(LayoutGetters layout) {
        this.defaultRowHeight = layout.getSize().getHeight();
        this.defaultColWidth = layout.getSize().getWidth();
        setLayoutGridPane(layout);
        //until here set the grid.
        setBindsTo();
        setScrollPane();
        return scrollPane;
    }

    private void setLayoutGridPane(LayoutGetters layout) {

        // Create a GridPane
        gridPane.setGridLinesVisible(true);  // Enable grid lines for visualization

        // Add column constraints to match FXML configuration
        for (int col = 0; col <= layout.getColumns(); col++) {

            ColumnConstraints columnConstraints = new ColumnConstraints();
            if (col == 0) {
                columnConstraints.setMinWidth(30);
                columnConstraints.setMaxWidth(30);
                columnConstraints.setPrefWidth(30);  // First column for row numbers
                columnConstraints.setHgrow(Priority.NEVER);  // No horizontal grow
            } else {
                columnConstraints.setMinWidth(layout.getSize().getWidth());
                columnConstraints.setPrefWidth(layout.getSize().getWidth());  // Other columns
                columnConstraints.setHgrow(Priority.NEVER);  // Allow horizontal grow
            }
            gridPane.getColumnConstraints().add(columnConstraints);
        }

        // Add row constraints to match FXML configuration
        for (int row = 0; row <= layout.getRows(); row++) {
            RowConstraints rowConstraints = new RowConstraints();
            if (row == 0) {
                rowConstraints.setMinHeight(30);
                rowConstraints.setMaxHeight(30);
                rowConstraints.setPrefHeight(30);
                rowConstraints.setVgrow(Priority.NEVER);  // No horizontal grow
            } else {
                rowConstraints.setMinHeight(layout.getSize().getHeight());
                rowConstraints.setPrefHeight(layout.getSize().getHeight());  // Set preferred height for rows
                rowConstraints.setVgrow(Priority.NEVER);  // Allow vertical grow
            }
            gridPane.getRowConstraints().add(rowConstraints);
        }
    }

    private void setBindsTo() {

        EffectiveValuesPoolPropertyReadOnly dataToView = mainController.getEffectiveValuesPool();
        for (int row = 0; row < gridPane.getRowCount(); row++) {
            for (int col = 0; col < gridPane.getColumnCount(); col++) {

                TextField textField = new TextField();
                textField.setEditable(false);  // Disable editing

                textField.setMaxWidth(Double.MAX_VALUE);  // Allow TextField to stretch horizontally
                textField.setMaxHeight(Double.MAX_VALUE);  // Allow TextField to stretch vertically
                textField.setBorder(Border.stroke(Paint.valueOf("gray")));
                textField.setBackground(Background.fill(Paint.valueOf("white")));
                textField.setStyle("-fx-text-fill: black; -fx-background-color: white");

                // Set font size and alignment to match FXML
                textField.setFont(Font.font("System", 12));
                textField.setAlignment(Pos.CENTER);
                // Dynamically set the content of the TextField

                if (row == 0 && col > 0) {
                    textField.setText(Character.toString((char) ('A' + col - 1)));  // Column headers (A, B, C, etc.)
                } else if (col == 0 && row > 0) {
                    textField.setText(Integer.toString(row));  // Row headers (1, 2, 3, etc.)
                } else {
                    if (row != 0 || col != 0) {
                        Coordinate coordinate = CoordinateFactory.createCoordinate(row-1,col-1);
                        cellsTextFieldMap.put(coordinate, textField);
                        textField.textProperty().bind(dataToView.getEffectiveValuePropertyAt(coordinate));
                        //add listener to focus, need to change.
                        int finalCol = col;
                        int finalRow = row;

                        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue) {
                                textField.setBorder(Border.stroke(Paint.valueOf("green")));
                            } else {
                                textField.setBorder(Border.stroke(Paint.valueOf("gray")));
                            }
                            mainController.focusChanged(newValue, coordinate);
                            mainController.changeCommandsColumnWidth(gridPane.getColumnConstraints().get(finalCol).getPrefWidth());
                            mainController.changeCommandsRowHeight(gridPane.getRowConstraints().get(finalRow).getPrefHeight());
                            mainController.changeCommandsColumnAlignment(textField.getAlignment());
                            mainController.changeCommandsCellBackgroundColor(getTextFieldBackgroundColor(textField));
                            mainController.changeCommandsCellTextColor(getTextFieldTextColor(textField));
                        });
                    }
                }

                gridPane.add(textField, col, row);

                GridPane.setHgrow(textField, Priority.NEVER);
                GridPane.setVgrow(textField, Priority.NEVER);
                GridPane.setHalignment(textField, HPos.CENTER);
                GridPane.setValignment(textField, VPos.CENTER);
            }
        }
    }

    private void setScrollPane() {
        scrollPane.setFitToWidth(true);  // Ensure it stretches to fit the width
        scrollPane.setFitToHeight(true);  // Ensure it stretches to fit the height
        scrollPane.setContent(gridPane);
    }

    public void changeColorDependedCoordinate(ListChangeListener.Change<? extends Coordinate> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (Coordinate coordinate : change.getAddedSubList()) {
                    Background currentBackground = Objects.requireNonNull(cellsTextFieldMap.get(coordinate)).getBackground();
                    previousBackgrounds.put(coordinate, currentBackground);
                    Objects.requireNonNull(cellsTextFieldMap.get(coordinate)).setBackground(Background.fill(Paint.valueOf("lightblue")));
                }
            }

            if (change.wasRemoved()) {
                for (Coordinate coordinate : change.getRemoved()) {
                    TextField textField = Objects.requireNonNull(cellsTextFieldMap.get(coordinate));
                    Background previousBackground = previousBackgrounds.remove(coordinate);
                    textField.setBackground(Objects.requireNonNullElseGet(previousBackground, () -> new Background(new BackgroundFill(Paint.valueOf("white"), CornerRadii.EMPTY, null))));
                }
            }
        }
    }

    public void changeColorInfluenceCoordinate(ListChangeListener.Change<? extends Coordinate> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                // Do something with the added items
                for (Coordinate coordinate : change.getAddedSubList()) {
                    Background currentBackground = Objects.requireNonNull(cellsTextFieldMap.get(coordinate)).getBackground();
                    previousBackgrounds.put(coordinate, currentBackground);
                    Objects.requireNonNull(cellsTextFieldMap.get(coordinate)).setBackground(Background.fill(Paint.valueOf("lightgreen")));
                }
            }

            if (change.wasRemoved()) {
                for (Coordinate coordinate : change.getRemoved()) {
                    TextField textField = Objects.requireNonNull(cellsTextFieldMap.get(coordinate));
                    Background previousBackground = previousBackgrounds.remove(coordinate);
                    textField.setBackground(Objects.requireNonNullElseGet(previousBackground, () -> new Background(new BackgroundFill(Paint.valueOf("white"), CornerRadii.EMPTY, null))));
                }
            }
        }
    }

//    private TextField targetTextField(Coordinate coordinate) {
//        for (Node node : gridPane.getChildren()) {
//            if(node instanceof TextField &&
//                    GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == coordinate.getRow() + 1 &&
//                    GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == coordinate.getCol() + 1)
//            {
//                return (TextField) node;  // Cast to TextField and return it
//            }
//        }
//        return null;
//    }

    public void changeColumnWidth(int column, int prefWidth) {
        gridPane.getColumnConstraints().get(column).setPrefWidth(prefWidth);
        gridPane.getColumnConstraints().get(column).setMinWidth(prefWidth);
        gridPane.getColumnConstraints().get(column).setMaxWidth(prefWidth);
    }

    public void changeRowHeight(int row, int prefHeight) {
        gridPane.getRowConstraints().get(row).setPrefHeight(prefHeight);
        gridPane.getRowConstraints().get(row).setMinHeight(prefHeight);
        gridPane.getRowConstraints().get(row).setMaxHeight(prefHeight);
    }

    public void changeColumnAlignment(int column, Pos pos) {
        gridPane.getChildren().forEach(node -> {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (node instanceof TextField && colIndex == column && rowIndex != 0) {
                ((TextField) node).setAlignment(pos);
            }
        });
    }

    public Color getTextFieldBackgroundColor(TextField textField) {
        Background background = textField.getBackground();
        if (background != null && !background.getFills().isEmpty()) {
            for (BackgroundFill fill : background.getFills().reversed()) {
                if (fill.getFill() instanceof Color) {
                    return (Color) fill.getFill();
                }
            }
        }
        return null; // No background color found
    }

    private Color getTextFieldTextColor(TextField textField) {
        Text text = (Text) textField.lookup(".text");
        if (text != null) {
            return (Color) text.getFill();
        }
        return Color.BLACK; // Default color if not set
    }

    public void changeCellBackgroundColor(Color color) {
        Objects.requireNonNull(cellsTextFieldMap.get(CoordinateFactory.toCoordinate(mainController.getCellInFocus().getCoordinate().get()))).setStyle("-fx-background-color: " + toHexString(color) + ";");
    }

    public void changeCellTextColor(Color color) {
        if (color != null)
            Objects.requireNonNull(cellsTextFieldMap.get(CoordinateFactory.toCoordinate(mainController.getCellInFocus().getCoordinate().get()))).setStyle("-fx-text-fill: " + toHexString(color) + ";");
    }

    private String toHexString(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public void resetCellsToDefault(Coordinate focusrdCellCoordinate) {
        TextField textField = cellsTextFieldMap.get(focusrdCellCoordinate);

        if (textField != null) {
            textField.setStyle("-fx-text-fill: black;");
            textField.setBackground(Background.fill(Paint.valueOf("white")));
        }
    }

    public void paintRangeOnSheet(RangeGetters range, Color color) {
        Boundaries boundaries = range.getBoundaries();
        Coordinate to = boundaries.getTo();
        Coordinate from = boundaries.getFrom();

        for (int i = from.getRow(); i <= to.getRow(); i++) {
            for (int j = from.getCol(); j <= to.getCol(); j++) {
                TextField textField = cellsTextFieldMap.get(CoordinateFactory.createCoordinate(i, j));
                if (textField != null) {
                    BackgroundFill backgroundFill = new BackgroundFill(color, CornerRadii.EMPTY, null);
                    Background background = new Background(backgroundFill);
                    textField.setBackground(background);
                }
            }
        }
    }
///Todo: the method that need to be deleted.
    public void resetSheetToDefault() {
        restRowsHeight();
        resetColWidth();
    }

    private void restRowsHeight() {
        for(int i = 1 ; i < gridPane.getRowCount(); i++){
            changeRowHeight(i,defaultRowHeight);
        }
    }

    private void resetColWidth() {
        for(int i = 1 ; i < gridPane.getColumnCount(); i++){
            changeColumnWidth(i,defaultColWidth);
        }
    }

    public GridPane getGridPane() {
     return gridPane;
    }


    public void setGridPaneDesign(VersionDesignManager.VersionDesign versionDesign) {
        setNodeDesign(versionDesign.getCellDesignsVersion());
        setColumnsDesign(versionDesign.getColumnsLayoutVersion());
        setRowsDesign(versionDesign.getRowsLayoutVersion());
    }

    private void setRowsDesign(Map<Integer, Integer> rowsLayoutVersion) {
        rowsLayoutVersion.forEach((index,rowHeight)->{
            RowConstraints rowConstraints = gridPane.getRowConstraints().get(index);
            rowConstraints.setPrefHeight(rowHeight);
            rowConstraints.setMinHeight(rowHeight);
            rowConstraints.setMaxHeight(rowHeight);
        });
    }

    private void setColumnsDesign(Map<Integer, Integer> columnsLayoutVersion) {
        columnsLayoutVersion.forEach((index,columnWidth)->{
            ColumnConstraints columnConstraints = gridPane.getColumnConstraints().get(index);
            columnConstraints.setPrefWidth(columnWidth);
            columnConstraints.setMinWidth(columnWidth);
            columnConstraints.setMaxWidth(columnWidth);
        });
    }

    private void setNodeDesign(Map<Integer, TextFieldDesign> cellDesignsVersion) {
        cellDesignsVersion.forEach((index, textFieldDesign) -> {
            gridPane.getChildren().get(index).setStyle(textFieldDesign.getStyle());
        });
    }
}



