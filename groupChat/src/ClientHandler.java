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
            this.usernameCliente = bufferedReader.readLine();
            this.timerInatividade = new Timer();
            this.timerAguardoRetornoAtiv = new Timer();
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
                else{
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

                    broadcastMessage(mensagemDoCliente);
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
                    String aviso = "SERVIDOR: AVISO: Na ausência de atividade, você será desconectado em alguns instantes";
                    bufferedWriter.write(aviso);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    aguardaRetornoAtiv(); //esperamos por uma nova atividade nos próximos 1 min antes de desconectar o cliente
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        timerInatividade.schedule(tarefa, 30000); //enviamos o aviso à cada 5 minutos corridos de inatividade - 300000
    }

    public void aguardaRetornoAtiv(){
        TimerTask tarefa = new TimerTask() {
            @Override
            public void run() {
                try {
                    String aviso = "SERVIDOR: AVISO: Você foi desconectado!";
                    bufferedWriter.write(aviso);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        timerAguardoRetornoAtiv.schedule(tarefa, 15000); //esperamos mais 1 min antes de fechar a conexão - 60000
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