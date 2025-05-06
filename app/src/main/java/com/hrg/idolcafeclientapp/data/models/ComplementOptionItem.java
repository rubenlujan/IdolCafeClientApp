package com.hrg.idolcafeclientapp.data.models;

public class ComplementOptionItem extends ComplementListItem {
    private ItemComplement itemComplement;
    public ComplementOptionItem(ItemComplement itemComplement) { this.itemComplement = itemComplement; }
    public ItemComplement getItemComplement() { return itemComplement; }
}
