
/*
 * Copyright (C) 2015 Archie L. Cobbs. All rights reserved.
 */

package io.permazen.cli.cmd;

import io.permazen.SessionMode;
import io.permazen.cli.CliSession;
import io.permazen.parse.EnumNameParser;
import io.permazen.parse.Parser;
import io.permazen.util.ParseContext;

import java.util.EnumSet;
import java.util.Map;

public class SetSessionModeCommand extends AbstractCommand {

    public SetSessionModeCommand() {
        super("set-session-mode mode:mode");
    }

    @Override
    public String getHelpSummary() {
        return "Sets the CLI session mode";
    }

    @Override
    public String getHelpDetail() {
        return "Changes the current CLI session mode. Specify PERMAZEN, CORE_API, or KEY_VALUE.";
    }

    @Override
    public EnumSet<SessionMode> getSessionModes() {
        return EnumSet.allOf(SessionMode.class);
    }

    @Override
    protected Parser<?> getParser(String typeName) {
        return "mode".equals(typeName) ? new EnumNameParser<>(SessionMode.class, false) : super.getParser(typeName);
    }

    @Override
    public CliSession.Action getAction(CliSession session0, ParseContext ctx, boolean complete, Map<String, Object> params) {
        final SessionMode mode = (SessionMode)params.get("mode");
        return session -> {
            session.setMode(mode);
            session.getWriter().println("Set session mode to " + mode);
        };
    }
}

