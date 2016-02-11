package fr.rsommerard.privacyaware.demo;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Demo {

    public static Integer getRandomColor() {
        Random rand = new Random();

        List<Integer> colors = getColors();

        return getColors().get(rand.nextInt(colors.size()));
    }

    private static List<Integer> getColors() {
        List<Integer> colors = new ArrayList<>();

        colors.add(Color.argb(255, 65, 229, 217));
        colors.add(Color.argb(255, 59, 76, 75));
        colors.add(Color.argb(255, 229, 65, 206));
        colors.add(Color.argb(255, 188, 119, 143));
        colors.add(Color.argb(255, 204, 108, 61));
        colors.add(Color.argb(255, 153, 133, 122));
        colors.add(Color.argb(255, 132, 178, 255));

        return colors;
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
