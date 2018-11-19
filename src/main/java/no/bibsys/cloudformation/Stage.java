package no.bibsys.cloudformation;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum Stage {

    TEST,FINAL;



    @Override
    public String toString(){
        return this.name().toLowerCase(Locale.getDefault());
    }


    public static Optional<Stage> fromString(String stage) {
        if (stage.equalsIgnoreCase(FINAL.name())) {
            return Optional.of(FINAL);
        } else if (stage.equalsIgnoreCase(TEST.name())) {
            return Optional.of(TEST);
        } else {
            return Optional.empty();
        }
    }


    public static List<String> listStages() {

        List<String> stages = new ArrayList<>();
        stages.add(TEST.toString());
        stages.add(FINAL.toString());
        return stages;
    }




}
