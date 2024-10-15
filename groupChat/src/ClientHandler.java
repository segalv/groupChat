import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList <ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usernameCliente;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.usernameCliente = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVIDOR: " + usernameCliente + " entrou no Chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run(){
        String mensagemDoCliente;
        while (socket.isConnected()){
            try{
                mensagemDoCliente = bufferedReader.readLine();
                if (mensagemDoCliente == null) { //adicio
                    closeEverything(socket, bufferedReader, bufferedWriter); //adicio
                    break; //adicio
                }
                broadcastMessage(mensagemDoCliente);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String mensagem){
        for (ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.usernameCliente.equals(usernameCliente)){
                    clientHandler.bufferedWriter.write(mensagem);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVIDOR: " + usernameCliente + " saiu do Chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null) {
                bufferedWriter.close();
            }
            if(socket!=null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}