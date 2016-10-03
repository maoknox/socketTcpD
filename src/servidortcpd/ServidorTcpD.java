/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidortcpd;
/**
 *
 * @author USER
 */
import dBConnect.DbConnect;
import java.net.*; 
import java.io.*; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.IOUtils;
 
//import javax.activation.*;

public class ServidorTcpD extends Thread{
    protected static boolean serverContinue = true;
    protected Socket clientSocket;
    public InputStream in;
    DataInputStream string;
    InputStreamReader stringRead;
    BufferedReader bufferReader;
    StringBuffer sb=new StringBuffer();
    static DataOutputStream os;
    static Connection conect;
    static DataOutputStream it;
    static int i=0;
    //static ArrayList clients = new ArrayList<>();
    static Map<String, DataOutputStream> clients = new HashMap<String, DataOutputStream>();
    static Map<String, HashMap<String, String>> comandos = Collections.synchronizedMap(new HashMap<String, HashMap<String, String>>());
    //static ConcurrentHashMap<String, ConcurrentHashMap<String,String>> chm = new ConcurrentHashMap<String,  ConcurrentHashMap<String,String>>();
    //Iterator<DataOutputStream> it = clients.iterator();
    //static DbConnect con=new DbConnect();
    public static void main(String[] args) throws IOException, ClassNotFoundException{ 
        ServerSocket serverSocket = null; 
        int port=8010;//Integer.parseInt(args[0]);
        try { 
             serverSocket = new ServerSocket(port); 
             System.out.println ("Connection Socket Created");
             //conect=con.conecta();
             try { 
                  while (serverContinue){
                        System.out.println ("Waiting for Connection");
                        //Socket ss =serverSocket.accept();
                        
//                            os = new DataOutputStream(ss.getOutputStream());
//                            clients.add(i,os);
//                            i++;
                        //System.out.println( ss.toString());
                         //DataOutputStream it = (DataOutputStream) clients.get(0);
//                        DataOutputStream it = (DataOutputStream) clients.get(0);
//                        OutputStreamWriter osw = new OutputStreamWriter(it);
//                        BufferedWriter bw = new BufferedWriter(osw);
//                        bw.write("sdf");               
//                        bw.flush();
                        new ServidorTcpD(serverSocket.accept()).start(); 
                     }
                 } 
             catch (IOException e) 
                 { 
                  System.err.println("Accept failed."); 
                  System.exit(1); 
                 } 
            } 
        catch (IOException e){ 
         System.err.println(e.getMessage()); 
         System.exit(1); 
        } 
        finally
        {
         try {
              serverSocket.close(); 
             }
         catch (IOException e)
             { 
              System.err.println("Could not close port: "+port); 
              System.exit(1); 
             } 
        }
   }

    private ServidorTcpD (Socket clientSoc)throws IOException{
        clientSocket = clientSoc;
        in=clientSocket.getInputStream();        
    }
    
 public void run()
   {
    System.out.println ("New Communication Thread Started");
        try { 
            boolean connect=true;
            int countIni=0;
            String trama;
            String idDispositivo="";
            while (connect){ //b & 0xff
                    int r = in.read();
                    if(r!=-1){
                        if("!".equals(Character.toString((char)(byte)r))){                            
                            if(countIni>=1){
                                trama="";
                                countIni=0;
                                sb.delete(0, sb.length()+1);
                                sb.append(Character.toString((char)(byte)r));                                
                            }
                            else{
                                sb.append(Character.toString((char)(byte)r));                                 
                            }
                           countIni++;
                        }
                        else if("*".equals(Character.toString((char)(byte)r))){
                            sb.append(Character.toString((char)(byte)r));
                            trama = sb.toString();
                            //int iniTrama=trama.indexOf("*");
                            //int finTrama=trama.indexOf("!");
                            //System.out.print(trama.substring(1, trama.length()-1)); 
                            System.out.println();
                            
                            
                            String[] tramaSplit=trama.substring(1, trama.length()-1).split(",");
                            System.out.println(tramaSplit);
                            //Si el primera coincidencia del array es C, es un comando.
                            if("C".equals(tramaSplit[0])){
                                //System.out.println("Comando");
                                
                                if(clients.get(tramaSplit[1])!=null){
                                    DataOutputStream it=(DataOutputStream)clients.get(tramaSplit[1]);                           
                                    OutputStreamWriter osw = new OutputStreamWriter(it);
                                    BufferedWriter bw = new BufferedWriter(osw);
                                    bw.write(trama);
                                    int hexi=0xD;
                                    int hex = 0xA;
                                    bw.write((char) hexi);
                                    bw.write((char) hex);
                                    bw.flush();
                                }
                                else{
                                        if(!comandos.containsKey(tramaSplit[1])){
                                            comandos.put(tramaSplit[1], new HashMap<>());
                                        }
                                        comandos.get(tramaSplit[1]).put(tramaSplit[0], trama);
                                        System.out.println(comandos.get(tramaSplit[1]).get(tramaSplit[0]));                                        
                                }
                                connect=false;
                            }
                            else{
                                //Si la primera coincidencia es N001, es para liberar central otro comando.
                                if("N".equals(tramaSplit[0])){
                                    //System.out.println("Comando");

                                    if(clients.get(tramaSplit[1])!=null){
                                        DataOutputStream it=(DataOutputStream)clients.get(tramaSplit[1]);                           
                                        OutputStreamWriter osw = new OutputStreamWriter(it);
                                        BufferedWriter bw = new BufferedWriter(osw);
                                        //System.out.println(tramaSplit.toString());
                                        bw.write(trama); 
                                        int hexi=0xD;
                                        int hex = 0xA;
                                        bw.write((char) hexi);
                                        bw.write((char) hex);
                                        bw.flush();
                                    }
                                    else{
                                        if(!comandos.containsKey(tramaSplit[1])){                                            
                                            comandos.put(tramaSplit[1], new HashMap<>());
                                        }
                                        comandos.get(tramaSplit[1]).put(tramaSplit[0], trama);
                                        System.out.println(comandos.get(tramaSplit[1]).get(tramaSplit[0]));                                                                        
                                    }
                                    connect=false;
                                }
                                else{
                                    if("".equals(idDispositivo)){
                                        idDispositivo=tramaSplit[1];
                                        //System.out.println("Datos");
                                        os = new DataOutputStream(clientSocket.getOutputStream());
                                        clients.put(idDispositivo,os);
                                        //System.out.println(clients);
                                        
                                    }
                                    if(comandos.containsKey(tramaSplit[1])){
                                        System.out.println(trama);
                                        os = new DataOutputStream(clientSocket.getOutputStream());                           
                                        OutputStreamWriter osw = new OutputStreamWriter(os);
                                        BufferedWriter bw = new BufferedWriter(osw);
                                        //System.out.println(tramaSplit.toString());
                                        comandos.get(tramaSplit[1]).forEach((k,v)-> {
                                            try {
                                                bw.write(v);
                                                int hexi=0xD;
                                                int hex =0xA;
                                                bw.write((char) hexi);
                                                bw.write((char) hex);
                                            } catch (IOException ex) {
                                                Logger.getLogger(ServidorTcpD.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        });
                                        //hm.forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
                                        //bw.write(comandos.get(tramaSplit[1]).get(tramaSplit[0])); 
                                        
                                        bw.flush();
                                        comandos.remove(tramaSplit[1]);
                                    }
                                    try {     
                                        DbConnect con=new DbConnect();
                                        conect=con.conecta();
                                        Date date = new Date();
                                        long time = date.getTime();
                                        Timestamp fechaRegistro = new Timestamp(time);
                                        
                                        PreparedStatement prepStmt;   
                                        String str1 = Arrays.toString(tramaSplit);               
                                        //replace starting "[" and ending "]" and ","
                                        str1 = str1.substring(1, str1.length()-1).replaceAll(" ", "");
                                        String dataTest="INSERT INTO test (person_id,date_test,temperatura,humedad,ph,conductividad,trama_datos) values ("
                                                + "'80760766',"
                                                + "?,"
                                                + "?,"
                                                + "?,"
                                                + "?,"
                                                + "?,"
                                                + "?);";
                                        
                                        prepStmt=conect.prepareStatement(dataTest); 
                                        prepStmt.setTimestamp(1,fechaRegistro);
                                        prepStmt.setDouble(2,Double.parseDouble(tramaSplit[4]));
                                        prepStmt.setDouble(3, Double.parseDouble(tramaSplit[5]));
                                        prepStmt.setDouble(4, Double.parseDouble(tramaSplit[6]));
                                        prepStmt.setDouble(5, Double.parseDouble(tramaSplit[7]));
                                        prepStmt.setString(6,str1);
                                        prepStmt.execute();
                                        prepStmt.close();
                                        conect.close();
                                        
                                    } catch (SQLException ex) {
                                        Logger.getLogger(ServidorTcpD.class.getName()).log(Level.SEVERE, null, ex);
                                        System.out.println(ex.getMessage());
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ServidorTcpD.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    System.out.println(tramaSplit);
                                    
                                }
                            }
                            System.out.println(trama);
                            trama="";
                            countIni=0;
                            sb.delete(0, sb.length());
                            connect=false;
                        }
                        else{
                             sb.append(Character.toString((char)(byte)r));
                             
                        }
                       
                        
                        //Character.toString((char)(byte)r);
                        //System.out.print(Character.toString((char)(byte)r));                        
                        //System.out.println(tramaDatos);
                        //System.out.printf("{%x}",(byte)contenido);
                        //OutputStream os = clientSocket.getOutputStream();
                        //DataOutputStream it = (DataOutputStream) clients.get(0);
//                        DataOutputStream msg = new DataOutputStream(clientSocket.getOutputStream());
//                        OutputStreamWriter osw = new OutputStreamWriter(msg);
//                        BufferedWriter bw = new BufferedWriter(osw);
//                        bw.write((byte)r);               
//                        bw.flush();
//                        
                    }
                    
                    
//                         byte contenido= (byte) in.read();
//                    if(contenido==0x30){
//                    System.out.printf("{%x}",contenido);
//                    System.out.println();                    
//                    System.out.printf("{%x}",  (byte)((contenido >>4)& 0x0f)); 
//                    System.out.printf("{%x}", (byte)((contenido)& 0x0f)); 
//                    }
//                        }
                    
                     
                  
            }
            clientSocket.close();
            clients.remove(idDispositivo);
            System.out.println("Cliente desconectado");
        } 
        catch (IOException e) 
        { 
         System.err.println("Problem with Communication Server");
        try {
            //System.exit(1);
            conect.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServidorTcpD.class.getName()).log(Level.SEVERE, null, ex);
        }
        }  
    }
}
