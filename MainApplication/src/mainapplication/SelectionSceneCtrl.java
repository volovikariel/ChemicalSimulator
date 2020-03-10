/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class SelectionSceneCtrl implements Initializable, SubSceneController {
    
    @FXML
    TextField txtManual;
    @FXML
    SplitPane splitPane;
    @FXML
    GridPane gridPane;
    @FXML
    AnchorPane paneSimulation;
    
    Atom[] atoms;
    
    static DataFormat data = new DataFormat("element");
    
    static ArrayList<VBox> vboxes = new ArrayList<>();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paneSimulation.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for (Node child : paneSimulation.getChildren()) {
                    if (child instanceof VBox) {
                        VBox box = (VBox) child;
                        box.setPrefWidth(paneSimulation.getWidth() / 18.0);
                        box.setLayoutX(newValue.doubleValue() * box.getLayoutX() / oldValue.doubleValue());
                    }
                }
             }
        });
        
        paneSimulation.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for (Node child : paneSimulation.getChildren()) {
                    if (child instanceof VBox) {
                        VBox box = (VBox) child;
                        box.setPrefHeight(paneSimulation.getHeight() / 9.0);
                        box.setLayoutY(newValue.doubleValue() * box.getLayoutY() / oldValue.doubleValue());
                    }
                }
             }
        });
        
        splitPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(txtManual.getText().isEmpty()) {
                    txtManual.getParent().requestFocus();
                    txtManual.setText("");
                    txtManual.setVisible(false);
                }
            }
        });
        paneSimulation.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if(event.getDragboard().hasContent(data)) {
                    event.acceptTransferModes(event.getTransferMode());
                }
                event.consume();
            }
        });
        paneSimulation.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                
                VBox newVBox = new VBox();
                vboxes.add(newVBox);
                TableElement tempEl = (TableElement) event.getDragboard().getContent(data);
                
                appendInput(tempEl.getElementName());
                
                
                //System.out.println(tempEl.getElementName() + tempEl.getElementNumber());
                newVBox.getChildren().add(new Label(tempEl.getElementNumber()));
                newVBox.getChildren().add(new Label(tempEl.getElementName()));
                
                newVBox.setBackground(new Background(new BackgroundFill(Color.web(tempEl.getColor()), null, null)));
                
                newVBox.setLayoutX(event.getX() - newVBox.getWidth()/2);
                newVBox.setLayoutY(event.getY() - newVBox.getHeight()/2);
                
                newVBox.setPrefWidth(paneSimulation.getWidth() / 18.0);
                newVBox.setPrefHeight(paneSimulation.getHeight() / 9.0);
                
                newVBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.SECONDARY) { 
                            paneSimulation.getChildren().remove(event.getSource());
                            removeString(txtManual.getText(), ((Label)((VBox) event.getSource()).getChildren().get(1)).getText());
                        }
                    }
                });
                
                newVBox.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    VBox tempBox = (VBox) event.getSource();
                    Dragboard db = tempBox.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    
                    content.put(data, new TableElement(tempBox.getChildren(), tempBox.getBackground()));
                    db.setContent(content);
                    paneSimulation.getChildren().remove(event.getSource());
                    removeString(txtManual.getText(), ((Label)((VBox) event.getSource()).getChildren().get(1)).getText());
                    event.consume();
                    }
                });
                
                paneSimulation.getChildren().add(newVBox);
                
                event.consume();
            }
        });
    }

    public void appendInput(String text) {
        txtManual.setVisible(true);
        txtManual.setText(txtManual.getText() + text);
    }
    
    public void removeChar() {
        if(txtManual.getLength() >= 1) {   
            txtManual.setText(txtManual.getText().substring(0, txtManual.getLength() - 1));
            // Update all the VBox's
            for(Node el : paneSimulation.getChildren()) {
                if(el instanceof VBox) {
                    String symbolName = ((Label)(((VBox)el).getChildren().get(1))).getText();
                    if(txtManual.getText().contains(symbolName.substring(0, symbolName.length() - 1)) && !txtManual.getText().contains(symbolName)) {
                        txtManual.setText(txtManual.getText().replace(symbolName.substring(0, symbolName.length() - 1), ""));
                        paneSimulation.getChildren().remove(el);
                        break;
                    }
                }
            }
        }
    }
    
    public void loadTable(Atom[] atoms) {
        RowConstraints tempRow = new RowConstraints();
        tempRow.setVgrow(Priority.ALWAYS);
        ColumnConstraints tempCol = new ColumnConstraints();
        tempCol.setHgrow(Priority.ALWAYS);
        
        int rowCount = 0;
        int startOfCol = 0;
        for (int i = 0; i < 18; i++) {
            gridPane.getColumnConstraints().add(i, tempCol);
        }
        for (int i = 0; i < 9; i++) {
            gridPane.getRowConstraints().add(i, tempRow);
        }
        
        VBox tempPane;
        Paint tempColor = Color.AQUA;
        for (int i = 0; i < atoms.length; i++) {
            switch (i) {
                case 2:
                case 10:
                case 18:
                case 36:
                case 54:
                case 86:
                    rowCount++;
                    startOfCol = i;
                    break;
            }
            int colIndx = 0;
            switch(rowCount) {
                case 0:
                    colIndx = i * 17;
                    break;
                case 1:
                case 2:
                    if (i < startOfCol + 2)
                        colIndx = i - startOfCol;
                    else
                        colIndx = i - startOfCol + 10;
                    break;
                case 6:
                case 5:
                    if (i < startOfCol + 17)
                        colIndx = i - startOfCol;
                    else
                        colIndx = i - startOfCol - 14;
                    break;
                default:
                    colIndx = i - startOfCol;
            }
            int rowIndx = rowCount;
            
            if ((i > 56 && i < 71) || (i > 88 && i < 103)) {
                rowIndx += 2;
            }
            
            switch (colIndx) {
                case 0:
                    tempColor = Color.rgb(255, 204, 201);
                    break;
                case 1:
                    tempColor = Color.rgb(209, 211, 255);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                    tempColor = Color.rgb(192, 220, 255);
                    break;
                case 17:
                    tempColor = Color.rgb(255, 233, 206);
                    break;
                default:
                    if (rowIndx < 5) {
                        tempColor = colIndx - rowIndx >= 12 ? Color.rgb(255, 255, 199) : Color.rgb(203, 255, 197);
                    }
                    else {
                        tempColor = colIndx > 15 ? Color.rgb(255, 255, 199) : Color.rgb(203, 255, 197);
                    }
                    
                    switch (i) {
                        case 4:
                        case 13:
                        case 31:
                        case 32:
                        case 50:
                        case 51:
                        case 83:
                            tempColor = Color.rgb(224, 240, 195);
                            break;
                    }
            }
            
            if (i == 0)
                tempColor = Color.rgb(255, 255, 199);
            
            else if (rowIndx == 7)
                tempColor = Color.rgb(192, 255, 255);
            
            else if (rowIndx == 8)
                tempColor = Color.rgb(197, 255, 234);
            
            tempPane = new VBox(new Label( "" + (i + 1)), new Label(atoms[i].getSymbol()));
            tempPane.setBackground(new Background(new BackgroundFill(tempColor, null, null)));
            //tempPane.setBorder(new Border(new BorderStroke(Color.BLACK, null, null, BorderStroke.THICK)));
            tempPane.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    VBox tempBox = (VBox) event.getSource();
                    Dragboard db = tempBox.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    
                    
                    content.put(data, new TableElement(tempBox.getChildren(), tempBox.getBackground()));
                    db.setContent(content);
                    event.consume();
                }
            });
            gridPane.add(tempPane, colIndx, rowIndx);
        }
    }
    
    public void removeString(String originalString, String elementName) {
        //String tempStr = txtManual.getText().replaceFirst(elementName, "");
        String[] atomList = getAtoms(parseInput());
        ArrayList<String> atomsArray = new ArrayList<>(Arrays.asList(atomList));
        System.out.println(Arrays.toString(atomList));
        
        atomsArray.remove(elementName);
        System.out.println(atomsArray);
        
        ArrayList<String> alFinished = concentrateStr(atomsArray);
                
        String tempStr = "";
        for (String el: alFinished)
            if (!el.equals("1"))
                  tempStr += el;
        
        txtManual.setText(tempStr);
    }
    
    public String[] parseInput() {
        String text = txtManual.getText();
        if(text.isEmpty()) {
            return new String[0];
        }
        
        LinkedList<String> llSymbols = splitString(text);
        
        ArrayList<String> alFormatted = addOnes(llSymbols);
        
        String[] arrayToSortSymbols = getAtoms(alFormatted.toArray(new String[alFormatted.size()]));
        
        int[] arrayDoneSorting = sortArray(arrayToSortSymbols);
        
        ArrayList<String> alName = numToSymbol(arrayDoneSorting);
        
        ArrayList<String> alFinished = concentrateStr(alName);
        
        // Uncomment if want to see the array returned
        // System.out.println("alFinished: " + alFinished);
        return alFinished.toArray(new String[alFinished.size()]);
    }
    
    public ArrayList<String> numToSymbol(int[] numArr) {
        ArrayList<String> returnArr = new ArrayList<>(numArr.length);
        
        for (int currNum : numArr)
            returnArr.add(atoms[currNum - 1].getSymbol());
        return returnArr;
    }
    
    public ArrayList<String> concentrateStr(ArrayList<String> alName) {
        // Converting back to [H, 2, O] kinda thing
        ArrayList<String> alFinished = new ArrayList<>();
        
        boolean firstTime = true;
        String currSymbol = "";
        int numTimesFound = 0;
        
        // Input 
//        System.out.println("Input: " + Arrays.toString(arrayDoneSorting));
        for(int i = 0; i < alName.size(); i++) {
            if(firstTime == true) {
                currSymbol = alName.get(i);
                alFinished.add(currSymbol);
                numTimesFound = 1;
                firstTime = false;
            }
            // It's a number now
            else {
                if(alName.get(i).equals(currSymbol)) {
                    numTimesFound++;
                } 
                else {
                    alFinished.add("" + numTimesFound);
                    firstTime = true;
                    i--;
                }
            }
        }
        if (numTimesFound != 0)
            alFinished.add("" + numTimesFound);
        
        return alFinished;
    }
    
    private int[] bubbleSort(int arr[]) {
        boolean isUnsorted = true;
        
        while (isUnsorted) {
            isUnsorted = false;
            
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i] > arr[i+1]) {
                    isUnsorted = true;
                    int temp = arr[i];
                    arr[i] = arr[i+1];
                    arr[i+1] = temp;
                }
            }
        }   
        return arr;
    }
    
    public int[] sortArray(String[] arrayToSortSymbols) {
        //** Sorting section **
        
        // Create an array to bubble sort
        int[] arrayToSortNumbers = new int[arrayToSortSymbols.length];
        for(int i = 0; i < arrayToSortNumbers.length; i++) {
            for (Atom atom : atoms) {
                if (arrayToSortSymbols[i].equals(atom.getSymbol())) {
                    arrayToSortNumbers[i] = atom.getNumber();
                }
            }
            
            
            if(arrayToSortNumbers[i] == 0) {
                System.out.println("Element: " + arrayToSortSymbols[i] + " not found.");
                arrayToSortNumbers[i] = -1;
            }
        }
        
        return bubbleSort(arrayToSortNumbers);
    }
    
    public ArrayList<String> addOnes(LinkedList<String> llSymbols) {
        ArrayList<String> alFormatted = new ArrayList<>();        
        // Formatting the output and checking if the total number of atoms exceeds the limit (20)
        for(int i = 0; i < llSymbols.size(); i++) {
            // If it's the last - must be letter
            if(i == llSymbols.size() - 1 && Character.isLetter(llSymbols.getLast().charAt(0))) {
                alFormatted.add(llSymbols.getLast());
                alFormatted.add(""+1);
            }
            // If it doesn't contain a number
            else if(Character.isLetter(llSymbols.get(i).charAt(0))) {
                // If the next value is a number, add the symbol and the number to the list
                if(Character.isDigit(llSymbols.get(i + 1).charAt(0))) {
                    alFormatted.add(llSymbols.get(i));
                    alFormatted.add(llSymbols.get(i + 1));
                    i++;
                }
                // If it doesn't contain a number after the symbol, add a 1 to the list 
                else {
                    alFormatted.add(llSymbols.get(i));
                    alFormatted.add(""+1);
                }
            }
        }
        
        return alFormatted;
    }
    
    public LinkedList<String> splitString(String text) {
        char[] llInput = new char[text.length()];
        // Converting input to a linked list
        for(int i = 0; i < text.length(); i++) {
            llInput[i] = text.charAt(i);
        }
        LinkedList<String> llSymbols = new LinkedList<>();
        
        // Getting ready for parsing
        boolean started = false;
        boolean startedNumber = false;
        String symbol = "";
        // Converting linked list array of character
        for(int j = 0; j < llInput.length; j++) {
            // >>H<<Co2C4, H>>C<<o2C4, HCo>>2<<C4, HCo2>>C<<4, HCo2C>>4<<
            if(!started) {
                if(Character.isDigit(llInput[j])) {
                    startedNumber = true;
                    llSymbols.add(symbol);
                    symbol = Character.toString(llInput[j]);  
                }
                else {
                    started = true;
                    symbol += llInput[j];
                    startedNumber = false;
                }
            }
            // HC>>o<<2C4
            else if(Character.isLowerCase(llInput[j])) {
                symbol += llInput[j];
                if (startedNumber) {
                    System.out.println("error");
                }
            }
            // H>>C<<o2C4
            else if(Character.isUpperCase(llInput[j])) {
                llSymbols.add(symbol);
                startedNumber = false;
                symbol = Character.toString(llInput[j]);
            }
            // HCo>>2<<C4, HCo2C>>4<<
            else if(Character.isDigit(llInput[j])) {
                if (startedNumber)
                    symbol += llInput[j];
                else {
                    startedNumber = true;
                    llSymbols.add(symbol);
                    symbol = Character.toString(llInput[j]); 
                }
            }
        }
        llSymbols.add(symbol);
        
        return llSymbols;
    }
    
    static public String[] getAtoms(String[] input) {
        int size = 0;
        for(int i = 1; i < input.length; i+=2) {
            size += Integer.parseInt(input[i]);
        }
        
        String[] returnStr = new String[size];
        
        int offset = 0;
        int prevOffset = 0;
        for (int i = 0; i < input.length - 1; i+=2) {
            prevOffset = offset;
            for (; offset < prevOffset + Integer.parseInt(input[i+1]); offset++)
            {
                returnStr[offset] = input[i];
            }
        }
        
        return returnStr;
    }

    void setAtoms(Atom[] atoms) {
        this.atoms = atoms;
    }
}
