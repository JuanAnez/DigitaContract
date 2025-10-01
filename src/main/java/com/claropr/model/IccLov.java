package com.claropr.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IccLov {
    private String lovKey;
    private String lovDescription;

    public IccLov() {}

    public IccLov(String lovKey, String lovDescription) {
        this.lovKey = lovKey;
        this.lovDescription = lovDescription;
    }
}

