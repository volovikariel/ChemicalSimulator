package sample;

import Database.Atom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Utils {

    private static ArrayList<Atom> atomList;

    public static String fileToStrings(String filepath) {
        String fileStrings = new String("");
        try(BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while((line = br.readLine()) != null) { fileStrings += line; }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileStrings;
    }

    public static ArrayList<Atom> getAtomList() {
        return atomList;
    }

    public static void setAtomList(ArrayList<Atom> atomListImported) {
        atomList = new ArrayList<>();
        for(int i = 0; i < atomListImported.size(); i++) {
            atomList.add(atomListImported.get(i));
        }
    }
}
