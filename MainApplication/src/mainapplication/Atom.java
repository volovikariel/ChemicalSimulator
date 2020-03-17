package mainapplication;

public class Atom {
    private String symbol;
    private int shells;
    private double electroneg;
    private boolean isMetal;
    
    private String color;
    
    private int number;
    
    public Atom() {}
    
    public Atom(String symbol, int shells, double electroneg) {
        this.symbol = symbol;
        this.shells = shells;
        this.electroneg = electroneg;
    }
    
    public Atom(String symbol, String shells, String electroneg, String isMetal, int number, String color) {
        this.symbol = symbol;
        this.shells = Integer.parseInt(shells);
        this.electroneg = Double.parseDouble(electroneg);
        this.number = number;
        this.isMetal = isMetal.equals("1");
        this.color = color;
    }
    
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getShells() {
        return shells;
    }

    public void setShells(int shells) {
        this.shells = shells;
    }

    public double getElectroneg() {
        return electroneg;
    }

    public void setElectroneg(double electroneg) {
        this.electroneg = electroneg;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Atom{"  + symbol + "," + shells + "," + electroneg + '}';
    }

    public boolean isIsMetal() {
        return isMetal;
    }

    public void setIsMetal(boolean isMetal) {
        this.isMetal = isMetal;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
