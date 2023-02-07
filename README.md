# MicroFog : A Framework for Scalable Placement of Microservices-based IoT Applications in Federated Fog Environments

###MicroFog is a scalable and extensible framework that enables Microservices-based IoT Applications placement within multi-fog multi-cloud environments using Kubernetes (as container orchestrator) and Istio (as Service Mesh)

##Features : 
1. Multi-fog Multi-cloud microservice placement and deployment
2. Seamless microservice composition across hybrid environments through automated deployment of  Kubernetes and Istio resources to ensure cross-cluster service discovery and load balancing. 
3. Ability to integrate novel placement algorithms and load balancing policies easily. 
4. Support for heterogeneous cloud-native application deployment without any application-level changes. 
5. Compatibility with cutting-edge cloud-native technologies. 
6. A configurable control engine to support different approaches for application placements
   1. operation modes like centralised or distributed operation
   2. application placement modes such as event-driven or periodic placement request processing and batch or sequential placement request processing
7. Distributed storage solutions to store the data required for application placement and deployment 
8. Rapid prototyping support 
9. Flexible and scalable for distributed deployment across fog and cloud clusters.

##Main Components of the framework:
1. MicrFog-CE : Control Engine of the framework, responsible for executing placement algorithms and deploying the microservices-based application using Kubernetes and Istio resources
2. Data Stores : Contains data required for application placement. Consists of 3 main data sores
   1. MinIO YAML Store
   2. Redis Meta Data Store
   3. Docker Image Registry
3. Monitoring components
4. Logging component

##This repository contains three resources for running and configuring MicroFog Framework. 

1. ControlEngine : Contains the code for the MicroFog-Control Engine developed as a microservice. Can be extended adn containerised to add novel placement policies, load balancing policies, etc.
2. Prototype_Setup : Contains resources required for deploying MicroFog framework (Example setup containes resources for setting up two Fog clusters and One Cloud cluster)
   1. cluster_setup : Resources required for Fog-Cloud infrastructure setup as Kubernetes and Istio installed clusters
   2. redis_setup : Resources required for setting up Redis Meta Data Store
   3. minio_setup : Resources required for setting up
   4. kiali_setup : Resources required for setting Kiali for microservice observerbility
   5. grafana-loki-setup : Resources required for configuring Loki for log aggregation and Grafana for visualisation of logs
   6. docs : Contains guidelines explaining each setup
3. Workload_Generator : A tool for creating dummy microservices-based application workloads for evaluating novel placement polcies.
   1. dummy-micro-java : A template microservice. It's a configurable microservice which can be as the buidling block for heterogeneous applications with diverse interaction patterns among microservices.
   2. sample-deployment-resources : We provide 5 example applications to demonstrate how to use the workload generator
   3. docs : Explains the design, architecture of the workload generator.


