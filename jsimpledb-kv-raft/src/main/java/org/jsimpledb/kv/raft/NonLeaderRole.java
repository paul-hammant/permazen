
/*
 * Copyright (C) 2015 Archie L. Cobbs. All rights reserved.
 */

package org.jsimpledb.kv.raft;

import com.google.common.base.Preconditions;

import org.jsimpledb.kv.raft.msg.AppendResponse;
import org.jsimpledb.kv.raft.msg.CommitRequest;

/**
 * Support superclass for the {@linkplain FollowerRole follower} and {@linkplain CandidateRole candidate} roles,
 * both of which have an election timer.
 */
public abstract class NonLeaderRole extends Role {

    final Timer electionTimer = new Timer(this.raft, "election timer", new Service(this, "election timeout") {
        @Override
        public void run() {
            NonLeaderRole.this.checkElectionTimeout();
        }
    });
    private final boolean startElectionTimer;

// Constructors

    NonLeaderRole(RaftKVDatabase raft, boolean startElectionTimer) {
        super(raft);
        this.startElectionTimer = startElectionTimer;
    }

// Status & Debugging

    /**
     * Get the election timer deadline, if currently running.
     *
     * <p>
     * For a follower that is not a member of its cluster, this will return null because no election timer is running.
     * For all other cases, this will return the time at which the election timer expires.
     *
     * @return current election timer expiration deadline, or null if not running
     */
    public Timestamp getElectionTimeout() {
        synchronized (this.raft) {
            return this.electionTimer.getDeadline();
        }
    }

    /**
     * Force an immediate election timeout.
     *
     * @throws IllegalStateException if this role is no longer active or election timer is not running
     */
    public void startElection() {
        synchronized (this.raft) {
            Preconditions.checkState(this.raft.role == this, "role is no longer active");
            Preconditions.checkState(this.electionTimer.isRunning(), "election timer is not running");
            this.debug("triggering immediate election timeout due to invocation of startElection()");
            this.electionTimer.timeoutNow();
        }
    }

// Lifecycle

    @Override
    void setup() {
        super.setup();
        if (this.startElectionTimer)
            this.restartElectionTimer();
    }

    @Override
    void shutdown() {
        super.shutdown();
        this.electionTimer.cancel();
    }

// Service

    // Check for an election timeout
    private void checkElectionTimeout() {
        if (this.electionTimer.pollForTimeout()) {
            if (this.log.isDebugEnabled())
                this.debug("election timeout while in " + this);
            this.handleElectionTimeout();
        }
    }

    void restartElectionTimer() {

        // Sanity check
        assert Thread.holdsLock(this.raft);

        // Generate a randomized election timeout delay
        final int range = this.raft.maxElectionTimeout - this.raft.minElectionTimeout;
        final int randomizedPart = Math.round(this.raft.random.nextFloat() * range);

        // Restart timer
        this.electionTimer.timeoutAfter(this.raft.minElectionTimeout + randomizedPart);
    }

    abstract void handleElectionTimeout();

// MessageSwitch

    @Override
    void caseAppendResponse(AppendResponse msg) {
        this.failUnexpectedMessage(msg);
    }

    @Override
    void caseCommitRequest(CommitRequest msg) {
        this.failUnexpectedMessage(msg);
    }
}
