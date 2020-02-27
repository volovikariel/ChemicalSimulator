package mainapplication;

public class Atom {
    private String symbol;
    private int shells;
    private double electroneg;
    
    public Atom() {}
    
    public Atom(String symbol, int shells, double electroneg) {
        this.symbol = symbol;
        this.shells = shells;
        this.electroneg = electroneg;
    }
    
    public Atom(String symbol, String shells, String electroneg) {
        this.symbol = symbol;
        this.shells = Integer.parseInt(shells);
        this.electroneg = Double.parseDouble(electroneg);
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

    @Override
    public String toString() {
        return "Atom{"  + symbol + "," + shells + "," + electroneg + '}';
    }
    
    
    
}
