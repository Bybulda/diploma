package org.diploma.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.diploma.model.*;

import java.util.List;

public class NewRequestMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public CustomModel createCustomModel(List<List<Double>> pt, String prof, Boolean pt_enc, String algo, AlternativeRoute rt, InModel model) {
        return new CustomModel(pt, prof, pt_enc, algo, rt, model);

    }

    public AlternativeRoute getAlternativeRoute(Integer paths, Double weight, Double share) {
        return AlternativeRoute.builder()
                .maxPaths(paths)
                .maxShareFactor(share)
                .maxWeightFactor(weight).build();
    }

    public PriorityF getPriority(String condition, Double mult){
        return new PriorityF(condition, mult);
    }

    public Geometry getGeometry(String type, List<List<List<Double>>> coordinates){
        return new Geometry(type, coordinates);
    }

    public Areas getAreas(String type, List<NewFeature> features){
        return new Areas(type, features);
    }

    public NewFeature getFeature(String type, String id, Geometry geometry){
        return new NewFeature(type, id, geometry);
    }

    public InModel getInModel(List<PriorityF> priorityFS, Areas area){
        return new InModel(priorityFS, area);
    }

    public String getJsonModel(CustomModel cm) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cm);
    }
}
