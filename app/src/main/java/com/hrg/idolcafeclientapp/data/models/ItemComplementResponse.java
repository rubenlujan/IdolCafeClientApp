package com.hrg.idolcafeclientapp.data.models;

import java.util.ArrayList;
import java.util.List;

public class ItemComplementResponse {
    private List<ItemComplement> Complements;
    public ItemComplementResponse() {
        Complements = new ArrayList<>();
    }
    public List<ItemComplement> getComplements() {
        return Complements;
    }

    public void setComplements(List<ItemComplement> complements) {
        Complements = complements;
    }
}
