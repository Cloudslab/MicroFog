# MicroFog : A Framework for Scalable Placement of Microservices-based IoT Applications in Federated Fog Environments

### MicroFog is a scalable and extensible framework that enables Microservices-based IoT Applications placement within multi-fog multi-cloud environments using Kubernetes (as container orchestrator) and Istio (as Service Mesh)

## Features : 
* Multi-fog Multi-cloud microservice placement and deployment
* Seamless microservice composition across hybrid environments through automated deployment of  Kubernetes and Istio resources to ensure cross-cluster service discovery and load balancing. 
* Ability to integrate novel placement algorithms and load balancing policies easily. 
* Support for heterogeneous cloud-native application deployment without any application-level changes. 
* Compatibility with cutting-edge cloud-native technologies. 
* A configurable control engine to support different approaches for application placements
   1. operation modes like centralised or distributed operation
   2. application placement modes such as event-driven or periodic placement request processing and batch or sequential placement request processing
* Distributed storage solutions to store the data required for application placement and deployment 
* Rapid prototyping support 
* Flexible and scalable for distributed deployment across fog and cloud clusters.



<figure class="image">
  <img src="https://user-images.githubusercontent.com/47441406/219677403-e262baa0-3ffd-4b8e-8eaa-3b925116b306.gif" width="500" height="350"/>
  <figcaption>Example Application Placement Using MicroFog Framework</figcaption>
</figure>

## Main Components of the framework:
1. MicrFog-CE : Control Engine of the framework, responsible for executing placement algorithms and deploying the microservices-based application using Kubernetes and Istio resources
2. Data Stores : Contains data required for application placement. Consists of 3 main data sores
   1. MinIO YAML Store
   2. Redis Meta Data Store
   3. Docker Image Registry
3. Monitoring components (Prometheus, Grafana, Kiali)
4. Logging component (Grafana Loki)

## Technologies used:
<img src="https://user-images.githubusercontent.com/47441406/219674401-acecad3d-fc9a-4fd4-b05d-ca5908c7e4ba.png" alt="" width="50" height="50"> <img src="https://user-images.githubusercontent.com/47441406/219667349-c1e94b04-317b-4271-afae-54da318845aa.png" alt="" width="50" height="50"> <img src="https://user-images.githubusercontent.com/47441406/219667516-6dd970c7-6b4d-4c54-a548-835a80dc4aaf.png" alt="" width="50" height="50"> &ensp;  <img src="https://user-images.githubusercontent.com/47441406/219671894-57a4b7e5-b021-40d4-a836-4502e4d2da42.png" alt="" width="70" height="50"> &ensp;  <img src="https://user-images.githubusercontent.com/47441406/219674566-4e37b79e-8447-4b32-a52b-f11e76cacdda.png" alt="" width="50" height="50">  <img src="https://user-images.githubusercontent.com/47441406/219674696-e23dec91-e7d4-4e9b-9c66-b1392c0540e3.png" alt="" width="50" height="50"> &ensp;  <img src="https://user-images.githubusercontent.com/47441406/219675269-61c7e264-fcfe-4cf0-90ed-f9a0fbdbc579.png" alt="" width="50" height="50">  &ensp;  <img src="https://user-images.githubusercontent.com/47441406/219675343-57b2ef83-cb20-47a9-b6dc-b7fba0eb653a.png" alt="" width="50" height="50"> &ensp;  <img src="https://user-images.githubusercontent.com/47441406/219675405-ba828bfa-308a-4e72-8709-bb65345257eb.png" alt="" width="50" height="50"> &ensp;  <img src="https://user-images.githubusercontent.com/47441406/219675724-8bc0073d-67c3-4eb6-9c55-682a301cec4e.png" alt="" width="50" height="50">


## This repository contains three resources for running and configuring MicroFog Framework. 

1. [ControlEngine](https://github.com/Cloudslab/MicroFog/tree/main/ControlEngine) : Contains the code for the MicroFog-Control Engine developed as a microservice. Can be extended adn containerised to add novel placement policies, load balancing policies, etc.
2. [Prototype_Setup](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup) : Contains resources required for deploying MicroFog framework (Example setup containes resources for setting up two Fog clusters and One Cloud cluster)
   1. [cluster_setup](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup/cluster-setup) : Resources required for Fog-Cloud infrastructure setup as Kubernetes and Istio installed clusters
   2. [redis_setup](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup/redis-setup) : Resources required for setting up Redis Meta Data Store
   3. [minio_setup](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup/minio-setup) : Resources required for setting up MinIO YAML Store
   4. [control-engine-setup](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup/control-engine-setup) : Resources required for setting up MicroFog-CE
   5. [kiali_setup](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup/kiali-setup) : Resources required for setting Kiali for microservice observerbility
   6. [grafana-loki-setup](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup/grafana-loki-setup) : Resources required for configuring Loki for log aggregation and Grafana for visualisation of logs
   7. [docs](https://github.com/Cloudslab/MicroFog/tree/main/Prototype_Setup/docs) : Contains guidelines explaining each setup
3. [Workload_Generator](https://github.com/Cloudslab/MicroFog/tree/main/Workload_Generator) : A tool for creating dummy microservices-based application workloads for evaluating novel placement polcies.
   1. [dummy-micro-java](https://github.com/Cloudslab/MicroFog/tree/main/Workload_Generator/dummy-micro-java) : A template microservice using JAVA. It's a configurable microservice which can be as the buidling block for heterogeneous applications with diverse interaction patterns among microservices.
   2. [sample-deployment-resources](https://github.com/Cloudslab/MicroFog/tree/main/Workload_Generator/sample-deployment-resources) : We provide 5 example applications to demonstrate how to use the workload generator
   3. [docs](https://github.com/Cloudslab/MicroFog/tree/main/Workload_Generator/docs) : Explains the design, architecture of the workload generator.
   
## References :
 * Samodha Pallewatta, Vassilis Kostakos, and Rajkumar Buyya, [”MicroFog: AFramework for Scalable Placement of Microservices-based IoT Applications in Federated Fog Environments”](https://arxiv.org/abs/2302.06971)


