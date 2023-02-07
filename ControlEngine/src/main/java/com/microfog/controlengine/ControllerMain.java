package com.microfog.controlengine;

import com.microfog.controlengine.metadata.MeteDataGenerator;
import com.microfog.controlengine.controllers.PeriodicSchedular;
import com.microfog.controlengine.controllers.PlacementLogicExecutor;
import com.microfog.controlengine.model.domainObjects.ForwardRequestQueue;
import io.minio.errors.*;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static jodd.util.ThreadUtil.sleep;


@QuarkusMain
public class ControllerMain {

    public static void main(String ... args) {
        System.out.println("Running main method");
        Quarkus.run(SchedularApp.class,args);
    }

    public static class SchedularApp implements QuarkusApplication {

        @ConfigProperty(name = "controlengine.placementmode.periodic")
        Boolean periodicPlacement;

        @ConfigProperty(name = "controlengine.period")
        long period;  //in milliseconds

        @ConfigProperty(name = "controlengine.populatemetadata")
        boolean populateMetadata;

        @ConfigProperty(name = "controlengine.operationmode.primary")
        Boolean isPrimary;

        @ConfigProperty(name = "controlengine.initDelay")
        long initdelay;

        @Inject
        PlacementLogicExecutor placementLogicExecutor;

        @Inject
        PeriodicSchedular periodiceSchedular;

        @Inject
        MeteDataGenerator meteDataGenerator;

        @Inject
        ForwardRequestQueue forwardRequestQueue;


        @Override
        public int run(String... args) throws Exception {
            System.out.println("Startup Logic");

            //initializer tasks
            /** Setting up datastore with basic metadata data.
             */
            loadMetaData();

            //create periodic task for placement request processing based on the placement mode
            if(isPrimary && periodicPlacement){
//                periodiceSchedular.submitLogicExecuter(placementLogicExecutor);
//                Timer time = new Timer();
//                time.schedule(periodiceSchedular,0,period); // time in milliseconds
                ScheduledExecutorService scheduler
                        = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleAtFixedRate(placementLogicExecutor,initdelay,period, TimeUnit.MILLISECONDS);

            }
            else if(isPrimary){
                new Thread(
                        ()->{
                            sleep(initdelay);
                            while(true){
                                placementLogicExecutor.executePlacement();
                            }
                        }
                ).start();
            }

            if(isPrimary) {
                new Thread(
                        () -> {
                            while (true) {
                                forwardRequestQueue.forwardRequest();
                            }
                        }
                ).start();
            }

            Quarkus.waitForExit();
            return 0;
        }

        //todo
        private void loadMetaData() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
            if(populateMetadata){
                meteDataGenerator.generetaMetaData();
            }
        }


    }
}
