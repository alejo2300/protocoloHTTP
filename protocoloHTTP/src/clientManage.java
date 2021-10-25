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
    List<String> virtualRedirection = new ArrayList<String>();

    public clientManage(Socket client, List<String> virtualRedirection) {
        this.client = client;
        for (int i = 0; i < virtualRedirection.size(); i++) {
            this.virtualRedirection.add(virtualRedirection.get(i));
        }
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

        String urlToConsult;
        // See the tipe of method
        if (method.equals("GET")) {
            //Check the url to consult and checking if is a virtual host
            urlToConsult = path;
            urlToConsult = virtualRedirection(host,path,urlToConsult);
            System.out.println("URL a consultar: " + urlToConsult);

            System.out.println("Method: " + method + "\nURL: " + urlToConsult + "\nPath: " + path + "\nVersion: "
                    + version + "\nHost: " + host + "\n" + headers.get(4) + "\n" + headers.get(0));
            // Print headers
            for (int i = 1; i < 4; i++) {
                System.out.println(headers.get(i));
            }

        } else if (method.equals("POST")) {
            urlToConsult = path;
            urlToConsult = virtualRedirection(host,path,urlToConsult);
            System.out.println("URL a consultar: " + urlToConsult);
            //urlToConsult = virtualRedirection(host,path,method,urlToConsult);

            System.out.println("Method: " + method + "\nURL: " + urlToConsult + "\nPath: " + path + "\nVersion: "
                    + version + "\nHost: " + host);

        } else if (method.equals("CONNECT")) {
            urlToConsult = urlSetterConnect(path);
            System.out.println("Method: " + method + "\nURL: " + urlToConsult + "\nPath: " + path + "\nVersion: "
                    + version + "\nHost: " + host);

        } else {// Other methods
            urlToConsult = urlSetter(host, path);
            System.out.println("Method: " + method + "\nURL: " + urlToConsult + "\nPath: " + path + "\nVersion: "
                    + version + "\nHost: " + host);
        }
        System.out.println("\n\n");

        // Manage localhost
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
        } else {
            // Internet consult

            // Check for virtualEnviroment
            if(method.equals("GET")){
                sendInternetResponse(client, urlToConsult, method);
            }else if(method.equals("POST")){

            }else{
                System.out.println("\nEl metodo actaul no es ni get ni post, por lo cual no se responde\n");
            }
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

    private static void sendInternetResponse(Socket client, String urlconsult, String method) {
        try {
            System.out.println("\nInto internet consult\n");
            URL url = new URL(urlconsult);
            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            String status = Integer.toString(urlcon.getResponseCode());
            
            
            OutputStream clientOutput = client.getOutputStream();
            clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
            clientOutput.write(("ContentType: " + urlcon.getContentType() + "\r\n").getBytes());
            clientOutput.write("\r\n".getBytes());
            InputStream stream = urlcon.getInputStream();
            int i;
            while ((i = stream.read()) != -1) {
                clientOutput.write((byte) i);
            }
            clientOutput.write("\r\n\r\n".getBytes());
            clientOutput.flush();
            client.close();

        } catch (Exception e) {
            System.out.println("There is a problem "+ e);
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
        // get only the port no the port
        StringTokenizer tokens = new StringTokenizer(host, ":");


        return "http://" + host + path.substring(6,path.length());
    }

    private static String urlSetterConnect(String path) {
        // get only the port no the port
        StringTokenizer tokens = new StringTokenizer(path, ":");
        return "http://" + tokens.nextToken();
    }

    private String virtualRedirection(String host, String path, String currentURL) {
        for (int i = 0; i < this.virtualRedirection.size(); i=i+3) {
            if(this.virtualRedirection.get(i).equals(host)){
                return "http://" + this.virtualRedirection.get(i+1) + this.virtualRedirection.get(i+1) + path.substring(host.length()+7,path.length());
            }
        }
        return currentURL;
    }
}
