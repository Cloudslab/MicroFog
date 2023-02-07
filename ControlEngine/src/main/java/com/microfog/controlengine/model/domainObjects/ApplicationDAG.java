package com.microfog.controlengine.model.domainObjects;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAG implements Serializable {
    DirectedGraph<Microservice, DefaultEdge> applicationGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public void addVertices(List<Microservice> microserviceList){
        for(Microservice m:microserviceList){
            applicationGraph.addVertex(m);
        }
    }

    public void addEdges(List<Pair<Microservice,Microservice>> edges){
        for(Pair<Microservice,Microservice> e: edges){
            applicationGraph.addEdge(e.getFirst(),e.getSecond());
        }
    }

    public List<Microservice> getConsumedMicroservices(Microservice mConsumer){
        List<Microservice> consumedMList = new ArrayList<>();
        for(DefaultEdge edge: applicationGraph.outgoingEdgesOf(mConsumer)){
            consumedMList.add(applicationGraph.getEdgeTarget(edge));
        }
        return consumedMList;
    }

    public List<Microservice> getConsumerMicroservices(Microservice mConsumed){
        List<Microservice> consumedByMList = new ArrayList<>();
        for(DefaultEdge edge: applicationGraph.incomingEdgesOf(mConsumed)){
            consumedByMList.add(applicationGraph.getEdgeSource(edge));
        }
        return consumedByMList;
    }

    public List<Microservice> getRootMicroservices(){
        List<Microservice> roots = new ArrayList<>();
        for(Microservice m:applicationGraph.vertexSet()){
            if(applicationGraph.incomingEdgesOf(m).isEmpty()){
                roots.add(m);
            }
        }
        return roots;
    }


}
