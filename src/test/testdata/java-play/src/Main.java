import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String thing = "hello";
        App a = new App();
        Ma m = new HashMap<String, String>();
        m<caret>.put("wh", "hello");

        System.out.println(a.test);
    }
}

