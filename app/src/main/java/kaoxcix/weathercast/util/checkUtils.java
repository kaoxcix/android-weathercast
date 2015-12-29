package kaoxcix.weathercast.util;

public class checkUtils {
    public checkUtils() {
    }

    public Boolean isEnglishAlphabetString(String string){
        if(string.matches("[a-zA-Z ]+")) {
            return true;
        } else {
            return false;
        }
    }
}
