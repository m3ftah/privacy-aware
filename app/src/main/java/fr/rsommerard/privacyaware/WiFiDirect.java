package fr.rsommerard.privacyaware;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class WiFiDirect {

    public static final String TAG = "WiFiDirect";

    public static String getRandomIdentifier() {
        Random rand = new Random();

        return Integer.toString(rand.nextInt(100000));
    }

    public static String getRandomContent() {
        Random rand = new Random();

        List<String> words = new ArrayList<>();
        words.add("Épitaphe");
        words.add("Bistouquette");
        words.add("Rhododendron");
        words.add("Gourgandine");
        words.add("Chouette");
        words.add("Esperluette");
        words.add("Corniche");
        words.add("Irrémédiable");
        words.add("Gargantuesque");
        words.add("Opercule");
        words.add("Pissenlit");
        words.add("Pommelé");
        words.add("Cataracte");
        words.add("Libellule");
        words.add("Inexorable");
        words.add("Frangipane");
        words.add("Fracas");
        words.add("Pamplemousse");
        words.add("Époustouflant");
        words.add("Ornithorynque");
        words.add("Papouille");
        words.add("Rascasse");
        words.add("Concupiscence");
        words.add("Parapluie");
        words.add("Margoulette");
        words.add("Clapotis");
        words.add("Nuage");
        words.add("Éphémère");

        return words.get(rand.nextInt(words.size()));
    }
}
