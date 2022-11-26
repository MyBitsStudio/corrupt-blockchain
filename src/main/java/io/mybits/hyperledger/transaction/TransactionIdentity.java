package io.mybits.hyperledger.transaction;

import java.io.Serializable;

public enum TransactionIdentity implements Serializable {

    SYSTEM, TRANSFER, FLYING, MINING, RELEASE, PARTY_CREATE, COMPANY_CREATE

}
