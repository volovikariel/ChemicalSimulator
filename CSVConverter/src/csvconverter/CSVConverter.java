package csvconverter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author Jorge Marcano
 */
public class CSVConverter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Storing all the Atoms' info in Utils.getAtomList
        new JsonToDatabase().jsonToDatabase("PeriodicTableJSON.json");

        // Creates CSV file form JSON Database
        try (CSVPrinter printer = new CSVPrinter(new FileWriter("elements.csv"), CSVFormat.DEFAULT)) {
            ArrayList<Atom> lst = Utils.getAtomList();
            printer.printRecord("Symbol", "Shells", "Electronegativity");
            for(Atom a : lst) {
                printer.printRecord(a.getSymbol(), a.getShells(), a.getElectronegativity_pauling());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
