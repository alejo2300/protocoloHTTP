import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class clientManage {
    Socket client;

    public clientManage(Socket client) {
        this.client = client;
    }

    public void readGetValues() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }
        // Split request info
        String request = requestBuilder.toString();
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];
        String version = requestLine[2];
        String host = requestsLines[1].split(" ")[1];

        List<String> headers = new ArrayList<>();
        for (int h = 2; h < requestsLines.length; h++) {
            String header = requestsLines[h];
            headers.add(header);
        }

        //Get complete url
        String urlToConsult = urlSetter(host, path);
        System.out.println("Method: " + method + "\nURL: " + urlToConsult + "\nPath: " + path + "\nVersion: " + version + "\nHost: " + host + "\n"
                + headers.get(4) + "\n" + headers.get(0));
        // Print accept section
        for (int i = 1; i < 4; i++) {
            System.out.println(headers.get(i));
        }
        System.out.println("\n\n");
        // Print Headers
        /*
         * for (int i = 0; i < headers.size(); i++) { System.out.println(i + ": "
         * +headers.get(i)); }
         */

        if (host.contains("localhost")) {
            Path filePath = getFilePath(path);
            if (Files.exists(filePath)) {
                // file exist
                String contentType = guessContentType(filePath);
                sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
            } else {
                // 404
                byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
                sendResponse(client, "404 Not Found", "text/html", notFoundContent);
            }
        }else{
            //Internet consult 
            System.out.println("Atara");

        }
    }

    private static void sendResponse(Socket client, String status, String contentType, byte[] content)
            throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    private static void sendInternetResponse(Socket client, String urlconsult){
        //Create the consult
        try {
            URL uconsult = new URL(urlconsult);
            HttpURLConnection hr = (HttpURLConnection)uconsult.openConnection();
            if(hr.getResponseCode() == 200){
                OutputStream clientOutput = client.getOutputStream();
                clientOutput.write(("HTTP/1.1 \r\n" + hr.getResponseCode()).getBytes());
                clientOutput.write(("ContentType: " + hr.getContentType() + "\r\n").getBytes());
                clientOutput.write("\r\n".getBytes());
                //Conten to bytes array
                byte[] content = convertObjectToBytes2(hr.getContent());
                clientOutput.write(content);
                clientOutput.write("\r\n\r\n".getBytes());
                clientOutput.flush();
                client.close();
            }
        } catch (Exception e) {
            System.out.println("Url inancanzable");
        }
        
    }

    public static byte[] convertObjectToBytes2(Object obj) throws IOException {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try (ObjectOutputStream ois = new ObjectOutputStream(boas)) {
            ois.writeObject(obj);
            return boas.toByteArray();
        }
    }

    private static Path getFilePath(String path) {
        return Paths.get("/tmp/www/MyPresentationPage/", path);
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    private static String urlSetter(String host, String path) {
        //get only the port no the port
        StringTokenizer tokens = new StringTokenizer(host,":");

        return "http://"+tokens.nextToken()+path;
    }
}
