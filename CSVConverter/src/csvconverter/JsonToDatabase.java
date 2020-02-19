package csvconverter;


import org.json.JSONObject;
import java.util.ArrayList;

public class JsonToDatabase {

        public static void jsonToDatabase(String filePath) {
            String jsonFile = Utils.fileToStrings(filePath);
            JSONObject jsonParentObject = new JSONObject(jsonFile);

            ArrayList<Atom> atomList = new ArrayList<>();
            Atom atom;
            for(int i = 0; i < jsonParentObject.getJSONArray("elements").length(); i++) {
                JSONObject atomI = jsonParentObject.getJSONArray("elements").getJSONObject(i);
                if(atomI.isNull("electronegativity_pauling")) {
                    atom = new Atom(atomI.getString("name"), atomI.getString("symbol"), (double) atomI.getDouble("atomic_mass"), atomI.getJSONArray("shells").getInt(atomI.getJSONArray("shells").length() - 1), -1);
                } else {
                    atom = new Atom(atomI.getString("name"), atomI.getString("symbol"), (double) atomI.getDouble("atomic_mass"), atomI.getJSONArray("shells").getInt(atomI.getJSONArray("shells").length() - 1), (double) atomI.getDouble("electronegativity_pauling"));
                }
                atomList.add(atom);
            }
            Utils.setAtomList(atomList);
        }

}
