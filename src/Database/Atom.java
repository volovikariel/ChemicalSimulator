package Database;

public class Atom {

    private String name;
    private String symbol;
    private float atomicMass;
    private String shells;
    private float electronegativity_pauling;

    public Atom(String name, String symbol, float atomicMass, String shells, float electronegativity_pauling) {
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

    public float getAtomicMass() {
        return atomicMass;
    }

    public String getShells() {
        return shells;
    }

    public float getElectronegativity_pauling() {
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
