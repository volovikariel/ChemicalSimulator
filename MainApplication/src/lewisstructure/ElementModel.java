/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lewisstructure;

/**
 *
 * @author cstuser
 */
public class ElementModel {
    public String symbol;
    public int shell;

    public ElementModel(String symbol, int shell) {
        this.symbol = symbol;
        this.shell = shell;
    }

    public ElementModel(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getShell() {
        return shell;
    }

    public void setShell(int shell) {
        this.shell = shell;
    }
    
}
