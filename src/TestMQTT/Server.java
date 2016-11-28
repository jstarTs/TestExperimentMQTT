package TestMQTT;
import io.moquette.BrokerConstants;
import io.moquette.interception.HazelcastInterceptHandler;
import io.moquette.interception.HazelcastMsg;
import io.moquette.interception.InterceptHandler;
import io.moquette.parser.proto.messages.PublishMessage;
import io.moquette.server.config.MemoryConfig;
//import io.moquette.spi.impl.ProtocolProcessorBootstrapper;
import io.moquette.server.config.FilesystemConfig;
import io.moquette.server.config.IConfig;
//import io.moquette.server.netty.NettyAcceptor;
//import io.moquette.spi.impl.ProtocolProcessor;
import io.moquette.spi.impl.subscriptions.Subscription;
import io.moquette.spi.security.IAuthenticator;
import io.moquette.spi.security.IAuthorizator;
import io.moquette.spi.security.ISslContextCreator;

import io.moquette.server.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.ITopic;

/**
 * Launch a  configured version of the server.
 * @author andrea
 */
public class Server extends io.moquette.server.Server{
    
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private static final String HZ_INTERCEPT_HANDLER = HazelcastInterceptHandler.class.getCanonicalName();
    
    private TestMQTT.ServerAcceptor m_acceptor;

    private volatile boolean m_initialized;

    private TestMQTT.ProtocolProcessor m_processor;

    private HazelcastInstance hazelcastInstance;

    private TestMQTT.ProtocolProcessorBootstrapper m_processorBootstrapper;

    public static void main(String[] args) throws IOException {
        final Server server = new Server();
        
        //server.startServer();
        final IConfig config = new FilesystemConfig();
        server.startServer(config, null, null, null, null ,Integer.parseInt(args[0]) );
        
        System.out.println("Server started, version 0.9-SNAPSHOT");
        //Bind  a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stopServer();
            }
        });
    }
  
    public void startServer(IConfig config, List<? extends InterceptHandler> handlers,
                            ISslContextCreator sslCtxCreator, IAuthenticator authenticator,
                            IAuthorizator authorizator , int MeterNum) throws IOException {
        if (handlers == null) {
            handlers = Collections.emptyList();
        }

        final String handlerProp = System.getProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME);
        if (handlerProp != null) {
            config.setProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME, handlerProp);
        }
        configureCluster(config);
        LOG.info("Persistent store file: {}", config.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME));
        m_processorBootstrapper = new TestMQTT.ProtocolProcessorBootstrapper();
        final TestMQTT.ProtocolProcessor processor = m_processorBootstrapper.init(config, handlers, authenticator, authorizator, this);

        if (sslCtxCreator == null) {
            sslCtxCreator = new DefaultMoquetteSslContextCreator(config);
        }

        m_acceptor = new NettyAcceptor();
        m_acceptor.initialize(processor, config, sslCtxCreator);
        m_processor = processor;
        m_initialized = true;
        
        FilterManagement fm = new FilterManagement();
        m_processor.setFilterManager(fm);
        fm.setSensorNum(MeterNum);
        try {
			fm.selectData();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void configureCluster(IConfig config) throws FileNotFoundException {
        String interceptHandlerClassname = config.getProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME);
        if (interceptHandlerClassname == null || !HZ_INTERCEPT_HANDLER.equals(interceptHandlerClassname)) {
            return;
        }
        String hzConfigPath = config.getProperty(BrokerConstants.HAZELCAST_CONFIGURATION);
        if (hzConfigPath != null) {
            boolean isHzConfigOnClasspath = this.getClass().getClassLoader().getResource(hzConfigPath) != null;
            Config hzconfig = isHzConfigOnClasspath ?
                    new ClasspathXmlConfig(hzConfigPath) :
                    new FileSystemXmlConfig(hzConfigPath);
            LOG.info(String.format("starting server with hazelcast configuration file : %s",hzconfig));
            hazelcastInstance = Hazelcast.newHazelcastInstance(hzconfig);
        } else {
            LOG.info("starting server with hazelcast default file");
            hazelcastInstance = Hazelcast.newHazelcastInstance();
        }
        listenOnHazelCastMsg();
    }
    
    private void listenOnHazelCastMsg() {
        HazelcastInstance hz = getHazelcastInstance();
        ITopic<HazelcastMsg> topic = hz.getTopic("moquette");
        topic.addMessageListener(new HazelcastListener(this));
    }

    public HazelcastInstance getHazelcastInstance(){
        return hazelcastInstance;
    }

    /**
     * Use the broker to publish a message. It's intended for embedding applications.
     * It can be used only after the server is correctly started with startServer.
     *
     * @param msg the message to forward.
     * @throws IllegalStateException if the server is not yet started
     * */
    public void internalPublish(PublishMessage msg) {
        if (!m_initialized) {
            throw new IllegalStateException("Can't publish on a server is not yet started");
        }
        m_processor.internalPublish(msg);
    }
    
    public void stopServer() {
    	LOG.info("Server stopping...");
        m_acceptor.close();
        m_processorBootstrapper.shutdown();
        m_initialized = false;
        if (hazelcastInstance != null) {
            try {
                hazelcastInstance.shutdown();
            } catch (HazelcastInstanceNotActiveException e) {
                LOG.info("hazelcast already shutdown");
            }
        }
        LOG.info("Server stopped");
    }

    /**
     * SPI method used by Broker embedded applications to get list of subscribers.
     * Returns null if the broker is not started.
     */
    public List<Subscription> getSubscriptions() {
        if (m_processorBootstrapper == null) {
            return null;
        }
        return m_processorBootstrapper.getSubscriptions();
    }

    /**
     * SPI method used by Broker embedded applications to add intercept handlers.
     * */
    public boolean addInterceptHandler(InterceptHandler interceptHandler) {
        if (!m_initialized) {
            throw new IllegalStateException("Can't register interceptors on a server that is not yet started");
        }
        return m_processor.addInterceptHandler(interceptHandler);
    }

    /**
     * SPI method used by Broker embedded applications to remove intercept handlers.
     * */
    public boolean removeInterceptHandler(InterceptHandler interceptHandler) {
        if (!m_initialized) {
            throw new IllegalStateException("Can't deregister interceptors from a server that is not yet started");
        }
        return m_processor.removeInterceptHandler(interceptHandler);
    }
}
