
/*
 * Copyright (C) 2014 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.jsimpledb.cli.func;

import org.jsimpledb.JObject;
import org.jsimpledb.JTransaction;
import org.jsimpledb.cli.Session;
import org.jsimpledb.cli.parse.expr.EvalException;
import org.jsimpledb.cli.parse.expr.Value;
import org.jsimpledb.core.DeletedObjectException;
import org.jsimpledb.core.ObjId;

@CliFunction
public class UpgradeFunction extends SimpleFunction {

    public UpgradeFunction() {
        super("upgrade", 1, 1);
    }

    @Override
    public String getUsage() {
        return "upgrade(object)";
    }

    @Override
    public String getHelpSummary() {
        return "updates an object's schema version if necessary, returning true if an update occurred";
    }

    @Override
    protected Value apply(Session session, Value[] params) {

        // Get object
        Object obj = params[0].checkNotNull(session, "upgrade()");
        if (obj instanceof JObject)
            obj = ((JObject)obj).getObjId();
        else if (!(obj instanceof ObjId))
            throw new EvalException("invalid upgrade() operation on non-database object of type " + obj.getClass().getName());
        final ObjId id = (ObjId)obj;

        // Upgrade object
        try {
            return new Value(session.hasJSimpleDB() ?
              JTransaction.getCurrent().getJObject(id).upgrade() : session.getTransaction().updateSchemaVersion(id));
        } catch (DeletedObjectException e) {
            throw new EvalException("invalid upgrade() operation on non-existent object " + id);
        }
    }
}

