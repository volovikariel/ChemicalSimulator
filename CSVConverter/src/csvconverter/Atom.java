package csvconverter;

public class Atom {

    private String name;
    private String symbol;
    private double atomicMass;
    private int shells;
    private double electronegativity_pauling;

    public Atom(String name, String symbol, double atomicMass, int shells, double electronegativity_pauling) {
        this.name = name;
        this.symbol = symbol;
        this.atomicMass = atomicMass;
        this.shells = shells;
        this.electronegativity_pauling = electronegativity_pauling;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getAtomicMass() {
        return atomicMass;
    }

    public int getShells() {
        return shells;
    }

    public double getElectronegativity_pauling() {
        return electronegativity_pauling;
    }

    @Override
    public String toString() {
        return "Atom{" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", atomicMass=" + atomicMass +
                ", shells='" + shells + '\'' +
                ", electronAffinity=" + electronegativity_pauling +
                '}';
    }
}
