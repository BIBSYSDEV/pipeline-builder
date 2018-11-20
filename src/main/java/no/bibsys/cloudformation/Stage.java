package no.bibsys.cloudformation;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum Stage {

    TEST,FINAL;



    @Override
    public String toString(){
        return this.name().toLowerCase(Locale.getDefault());
    }


    public static Stage fromString(String stage) {
        if (stage.equalsIgnoreCase(FINAL.name())) {
            return FINAL;
        } else if (stage.equalsIgnoreCase(TEST.name())) {
            return TEST;
        } else {
            throw new IllegalArgumentException("Allowed stages:"+String.join(",",listStages()));
        }
    }


    public static List<String> listStages() {

        List<String> stages = new ArrayList<>();
        stages.add(TEST.toString());
        stages.add(FINAL.toString());
        return stages;
    }




}
