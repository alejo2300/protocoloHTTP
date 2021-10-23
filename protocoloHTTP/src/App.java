import java.net.ServerSocket;
import java.net.Socket;

public class App {

    public static void main( String[] args ) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {//Open on 8080
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    clientManage clientNew = new clientManage(client);
                    clientNew.readGetValues();
                }
            }
        }
    }        
}