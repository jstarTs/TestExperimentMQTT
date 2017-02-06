import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.MqttClient;
        import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
        import org.eclipse.paho.client.mqttv3.MqttException;
        import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

        public class MqttPublishSample {

        public static void main(String[] args) throws FileNotFoundException {
        	
        	MqttPublishSample test = new MqttPublishSample();
            
        	String topic        = "MQTT Examples";
            //String topic        = "MQTT vs Coap";
            
            String content      = "Message from MqttPublishSample";
            
            String[] xmlfile = {"testPubSub1.xml" , "testPubSub2.xml" , "testPubSub3.xml" ,"testPubSubAll.xml"};
			String temp = "";
			Scanner sc ;
			List<String> fileList = new ArrayList<String>();
			
			String[] timeArray = {"hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Bg","hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Ba","hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Bf","hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Be"};		
			
			String timeA = "hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Bh";
			
			for(String filename : xmlfile)
			{
				//sc = new Scanner(new File("testData/"+filename));
				sc = new Scanner(new File("E:\\Downloads\\moquette-master\\moquette-master\\broker\\testData\\"+filename));
				
				while(sc.hasNextLine())
				{
					temp += sc.nextLine();
				}
				fileList.add(temp);
				
				temp = "";
			}
            
            int qos             = 0;
            //String broker = "tcp://iot.eclipse.org:1883";
            //String broker = "tcp://localhost:1883";
            String broker = "tcp://140.120.15.159:1883";
            //String broker = "tcp://140.120.15.136:1883";
            
            String clientId     = "JavaSample";
            MemoryPersistence persistence = new MemoryPersistence();

            /*
            try {
                
//            	MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
//                MqttConnectOptions connOpts = new MqttConnectOptions();
//                connOpts.setCleanSession(true);
                //System.out.println("Connecting to broker: "+broker);
                //sampleClient.connect(connOpts);
                //System.out.println("Connected");
                //System.out.println("Publishing message: "+content);
                //System.out.println("Publishing message: "+content);
                //MqttMessage message = new MqttMessage(content.getBytes());
                //MqttMessage message;
                
                
            	
                for(int i=0 ; i<500 ; i++)
                {
                	MqttClient sampleClient = new MqttClient(broker, clientId+i , persistence);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);
                	sampleClient.connect(connOpts); 
                	//message = new MqttMessage(("hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Bg"+","+fileList.get(3)).getBytes());
                	MqttMessage message = new MqttMessage(fileList.get(3).getBytes());
                  
                	message.setQos(qos);
                     sampleClient.publish(topic, message);
                     System.out.println("Message published");
                     
                     sampleClient.disconnect();
                }
                
            } 
            
            catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            }
            */
            
            
//                sampleClient.connect(connOpts); 
//            	MqttMessage message;
//                message = new MqttMessage(("hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Bg"+","+fileList.get(3)).getBytes());
//                 message.setQos(qos);
//                 sampleClient.publish(topic, message);
//                 System.out.println("Message published");
                 //sampleClient.disconnect();
                 
                //message = new MqttMessage(("hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Bg"+","+fileList.get(3)).getBytes());
            	
            /*
                for(int i=0 ; i<500 ; i++)
                {
                	int index = i;
                	new Thread (()->{
                		try 
                		{
							MqttClient sampleClient = new MqttClient(broker, clientId+index, persistence);
                			MqttConnectOptions connOpts = new MqttConnectOptions();
							sampleClient.connect(connOpts);
							connOpts.setCleanSession(true);
							MqttMessage message = new MqttMessage(fileList.get(3).getBytes());;
							message.setQos(qos);
	                        sampleClient.publish(topic, message);
	                        System.out.println("Message published");
	                        System.out.println(index);
	                         
	                         sampleClient.disconnect();
						} catch (MqttSecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
                	}).start();
                	
                }
               	*/
                 
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            
            for(int i=1 ; i<=1000 ; i++)
            {
            	executorService.execute(test.new publish(broker,i , fileList.get(3)));
            }
            executorService.shutdown();
            
                System.out.println("Disconnected");
                //System.exit(0);
                
           
        }
        
        public class publish implements Runnable
        {
        	String broker , document;
        	int index , qos = 0;
        	
        	MemoryPersistence persistence1 = new MemoryPersistence();
        	
        	publish(String Broker,int Index , String doc)
        	{
        		broker = Broker;
        		index = Index;
        		document = doc;
        	}
        	
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try 
        		{
					MqttClient sampleClient = new MqttClient(broker, "JavaSample"+index, persistence1);
        			MqttConnectOptions connOpts = new MqttConnectOptions();
					sampleClient.connect(connOpts);
					connOpts.setCleanSession(true);
					MqttMessage message = new MqttMessage(document.getBytes());;
					message.setQos(qos);
                    sampleClient.publish("MQTT Examples", message);
                    System.out.println("Message published");
                    System.out.println(index);
                     
                     sampleClient.disconnect();
				} catch (MqttSecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
        	
        }
        
    }