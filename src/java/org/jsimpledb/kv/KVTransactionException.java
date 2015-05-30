
/*
 * Copyright (C) 2014 Archie L. Cobbs. All rights reserved.
 */

package org.jsimpledb.kv;

/**
 * Thrown when an operation on a {@link KVTransaction} fails.
 */
@SuppressWarnings("serial")
public class KVTransactionException extends KVDatabaseException {

    private final KVTransaction kvt;

    public KVTransactionException(KVTransaction kvt) {
        super(kvt.getKVDatabase());
        this.kvt = kvt;
    }

    public KVTransactionException(KVTransaction kvt, Throwable cause) {
        super(kvt.getKVDatabase(), cause);
        this.kvt = kvt;
    }

    public KVTransactionException(KVTransaction kvt, String message) {
        super(kvt.getKVDatabase(), message);
        this.kvt = kvt;
    }

    public KVTransactionException(KVTransaction kvt, String message, Throwable cause) {
        super(kvt.getKVDatabase(), message, cause);
        this.kvt = kvt;
    }

    /**
     * Get the {@link KVTransaction} that generated this exception.
     *
     * @return the associated transaction
     */
    public KVTransaction getTransaction() {
        return this.kvt;
    }
}

