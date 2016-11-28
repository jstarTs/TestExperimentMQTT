import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttClient;
        import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
        import org.eclipse.paho.client.mqttv3.MqttException;
        import org.eclipse.paho.client.mqttv3.MqttMessage;
        import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

        public class MqttPublishSample {

        public static void main(String[] args) throws FileNotFoundException {

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
            
            int qos             = 2;
            //String broker = "tcp://iot.eclipse.org:1883";
            //String broker = "tcp://localhost:1883";
            String broker = "tcp://140.120.15.136:1883";
            String clientId     = "JavaSample";
            MemoryPersistence persistence = new MemoryPersistence();

            try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                System.out.println("Connecting to broker: "+broker);
                //sampleClient.connect(connOpts);
                System.out.println("Connected");
                //System.out.println("Publishing message: "+content);
                System.out.println("Publishing message: "+content);
                //MqttMessage message = new MqttMessage(content.getBytes());
                MqttMessage message;
                for(int i=0 ; i<10 ; i++)
                {
                	sampleClient.connect(connOpts); 
                	message = new MqttMessage(("hnClcI14k/DCCLPkEfwUnPD/V+FoGLR05+ZoYx6t5Bg"+","+fileList.get(3)).getBytes());
                     message.setQos(qos);
                     sampleClient.publish(topic, message);
                     System.out.println("Message published");
                     
                     sampleClient.disconnect();
                }
                
               
                System.out.println("Disconnected");
                System.exit(0);
                
            } catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            }
        }
    }