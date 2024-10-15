import java.io.*;
import java.net.Socket;
import java.util.Scanner;
//ADIC
//import java.util.Timer;
//import java.util.TimerTask;


public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    //private Timer timerInatividade; //ADIC
    //private Timer timerAguardoRetornoAtiv; //ADIC


    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            //this.timerInatividade = new Timer(); //ADIC
            //this.timerAguardoRetornoAtiv = new Timer(); //ADIC

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

                /*
                //caso uma mensagem seja enviada, "restartamos" o timer,
                // impedindo que a conexão seja interrompida por mais 5 minutos!
                if (timerInatividade != null) {
                    timerInatividade.cancel();
                }
                timerInatividade = new Timer();
                avisoInatividade();
                if (timerAguardoRetornoAtiv != null) {
                    timerAguardoRetornoAtiv.cancel();
                }
                timerAguardoRetornoAtiv = new Timer();
                */
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }



    /*
    public void avisoInatividade(){
        TimerTask tarefa = new TimerTask() {
            @Override
            public void run() {
                System.out.println("AVISO: Na ausência de atividade, você será desconectado em alguns instantes");
                aguardaRetornoAtiv(); //esperamos por uma nova atividade nos próximos 1 min antes de desconectar o cliente
                System.out.println("TESTEEE1");

            }
        };
        timerInatividade.schedule(tarefa, 30000); //enviamos o aviso à cada 5 minutos corridos de inatividade - 300000
    }

    public void aguardaRetornoAtiv(){
        TimerTask tarefa = new TimerTask() {
            @Override
            public void run() {
                closeEverything(socket, bufferedReader, bufferedWriter);
                System.out.println("AVISO: Você foi desconectado!");
                System.exit(0);
            }
        };
        timerAguardoRetornoAtiv.schedule(tarefa, 15000); //esperamos mais 1 min antes de fechar a conexão - 60000
    }
    */


    public void recebeMensagem(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String mensagemChat;

                while(socket.isConnected()){
                    try{
                        mensagemChat = bufferedReader.readLine();
                        System.out.println(mensagemChat);
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
        //client.avisoInatividade();
        client.recebeMensagem();
        client.enviaMensagem();
    }
}
