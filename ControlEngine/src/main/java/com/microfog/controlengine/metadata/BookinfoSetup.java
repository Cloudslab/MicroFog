package com.microfog.controlengine.metadata;

import com.microfog.controlengine.model.deployment.*;
import com.microfog.controlengine.model.domainObjects.Application;
import com.microfog.controlengine.model.domainObjects.CompositeService;
import com.microfog.controlengine.model.domainObjects.Dataflow;
import com.microfog.controlengine.model.domainObjects.Microservice;
import io.minio.errors.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BookinfoSetup extends ApplicationSetup{


    public BookinfoSetup(Utils utils) {
        super(utils);
    }

    public  Application generateApplicationObject() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String applicationId = "bookinfo";
        /** application model
         *  1. microservices (6)
         *  2. Dataflows
         *  3. composite services (4)
         */
        Microservice productPage = new Microservice("BI_ProductPage", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.3));
                put(Microservice.RAM, utils.getbytesForMib(300));
            }
        });
        Microservice reviewsV1 = new Microservice("BI_ReviewV1", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.4));
                put(Microservice.RAM, utils.getbytesForMib(200));
            }
        });
        Microservice reviewsV2 = new Microservice("BI_ReviewV2", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.4));
                put(Microservice.RAM, utils.getbytesForMib(200));
            }
        });
        Microservice reviewsV3 = new Microservice("BI_ReviewV3", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.4));
                put(Microservice.RAM, utils.getbytesForMib(200));
            }
        });
        Microservice details = new Microservice("BI_Details", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.2));
                put(Microservice.RAM, utils.getbytesForMib(200));
            }
        });
        Microservice ratings = new Microservice("BI_Ratings", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.2));
                put(Microservice.RAM, utils.getbytesForMib(200));
            }
        });
        List<Microservice> microservices = Arrays.asList(new Microservice[]{productPage, reviewsV1, reviewsV2, reviewsV3, details, ratings});

        Dataflow productPage_reviewsV1 = new Dataflow(productPage,reviewsV1,true);
        Dataflow reviewsV1_productPage = new Dataflow(reviewsV1,productPage, false);

        Dataflow productPage_reviewsV2 = new Dataflow(productPage,reviewsV2, true);
        Dataflow reviewV2_ratings = new Dataflow(reviewsV2,ratings,true);
        Dataflow ratings_reviewV2 = new Dataflow(ratings,reviewsV2,false);
        Dataflow reviewsV2_productPage = new Dataflow(reviewsV2,productPage,false);

        Dataflow productPage_reviewsV3 = new Dataflow(productPage,reviewsV3,true);
        Dataflow reviewV3_ratings = new Dataflow(reviewsV3,ratings,true);
        Dataflow ratings_reviewV3 = new Dataflow(ratings,reviewsV3, false);
        Dataflow reviewsV3_productPage = new Dataflow(reviewsV3,productPage,false);

        Dataflow productPage_details = new Dataflow(productPage,details,true);
        Dataflow details_productPage = new Dataflow(details,productPage,false);

        List<Dataflow> dataflows = Arrays.asList(new Dataflow[]{productPage_reviewsV1, reviewsV1_productPage,
                productPage_reviewsV2, reviewV2_ratings, ratings_reviewV2, reviewsV2_productPage,
                productPage_reviewsV3, reviewV3_ratings, ratings_reviewV3, reviewsV3_productPage,
                productPage_details, details_productPage});

        CompositeService reviewV1Service = new CompositeService("BI_Review1_S",Arrays.asList(new Microservice[]{productPage, reviewsV1}),
                Arrays.asList(Arrays.asList(new Dataflow[]{productPage_reviewsV1, reviewsV1_productPage})));
        CompositeService reviewV2Service = new CompositeService("BI_Review2_S", Arrays.asList(new Microservice[]{productPage, reviewsV2, ratings}),
                Arrays.asList(Arrays.asList(new Dataflow[]{productPage_reviewsV2, reviewV2_ratings, ratings_reviewV2, reviewsV2_productPage})));
        CompositeService reviewV3Service = new CompositeService("BI_Review3_S", Arrays.asList(new Microservice[]{productPage, reviewsV3, ratings}),
                Arrays.asList(Arrays.asList(new Dataflow[]{productPage_reviewsV3, reviewV3_ratings, ratings_reviewV3, reviewsV3_productPage})));
        CompositeService detailsService = new CompositeService("BI_Details_S", Arrays.asList(new Microservice[]{productPage,details}),
                Arrays.asList(Arrays.asList(new Dataflow[]{productPage_details, details_productPage})));

        List<CompositeService> compositeServices = Arrays.asList(new CompositeService[]{reviewV1Service, reviewV2Service, reviewV3Service, detailsService});

        Application application = new Application(applicationId);
        application.initModel(microservices, compositeServices, dataflows);

        /**
         * Deployment Related Resources
         */
        List<Command> commands = new ArrayList<>();

        // application level commands
        Map<String,String> nsLabels = new HashMap<>();
        nsLabels.put("istio-injection","enabled");
        Namespace ns = new Namespace(applicationId, "bookinfoapp", nsLabels);
        Command namespace = new Command(CommandTypeEnum.CREATE_NS, CommandLevelEnum.APP_LEVEL, ResourceEnum.NS, ns);
        commands.add(namespace);

        // composite service level commands
        // (clusters hosting microservices that consume other microservices
        // should have Kubernetes Services deployed for consumed microservices, so that the microservice discovery can happen )
        Service productPageS = new Service("productpageService.yaml",ns.getName(), Arrays.asList(new String[]{"BI_ProductPage"}));
        Command productPageSCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, productPageS);
        commands.add(productPageSCommand);

        IstioVirtualService productPageVS = new IstioVirtualService("bookinfovs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"BI_ProductPage"}));
        Command productPageVSCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, productPageVS);
        commands.add(productPageVSCommand);

        Gateway productPageGW = new Gateway("bookinfogw.yaml",ns.getName(), Arrays.asList(new String[]{"BI_ProductPage"}));
        Command productPageGWCommand = new Command(CommandTypeEnum.CREATE_GW, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, productPageGW);
        commands.add(productPageGWCommand);

        /*********************/

        Service reviewsS = new Service("reviewService.yaml",ns.getName(),Arrays.asList(new String[]{"BI_ReviewV1", "BI_ReviewV2", "BI_ReviewV3"}));
        Command reviewsSCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, reviewsS);
        commands.add(reviewsSCommand);

        IstioVirtualService reviewsVS = new IstioVirtualService("reviews_vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"BI_ReviewV1", "BI_ReviewV2", "BI_ReviewV3"}));
        Command reviewsVSCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, reviewsVS);
        commands.add(reviewsVSCommand);

        IstioDestinationRule reviewsDR = new IstioDestinationRule("reviews_dr.yaml",ns.getName(), Arrays.asList(new String[]{"BI_ReviewV1", "BI_ReviewV2", "BI_ReviewV3"}));
        Command reviewsDRCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, reviewsDR);
        commands.add(reviewsDRCommand);

        /*********************/

        Service detailsS = new Service("detailsService.yaml",ns.getName(),Arrays.asList(new String[]{"BI_Details"}));
        Command detailsSCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, detailsS);
        commands.add(detailsSCommand);


        /*********************/

        Service ratingsS = new Service("ratingsService.yaml",ns.getName(),Arrays.asList(new String[]{"BI_Ratings"}));
        Command ratingsSCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, ratingsS);
        commands.add(ratingsSCommand);

        //microservice level (each microservice instance is deployed using Pod definitions)

        Pod productPageM = new Pod("productpage.yaml",ns.getName(),"BI_ProductPage");
        Command productPageMCommand = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, productPageM);
        commands.add(productPageMCommand);

        Pod review1M = new Pod("reviews1.yaml",ns.getName(),"BI_ReviewV1");
        Command review1MCommand = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, review1M);
        commands.add(review1MCommand);

        Pod review2M = new Pod("reviews2.yaml",ns.getName(),"BI_ReviewV2");
        Command review2MCommand = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, review2M);
        commands.add(review2MCommand);

        Pod review3M = new Pod("reviews3.yaml",ns.getName(),"BI_ReviewV3");
        Command review3MCommand = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, review3M);
        commands.add(review3MCommand);

        Pod detailsM = new Pod("details.yaml",ns.getName(),"BI_Details");
        Command detailsMCommand = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, detailsM);
        commands.add(detailsMCommand);

        Pod ratingsM = new Pod("ratings.yaml",ns.getName(),"BI_Ratings");
        Command ratingsMCommand = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, ratingsM);
        commands.add(ratingsMCommand);

        utils.updateYamlUrl(commands, applicationId);
        application.initCommands(commands);
        return application;
    }
}
