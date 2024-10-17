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
            bufferedWriter.write("CONNECT|" + username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String mensagem = scanner.nextLine();
                if (mensagem.equals("EXIT")){
                    bufferedWriter.write("EXIT|" + username);
                }
                else{
                    bufferedWriter.write("MSG|" + username + "|" + mensagem);
                }
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
                        if (mensagemChat == null) {
                            closeEverything(socket, bufferedReader, bufferedWriter);
                            break;
                        }
                        else {
                            // Adicionar lógica para tratamento de diferentes tipos de mensagem
                            if (mensagemChat.startsWith("NOTIFICATION|")) {
                                System.out.println("NOTIFICAÇÃO: " + mensagemChat.substring(14));
                            }else if (mensagemChat.startsWith("INACTIVE_WARNING|")) {
                                System.out.println("AVISO: " + mensagemChat.substring(17 + username.length() + 1));
                            }else if (mensagemChat.startsWith("DISCONNECTED|")) {
                                System.out.println("SERVIDOR: " + mensagemChat.substring(13));
                                closeEverything(socket, bufferedReader, bufferedWriter); //
                            } else { //é um broadcast
                                String mensagem = mensagemChat.substring(10);
                                mensagem = mensagem.replace("|", ": "); //tratamos a mensagem recebida para ser exibida no formato desejado
                                System.out.println(mensagem);
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
            System.exit(0);
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    public static void main (String [] args) throws IOException {
        Scanner scanner = new Scanner(System.in); //
        System.out.println("Entre o nome do seu usuário para ser usado no Chat (caracteres especiais não são permitidos): ");
        String username = scanner.nextLine();
        while(username.contains("|")){
          System.out.println("Nome de usuário inválido, insira um novo nome de usuário: "); //caso o usuário utilize o caracter especial '|' barramos a utilização pois atrapalharia o tratamento das mensagens de broadcast
          username = scanner.nextLine();
        }
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        System.out.println("Bem vindo ao Chat, " + client.username + "!\nCaso queira sair do chat é só escrever 'EXIT'!");
        client.recebeMensagem();
        client.enviaMensagem();
    }
}
