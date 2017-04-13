package chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import chat.dbside.ini.InitialLoader;
import chat.webside.imagesstorage.ImageStoreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application extends SpringBootServletInitializer {

//    private static InitialLoader initiator;
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

//    @Autowired
//    public void setInitialLoader(InitialLoader initiator) {
//        Application.initiator = initiator;
//    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
//SessionFactoryImpl
        try {
            ImageStoreService.login();
        } catch (Exception ex) {
            logger.debug("Error create ImageStoreService", ex);
        }
//        int port = 8888;
//        SignalHandler signalHandler = new SignalHandler(port);
//        signalHandler.start();
//        System.out.println("Server listen on :" + port);
     //   initiator.initial();
        logger.info("successfully start");
    }

}
