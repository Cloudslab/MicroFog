//package com.microfog.scheduler;
//
//import io.fabric8.kubernetes.api.model.Service;
//import io.fabric8.kubernetes.client.DefaultKubernetesClient;
//import io.fabric8.kubernetes.client.KubernetesClient;
//import io.minio.*;
//import io.minio.errors.*;
//import io.minio.http.Method;
//import io.quarkus.test.junit.QuarkusTest;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.net.URL;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//
//
///**
// * Test deploying kubernetes resources using url for yaml file storein in Minio
// */
//@QuarkusTest
//public class YamlFileAccess {
//    @Test
//    public void testBucketAccess() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        MinioClient minioClient = MinioClient.builder().endpoint("http://127.0.0.1:9000").
//                credentials("MvOGfIGXDUVAINYU", "UBBJiPngiypgI9YQ1pb7QnanyFXouFT8")
//                . build();
//
//       boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("microfog-app-metadata").build());
//
//
//
//       if(found){
////           minioClient.downloadObject(
////                   DownloadObjectArgs.builder().
////                           bucket("microfog-app-metadata")
////                           .object("bookinfogw.yaml")
////                           .filename("bookinfogw.yaml")
////                           .build());
//
////           minioClient.getObject(
////                   GetObjectArgs.builder().
////                           bucket("microfog-app-metadata")
////                           .object("bookinfogw.yaml").build()
////           );
//
//           KubernetesClient kubernetesClient = new DefaultKubernetesClient();
////        File serviceYaml
//           String url = minioClient.getPresignedObjectUrl(
//                   GetPresignedObjectUrlArgs.builder().bucket("microfog-app-metadata")
//                           .object("bookinfo/productpageService.yaml").method(Method.GET).build()
//           );
//           URL url1 = new URL(url);
//           Service service = kubernetesClient.services().load(url1).get();
//           service = kubernetesClient.services().inNamespace("default").create(service);
//       }
//
//    }
//}
