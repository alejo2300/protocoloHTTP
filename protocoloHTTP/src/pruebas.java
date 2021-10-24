import java.util.StringTokenizer;

public class pruebas {
    public static void main(String[] args) {
        String pepe = "localhost:8080";
        StringTokenizer tokens = new StringTokenizer(pepe, ":");
        System.out.println(tokens.nextToken(":")); 
    }
}
