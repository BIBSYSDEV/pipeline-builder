package no.bibsys.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StringUtils {


    /**
     * Lowercases the input string, replaces underscores with dashes
     *
     *
     *
     * @param input The string to be normalized
     * @return The normalized String
     */
    public String normalizeString(String input){
        String res= input
            .toLowerCase(Locale.getDefault())
            .replaceAll("_","-")
            .replaceAll("[^-a-z0-9]","");

        return res;
    }


    /**
     * Lowercases the input string, replaces underscores with dashes,
     * and truncates each word (string between two dashes) to {@code maxWordLength}
     *
     *
     *
     * @param input The string to be normalized
     * @param maxWorldLength max number of characters between two dashes
     * @return a normalized String
     */
    public String shortNormalizedString(String input,int maxWorldLength){
        String[] words = normalizeString(input).split("-");
        int maxnumberOfWords = Math.min(maxWorldLength, words.length);
        List<String> wordList = Arrays.stream(words).map(word->shorten(word,maxWorldLength))
            .collect(Collectors.toList()).subList(0,maxnumberOfWords);

        return String.join("-", wordList);
    }


    private String shorten(String word,int maxLength) {
        int maxIndex = Math.min(word.length(), maxLength);
        return word.substring(0, maxIndex);
    }





}
