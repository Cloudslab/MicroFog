package com.example;

import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "microservice")
public interface MicroserviceControlInfoInterface {

    int nextservices();
    Optional<List<NextServiceData>> nextservice();
    Optional<DirectReply> directreply();
    Optional<String> candidateselection();

    Optional<List<Aggregator>> aggregators();
    Optional<List<Candidates>> candidates();
    List<Level> sync();

    interface NextServiceData {
        String type();
        String url();
        int reqmsgsize();
        int reqprocesstime();

        int repmsgsize();
        int repprocesstime();
        Optional<Integer> weight();
    }

    interface DirectReply{
        int msgsize();
        int processingtime();
    }

    interface Aggregator {
        Optional<List<Integer>> services();
        Optional<List<Integer>> candidateids();
    }

    interface Candidates {
        List<Integer>  services();
    }

    interface Level {
        Optional<List<Integer>> candidateids();
        Optional<Integer> aggregatorid();
        Optional<List<Integer>> services();
    }

}
