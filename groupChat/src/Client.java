import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void enviaMensagem(){
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String mensagem = scanner.nextLine();
                bufferedWriter.write(username + ": " + mensagem);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    public void recebeMensagem(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String mensagemChat;
                while(socket.isConnected()){
                    try{
                        mensagemChat = bufferedReader.readLine();
                        if (mensagemChat == null) { //quando o socket é fechado do outro lado, isso é recebido como uma mensagem nula
                            closeEverything(socket, bufferedReader, bufferedWriter); //adicio
                            break; //adicio
                        }
                        else {
                            System.out.println(mensagemChat);
                            if (mensagemChat.equals("SERVIDOR: AVISO: Você foi desconectado!")) {
                                closeEverything(socket, bufferedReader, bufferedWriter);
                            }
                        }
                    } catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedReader!=null) {
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null) {
                socket.close();
            }
            System.exit(0); //encerra a execução do programa no lado do cliente
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main (String [] args) throws IOException {
        Scanner scanner = new Scanner(System.in); //
        System.out.println("Entre o nome do seu usuário para ser usado no Chat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        System.out.println("Bem vindo ao Chat, " + client.username + "!\nCaso queira sair do chat é só escrever 'EXIT'!"); //AQUIIII
        client.recebeMensagem();
        client.enviaMensagem();
    }
}
