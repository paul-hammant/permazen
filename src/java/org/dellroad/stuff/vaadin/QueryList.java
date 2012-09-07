
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin;

/**
 * Cacheable list of Java objects backing an {@link AbstractQueryContainer}.
 * Instances contain a list of Java objects (or some portion thereof), and may become invalid over time.
 *
 * @see AbstractQueryContainer
 */
public interface QueryList<T> {

    /**
     * Get the size of this list.
     */
    int size();

    /**
     * Get an item in the list, or throw an exception if this instance is no longer valid.
     *
     * @param index index of the item (zero-based)
     * @throws IndexOutOfBoundsException if {@code index} is less than zero or greater than {@link #size}
     * @throws InvalidQueryListException if this list has become invalid or cannot provide the item
     */
    T get(int index) throws InvalidQueryListException;
}

