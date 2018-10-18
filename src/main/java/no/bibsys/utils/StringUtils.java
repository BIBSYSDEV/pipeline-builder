package no.bibsys.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StringUtils {


    private final static  transient int maxLength=6;

    public String normalizeString(String input){
        String res= input
            .toLowerCase(Locale.getDefault())
            .replaceAll("_","-")
            .replaceAll("[^-a-z0-9]","");

        return res;
    }


    public String shortNormalizedString(String input){
        String[] words = normalizeString(input).split("-");
        int maxnumberOfWords = Math.min(maxLength, words.length);
        List<String> wordList = Arrays.stream(words).map(this::shorten)
            .collect(Collectors.toList()).subList(0,maxnumberOfWords);
        return String.join("-", wordList);

    }


    private String shorten(String word) {
        int maxIndex = Math.min(word.length(), maxLength);
        return word.substring(0, maxIndex);
    }

}