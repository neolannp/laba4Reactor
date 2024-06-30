package org.example;

import org.example.handlers.JSONHandler;
import org.example.handlers.XMLHandler;
import org.example.handlers.YAMLHandler;
import org.example.reactor.Reactor;

import java.util.HashMap;
import java.util.Map;

public class Manager {
    private HashMap<String, Reactor> reactors;

    public HashMap<String, Reactor> readCommonClass(String filePath) {
        JSONHandler jsonHandler = new JSONHandler();
        XMLHandler xmlHandler = new XMLHandler();
        YAMLHandler yamlHandler = new YAMLHandler();

        yamlHandler.setNextFileHandler(xmlHandler);
        xmlHandler.setNextFileHandler(jsonHandler);

        HashMap<String, Reactor> reactors = yamlHandler.selectivelyLoadReactors(filePath);
        return reactors;
    }
    public HashMap<String,Double> getReactorTypeMap(HashMap<String, Reactor> reactors){
        HashMap<String, Double> reactorTypeMap = new HashMap<>();
        for (Map.Entry<String, Reactor> entry : reactors.entrySet()) {
            String type=entry.getKey();
            double burnup=entry.getValue().getBurnup();
            reactorTypeMap.put(type,burnup);
        }
        reactorTypeMap.put("LWGR", 25.0);
        reactorTypeMap.put("GCR", 22.0);
        reactorTypeMap.put("HWDCR", 12.0);
        reactorTypeMap.put("HTGR", 100.0);
        reactorTypeMap.put("FBR", 150.0);
        reactorTypeMap.put("SGHWR", 8.0);
        return reactorTypeMap;
    }
    public HashMap<String, Reactor> getReactorMap(){
        return reactors;
    }
}

