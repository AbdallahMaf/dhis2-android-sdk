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

package org.hisp.dhis.android.core.trackedentity.internal;

import org.hisp.dhis.android.core.arch.db.querybuilders.internal.SQLStatementBuilderImpl;
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.db.statementwrapper.internal.SQLStatementWrapper;
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.StatementBinder;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableDataObjectStoreImpl;
import org.hisp.dhis.android.core.common.BaseDataModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo;

import java.util.Arrays;
import java.util.List;

import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

public final class TrackedEntityInstanceStoreImpl extends IdentifiableDataObjectStoreImpl<TrackedEntityInstance>
        implements TrackedEntityInstanceStore {

    private static final StatementBinder<TrackedEntityInstance> BINDER = (o, sqLiteStatement) -> {
        sqLiteBind(sqLiteStatement, 1, o.uid());
        sqLiteBind(sqLiteStatement, 2, o.created());
        sqLiteBind(sqLiteStatement, 3, o.lastUpdated());
        sqLiteBind(sqLiteStatement, 4, o.createdAtClient());
        sqLiteBind(sqLiteStatement, 5, o.lastUpdatedAtClient());
        sqLiteBind(sqLiteStatement, 6, o.organisationUnit());
        sqLiteBind(sqLiteStatement, 7, o.trackedEntityType());
        sqLiteBind(sqLiteStatement, 8, o.geometry() == null ? null : o.geometry().type());
        sqLiteBind(sqLiteStatement, 9, o.geometry() == null ? null : o.geometry().coordinates());
        sqLiteBind(sqLiteStatement, 10, o.state());
        sqLiteBind(sqLiteStatement, 11, o.deleted());
    };

    public TrackedEntityInstanceStoreImpl(DatabaseAdapter databaseAdapter,
                                          SQLStatementWrapper statementWrapper,
                                          SQLStatementBuilderImpl builder) {
        super(databaseAdapter, statementWrapper, builder, BINDER, TrackedEntityInstance::create);
    }

    @Override
    public List<TrackedEntityInstance> queryTrackedEntityInstancesToSync() {
        String whereToSyncClause = new WhereClauseBuilder()
                .appendInKeyStringValues(BaseDataModel.Columns.STATE, Arrays.asList(
                        State.TO_POST.name(),
                        State.TO_UPDATE.name()))
                .build();

        return selectWhere(whereToSyncClause);
    }

    @Override
    public List<TrackedEntityInstance> queryTrackedEntityInstancesToPost() {
        String whereToPostClause = new WhereClauseBuilder()
                .appendKeyStringValue(BaseDataModel.Columns.STATE, State.TO_POST.name())
                .build();

        return selectWhere(whereToPostClause);
    }

    @Override
    public List<String> querySyncedTrackedEntityInstanceUids() {
        String whereSyncedClause = new WhereClauseBuilder()
                .appendKeyStringValue(BaseDataModel.Columns.STATE, State.SYNCED)
                .build();

        return selectUidsWhere(whereSyncedClause);
    }

    @Override
    public List<String> queryMissingRelationshipsUids() {
        String whereRelationshipsClause = new WhereClauseBuilder()
                .appendKeyStringValue(BaseDataModel.Columns.STATE, State.RELATIONSHIP)
                .appendIsNullValue(TrackedEntityInstanceTableInfo.Columns.ORGANISATION_UNIT)
                .build();

        return selectUidsWhere(whereRelationshipsClause);
    }

    public static TrackedEntityInstanceStore create(DatabaseAdapter databaseAdapter) {
        SQLStatementBuilderImpl statementBuilder = new SQLStatementBuilderImpl(
                TrackedEntityInstanceTableInfo.TABLE_INFO.name(),
                TrackedEntityInstanceTableInfo.TABLE_INFO.columns());
        SQLStatementWrapper statementWrapper = new SQLStatementWrapper(statementBuilder, databaseAdapter);

        return new TrackedEntityInstanceStoreImpl(
                databaseAdapter,
                statementWrapper,
                statementBuilder
        );
    }
}