package wordnet;

import rita.RiWordNet;

public class WordNetMain {
    public static void main(String args[])
    {

        RiWordNet wordnet = new RiWordNet("/Users/satheeshchandra/Desktop/KDM/WordNet-3.0");

        // Demo finding a list of related words (synonyms)
        String word = "sibling";
        String[] poss = wordnet.getPos(word);
        for (int j = 0; j < poss.length; j++) {
            System.out.println("\n\nSynonyms for " + word + " (pos: " + poss[j] + ")");
            String[] synonyms = wordnet.getAllSynonyms(word,poss[j],10);
            for (int i = 0; i < synonyms.length; i++) {
                System.out.println(synonyms[i]);
            }
        }

    }
}
