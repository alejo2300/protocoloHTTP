import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    public static void main( String[] args ) throws Exception {

        //Set virtual host for redirecction
        System.out.println("PROXY REDES ENTREGA 1 : CONFIGURACION SITIOS VIRTUALES");
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Digite el número de sitios virtuales a configurar");
        int virtualURLSize = Integer.parseInt(inputScanner.nextLine());
        List<String> virtualHostList = new ArrayList<String>();
        for (int i = 0; i < virtualURLSize*3; i=i+3) {
            String virtualHost, realHost, rootDirecory;
            System.out.println("Digite el host virtual: ");
            virtualHost = inputScanner.nextLine();
            System.out.println("Digite el host real: ");
            realHost = inputScanner.nextLine();
            System.out.println("Digite el directorio raiz: ");
            rootDirecory = inputScanner.nextLine();

            virtualHostList.add(virtualHost);
            virtualHostList.add(realHost);
            virtualHostList.add(rootDirecory);
        }


        try (ServerSocket serverSocket = new ServerSocket(8080)) {//Open on 8080
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    clientManage clientNew = new clientManage(client,virtualHostList);
                    clientNew.readGetValues();
                }
            }
        }
    }        
}