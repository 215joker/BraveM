package com.bravem.app.domain.model;

import java.util.ArrayList;
import java.util.List;

public class DegreeMapper {
    public static Degree toDomain(com.bravem.app.model.Degree dataDegree) {
        if (dataDegree == null) return null;
        return new Degree(
            dataDegree.getId(),
            dataDegree.getName(),
            dataDegree.getDescription(),
            dataDegree.getUniversity()
        );
    }

    public static List<Degree> toDomain(List<com.bravem.app.model.Degree> dataDegrees) {
        List<Degree> domainDegrees = new ArrayList<>();
        if (dataDegrees != null) {
            for (com.bravem.app.model.Degree d : dataDegrees) {
                domainDegrees.add(toDomain(d));
            }
        }
        return domainDegrees;
    }
}
