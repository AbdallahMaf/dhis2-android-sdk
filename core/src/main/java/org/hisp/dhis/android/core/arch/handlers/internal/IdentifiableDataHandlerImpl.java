/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.arch.handlers.internal;

import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.DataColumns;
import org.hisp.dhis.android.core.common.DeletableDataObject;
import org.hisp.dhis.android.core.common.IdentifiableColumns;
import org.hisp.dhis.android.core.common.ObjectWithUidInterface;
import org.hisp.dhis.android.core.common.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hisp.dhis.android.core.arch.helpers.CollectionsHelper.isDeleted;

public class IdentifiableDataHandlerImpl<O extends DeletableDataObject & ObjectWithUidInterface>
        extends IdentifiableHandlerImpl<O> {

    public IdentifiableDataHandlerImpl(IdentifiableObjectStore<O> store) {
        super(store);
    }

    @Override
    protected Collection<O> beforeCollectionHandled(Collection<O> os) {
        List<String> storedObjectUids = storedObjectUids(os);
        List<String> syncedObjectUids = syncedObjectUids(storedObjectUids);

        List<O> objectsToStore = new ArrayList<>();
        for (O object : os) {
            if (!storedObjectUids.contains(object.uid()) || syncedObjectUids.contains(object.uid())
                    || isDeleted(object)) {
                objectsToStore.add(object);
            }
        }

        return objectsToStore;
    }

    private List<String> storedObjectUids(Collection<O> os) {
        List<String> objectUids = UidsHelper.getUidsList(os);

        String storedObjectUidsWhereClause = new WhereClauseBuilder()
                .appendInKeyStringValues(IdentifiableColumns.UID, objectUids).build();
        return store.selectUidsWhere(storedObjectUidsWhereClause);
    }

    private List<String> syncedObjectUids(List<String> storedObjectUids) {
        if (!storedObjectUids.isEmpty()) {
            String syncedObjectUidsWhereClause2 = new WhereClauseBuilder()
                    .appendInKeyStringValues(IdentifiableColumns.UID, storedObjectUids)
                    .appendInKeyStringValues(DataColumns.STATE,
                            Arrays.asList(State.SYNCED.name(), State.RELATIONSHIP.name(), State.SYNCED_VIA_SMS.name()))
                    .build();
            return store.selectUidsWhere(syncedObjectUidsWhereClause2);
        }

        return new ArrayList<>();
    }
}