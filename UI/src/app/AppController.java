package app;

import commands.CommandsController;
import engine.api.Engine;
import engine.impl.EngineImpl;
import header.HeaderController;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import modelUI.impl.FocusCellImpl;
import ranges.RangesController;
import sheet.SheetController;
import sheet.cell.api.CellGetters;
import sheet.coordinate.impl.CoordinateFactory;

public class AppController {

    @FXML private BorderPane appBorderPane;
    @FXML private ScrollPane headerComponent;
    @FXML private HeaderController headerComponentController;
    @FXML private ScrollPane commandsComponent;
    @FXML private CommandsController commandsComponentController;
    @FXML private ScrollPane rangesComponent;
    @FXML private RangesController rangesComponentController;
    private ScrollPane sheetComponent;
    private SheetController sheetComponentController;

    private FocusCellImpl cellInFocus;
    private StringProperty[][] cellsValue; //should be here or in app controller ?
    private Engine engine;
    //private View view;

    @FXML
    public void initialize() {

        cellInFocus = new FocusCellImpl();
        engine = EngineImpl.create();

        if (headerComponentController != null && commandsComponentController != null && rangesComponentController != null) {
            headerComponentController.setMainController(this);
            commandsComponentController.setMainController(this);
            rangesComponentController.setMainController(this);

            headerComponentController.textFieldCellId.textProperty().bind(cellInFocus.coordinate);
            headerComponentController.textFieldOrignalValue.textProperty().bind(cellInFocus.originalValue);
            headerComponentController.labelVersionSelector.textProperty().bind(cellInFocus.lastUpdateVersion);
        }
    }

    public void uploadXml()
    {
       // headerComponentController.buttonUploadXmlFileAction(new ActionEvent()); //why i need it or how to use it ?

        engine.readXMLInitFile("C:/Users/itayr/OneDrive/Desktop/basic.xml");
        //dynamic sheet component,
        //TODO: from here put in private function
        sheetComponentController = new SheetController();
        sheetComponent = sheetComponentController.getInitializedSheet(engine.getSheetStatus().getLayout().getRows(),
                                                                      engine.getSheetStatus().getLayout().getColumns()); //only for structure of flow.
        setContentAndBindsOnGrid();
        appBorderPane.setCenter(sheetComponent);
    }

    public void setContentAndBindsOnGrid()
    {
        int rows = engine.getSheetStatus().getLayout().getRows();
        int columns = engine.getSheetStatus().getLayout().getColumns();
        // Dynamically populate the GridPane with TextFields
        //all of this should be in the appController ??
        cellsValue = new StringProperty[rows + 1][columns + 1];

        for (int row = 0; row <= rows; row++) {
            for (int col = 0; col <= columns; col++) {

                TextField textField = new TextField();
                textField.setEditable(false);  // Disable editing

                textField.setMaxWidth(Double.MAX_VALUE);  // Allow TextField to stretch horizontally
                textField.setMaxHeight(Double.MAX_VALUE);  // Allow TextField to stretch vertically

                // Set font size and alignment to match FXML
                textField.setFont(Font.font("System", 12));
                textField.setAlignment(javafx.geometry.Pos.CENTER);

                // Dynamically set the content of the TextField

                if (row == 0 && col > 0) {
                    cellsValue[row][col].setValue(Character.toString((char) ('A' + col - 1)));  // Column headers (A, B, C, etc.)
                } else if (col == 0 && row > 0) {
                    cellsValue[row][col].setValue(Integer.toString(row));  // Row headers (1, 2, 3, etc.)
                } else {
                    //getting cel
                    CellGetters cell = engine.getCellStatus(row, col);
                    final String originalValue;
                    final String coord;
                    final String lastUpdateVersion;

                    if (cell != null) //exist
                    {
                        cellsValue[row][col].setValue(cell.getEffectiveValue().getValue().toString());
                        originalValue = cell.getOriginalValue();
                        coord = cell.getCoordinate().toString();
                        lastUpdateVersion = String.valueOf(cell.getVersion());

                    } else { //empty cell
                        cellsValue[row][col].setValue("");
                        originalValue = "";
                        coord = CoordinateFactory.createCoordinate(row,col).toString();
                        lastUpdateVersion = "";

                    }
                    //add listener to focus.
                    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                        focusChanged(newValue,coord,originalValue,lastUpdateVersion);
                    });
                }

                textField.textProperty().bind(cellsValue[row][col]); //cellsValue[row][col] = stringProperty.
                // Add TextField to cells

                // Add TextField to the GridPane
                sheetComponentController.gridPane.add(textField, col, row); //gridPane is public just for flow.

                // Set alignment for grid children if necessary
                GridPane.setHgrow(textField, Priority.ALWAYS);
                GridPane.setVgrow(textField, Priority.ALWAYS);
                GridPane.setHalignment(textField, HPos.CENTER);
                GridPane.setValignment(textField, VPos.CENTER);
            }
        }
    }

    public void focusChanged(boolean newValue,String coord,String originalValue,String lastUpdateVersion)
    {
        if(newValue)
        {
            //change text box cell id
            cellInFocus.setCoordinate(coord);
            //change orignal value
            cellInFocus.setOriginalValue(originalValue);
            //change version
            cellInFocus.lastUpdateVersion.set(lastUpdateVersion);
        }
    }

}
