
/*
 * Copyright (C) 2014 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.jsimpledb.cli.func;

import java.util.NavigableMap;
import java.util.NavigableSet;

import org.jsimpledb.JTransaction;
import org.jsimpledb.cli.Session;
import org.jsimpledb.cli.parse.IndexedFieldParser;
import org.jsimpledb.cli.parse.ParseException;
import org.jsimpledb.cli.parse.SpaceParser;
import org.jsimpledb.cli.parse.expr.Value;
import org.jsimpledb.core.Field;
import org.jsimpledb.core.FieldSwitchAdapter;
import org.jsimpledb.core.ListField;
import org.jsimpledb.core.ListIndexEntry;
import org.jsimpledb.core.MapField;
import org.jsimpledb.core.MapKeyIndexEntry;
import org.jsimpledb.core.MapValueIndexEntry;
import org.jsimpledb.core.Transaction;
import org.jsimpledb.util.ParseContext;

@CliFunction
public class QueryEntriesFunction extends Function {

    private final SpaceParser spaceParser = new SpaceParser();

    public QueryEntriesFunction() {
        super("queryEntries");
    }

    @Override
    public String getHelpSummary() {
        return "queries an indexed list or map field's element, key, or value index entries";
    }

    @Override
    public String getUsage() {
        return "queryEntries(type.field.subfield)";
    }

    @Override
    public String getHelpDetail() {
        return "Queries an indexed list or map field and returns a mapping from list element or map key or value to"
          + " the set of ListIndexEntry objects (for list fields) or MapKeyIndexEntry or MapValueIndexEntry objects"
          + " (for map fields) corresponding to objects having that value in the field.";
    }

    @Override
    public Object parseParams(final Session session, final ParseContext ctx, final boolean complete) {

        // Parse indexed field parameter
        if (ctx.tryLiteral(")"))
            throw new ParseException(ctx, "indexed field parameter required");
        final IndexedFieldParser.Result result = new IndexedFieldParser().parse(session, ctx, complete);

        // Check field type
        final Query query = result.getParentField() != null ? result.getParentField().visit(new FieldSwitchAdapter<Query>() {
            @Override
            public <E> Query caseListField(ListField<E> field) {
                return new Query(field.getStorageId(), SubField.LIST_ELEMENT);
            }
            @Override
            public <K, V> Query caseMapField(MapField<K, V> field) {
                return new Query(field.getStorageId(),
                  result.getField().getStorageId() == field.getKeyField().getStorageId() ? SubField.MAP_KEY : SubField.MAP_VALUE);
            }
            @Override
            protected <T> Query caseField(Field<T> field) {
                return null;
            }
        }) : null;
        if (query == null) {
            throw new ParseException(ctx, "indexed field `" + result.getField().getName()
              + "' is not a sub-field of a list or map field");
        }

        // Finish parse
        ctx.skipWhitespace();
        if (!ctx.tryLiteral(")"))
            throw new ParseException(ctx, "expected `)'").addCompletion(") ");

        // Done
        return query;
    }

    @Override
    public Value apply(Session session, Object params) {
        final Query query = (Query)params;
        return new Value(null) {
            @Override
            public Object get(Session session) {
                return query.query(session);
            }
        };
    }

// Query

    private static class Query {

        private final int storageId;
        private final SubField subField;

        Query(int storageId, SubField subField) {
            this.storageId = storageId;
            this.subField = subField;
        }

        public NavigableMap<?, ? extends NavigableSet<?>> query(Session session) {
            return session.hasJSimpleDB() ?
              this.subField.query(JTransaction.getCurrent(), this.storageId) :
              this.subField.query(session.getTransaction(), this.storageId);
        }
    }

// Sub-fields with index entries

    private enum SubField {
        LIST_ELEMENT() {

            @Override
            NavigableMap<?, NavigableSet<ListIndexEntry>> query(Transaction tx, int storageId) {
                return tx.queryListFieldEntries(storageId);
            }

            @Override
            NavigableMap<?, NavigableSet<org.jsimpledb.ListIndexEntry<?>>> query(JTransaction jtx, int storageId) {
                return jtx.queryListFieldEntries(storageId);
            }
        },
        MAP_KEY() {

            @Override
            NavigableMap<?, NavigableSet<MapKeyIndexEntry<?>>> query(Transaction tx, int storageId) {
                return tx.queryMapFieldKeyEntries(storageId);
            }

            @Override
            NavigableMap<?, NavigableSet<org.jsimpledb.MapKeyIndexEntry<?, ?>>> query(JTransaction jtx, int storageId) {
                return jtx.queryMapFieldKeyEntries(storageId);
            }
        },
        MAP_VALUE() {

            @Override
            NavigableMap<?, NavigableSet<MapValueIndexEntry<?>>> query(Transaction tx, int storageId) {
                return tx.queryMapFieldValueEntries(storageId);
            }

            @Override
            NavigableMap<?, NavigableSet<org.jsimpledb.MapValueIndexEntry<?, ?>>> query(JTransaction jtx, int storageId) {
                return jtx.queryMapFieldValueEntries(storageId);
            }
        };

        // Core API
        abstract NavigableMap<?, ? extends NavigableSet<?>> query(Transaction tx, int storageId);

        // JSimpleDB API
        abstract NavigableMap<?, ? extends NavigableSet<?>> query(JTransaction jtx, int storageId);
    }
}
