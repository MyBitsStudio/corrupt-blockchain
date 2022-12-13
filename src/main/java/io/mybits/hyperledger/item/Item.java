package io.mybits.hyperledger.item;

import io.mybits.utils.Utilities;

public class Item {

    private final int id, amount;
    private final String hash;

    private boolean validated;

    public Item(int id, int amount){
        this.id = id;
        this.amount = amount;
        this.hash = Utilities.createRandomString(12);
        this.validated = false;
    }

    public Item(int id, int amount, String hash){
        this.id = id;
        this.amount = amount;
        this.hash = hash;
        this.validated = false;
    }

    public void validate(){
        this.validated = true;
    }
}
