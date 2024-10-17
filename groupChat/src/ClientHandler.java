import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler implements Runnable{
    public static ArrayList <ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usernameCliente;
    private Timer timerInatividade;
    private Timer timerAguardoRetornoAtiv;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String connect = bufferedReader.readLine();
            this.usernameCliente = connect.substring(8); //seleciona só o conteúdo (username) de fato da mensagem CONNECT
            this.timerInatividade = new Timer();
            this.timerAguardoRetornoAtiv = new Timer();
            clientHandlers.add(this);
            broadcastMessage("NOTIFICATION|" + usernameCliente + " entrou no chat!");
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
                if (mensagemDoCliente == null) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                } else
                if (mensagemDoCliente.startsWith("EXIT|")) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                } else if (mensagemDoCliente.startsWith("MSG|")) {
                    //caso uma mensagem seja enviada, "restartamos" os timers,
                    // impedindo que a conexão seja interrompida por mais 6 minutos!
                    if (timerInatividade != null) {
                        timerInatividade.cancel();
                    }
                    timerInatividade = new Timer();
                    avisoInatividade();
                    if (timerAguardoRetornoAtiv != null) {
                        timerAguardoRetornoAtiv.cancel();
                    }
                    timerAguardoRetornoAtiv = new Timer();

                    String mensagemTransmissao = mensagemDoCliente.substring(4);  // Remove o prefixo "MSG|", mas mantem o nome do cliente, e transmite a mensagem
                    broadcastMessage(mensagemTransmissao);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void avisoInatividade(){
        TimerTask tarefa = new TimerTask() {
            @Override
            public void run() {
                try {
                    String aviso = "INACTIVE_WARNING|" + usernameCliente + "|Na ausência de atividade, você será desconectado em 1 minuto.";
                    bufferedWriter.write(aviso);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    aguardaRetornoAtiv(); //esperamos por uma nova atividade nos próximos 1 min antes de desconectar o cliente
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        timerInatividade.schedule(tarefa, 300000); //enviamos o aviso à cada 5 minutos corridos de inatividade
    }

    public void aguardaRetornoAtiv(){
        TimerTask tarefa = new TimerTask() {
            @Override
            public void run() {
                try {
                    String aviso = "DISCONNECTED|Você foi desconectado por inatividade"; //Alteração para p padrão do protocolo
                    bufferedWriter.write(aviso);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        timerAguardoRetornoAtiv.schedule(tarefa, 60000); //esperamos mais 1 min antes de fechar a conexão
    }


    public void broadcastMessage(String mensagem){
        for (ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.usernameCliente.equals(usernameCliente)){
                    clientHandler.bufferedWriter.write("BROADCAST|" + mensagem);
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
        broadcastMessage("NOTIFICATION|" + usernameCliente + " saiu do chat!");
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