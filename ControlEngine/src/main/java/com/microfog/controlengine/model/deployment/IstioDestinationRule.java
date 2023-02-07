package com.microfog.controlengine.model.deployment;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IstioDestinationRule extends ServiceLevelResource {
    String namespace;

    List<IstioSubset> subsets = new ArrayList<>();

    public IstioDestinationRule(String yamlPath, String namespace, List<String> microserviceIdentifier){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void addSubset(String subsetName, Map<String,String> label){
        IstioSubset subset = new IstioSubset(subsetName, label);
        subsets.add(subset);
    }

    public List<IstioSubset> getSubsets() {
        return subsets;
    }

    public class IstioSubset {
        String subsetName;
        Map<String,String> labels;

        public IstioSubset(String subsetName, Map<String,String> labels){
            this.subsetName = subsetName;
            this.labels = labels;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public String getSubsetName() {
            return subsetName;
        }
    }

}



