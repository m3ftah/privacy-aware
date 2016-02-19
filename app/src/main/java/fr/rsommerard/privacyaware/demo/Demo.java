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

        colors.add(Color.rgb(233, 30, 99)); // Pink
        colors.add(Color.rgb(156, 39, 176)); // Purple

        colors.add(Color.rgb(103, 58, 183)); // Deep Purple
        colors.add(Color.rgb(63, 81, 181)); // Indigo
        colors.add(Color.rgb(33, 150, 243)); // Blue

        colors.add(Color.rgb(3, 169, 244)); // Light Blue
        colors.add(Color.rgb(0, 188, 212)); // Cyan
        colors.add(Color.rgb(0, 150, 136)); // Teal

        colors.add(Color.rgb(139, 195, 74)); // Light Green
        colors.add(Color.rgb(205, 220, 57)); // Lime

        colors.add(Color.rgb(255, 235, 59)); // Yellow
        colors.add(Color.rgb(255, 193, 7)); // Amber
        colors.add(Color.rgb(255, 152, 0)); // Orange

        colors.add(Color.rgb(255, 87, 34)); // Deep Orange
        colors.add(Color.rgb(121, 85, 72)); // Brown
        colors.add(Color.rgb(158, 158, 158)); // Grey

        colors.add(Color.rgb(96, 125, 139)); // Blue Grey

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
