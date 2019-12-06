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

package org.hisp.dhis.android.core.program.internal;

import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter;
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.SQLStatementBuilderImpl;
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.db.statementwrapper.internal.SQLStatementWrapper;
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.NameableWithStyleStatementBinder;
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.StatementBinder;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStoreImpl;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.IdentifiableColumns;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramTableInfo;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.List;

import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

public final class ProgramStore extends IdentifiableObjectStoreImpl<Program> implements ProgramStoreInterface {

    private ProgramStore(DatabaseAdapter databaseAdapter,
                         SQLStatementWrapper statementWrapper,
                         SQLStatementBuilderImpl statementBuilder) {
        super(databaseAdapter, statementWrapper, statementBuilder, BINDER, Program::create);
    }
    
    private static StatementBinder<Program> BINDER = new NameableWithStyleStatementBinder<Program>() {
        
        @Override
        public void bindToStatement(@NonNull Program o, @NonNull SQLiteStatement sqLiteStatement) {
            super.bindToStatement(o, sqLiteStatement);
            sqLiteBind(sqLiteStatement, 13, o.version());
            sqLiteBind(sqLiteStatement, 14, o.onlyEnrollOnce());
            sqLiteBind(sqLiteStatement, 15, o.enrollmentDateLabel());
            sqLiteBind(sqLiteStatement, 16, o.displayIncidentDate());
            sqLiteBind(sqLiteStatement, 17, o.incidentDateLabel());
            sqLiteBind(sqLiteStatement, 18, o.registration());
            sqLiteBind(sqLiteStatement, 19, o.selectEnrollmentDatesInFuture());
            sqLiteBind(sqLiteStatement, 20, o.dataEntryMethod());
            sqLiteBind(sqLiteStatement, 21, o.ignoreOverdueEvents());
            sqLiteBind(sqLiteStatement, 22, o.selectIncidentDatesInFuture());
            sqLiteBind(sqLiteStatement, 23, o.useFirstStageDuringRegistration());
            sqLiteBind(sqLiteStatement, 24, o.displayFrontPageList());
            sqLiteBind(sqLiteStatement, 25, o.programType());
            sqLiteBind(sqLiteStatement, 26, UidsHelper.getUidOrNull(o.relatedProgram()));
            sqLiteBind(sqLiteStatement, 27, UidsHelper.getUidOrNull(o.trackedEntityType()));
            sqLiteBind(sqLiteStatement, 28, o.categoryComboUid());
            sqLiteBind(sqLiteStatement, 29, o.access().data().write());
            sqLiteBind(sqLiteStatement, 30, o.expiryDays());
            sqLiteBind(sqLiteStatement, 31, o.completeEventsExpiryDays());
            sqLiteBind(sqLiteStatement, 32, o.expiryPeriodType());
            sqLiteBind(sqLiteStatement, 33, o.minAttributesRequiredToSearch());
            sqLiteBind(sqLiteStatement, 34, o.maxTeiCountToReturn());
            sqLiteBind(sqLiteStatement, 35, o.featureType());
            sqLiteBind(sqLiteStatement, 36, o.accessLevel());
        }
    };

    public static ProgramStoreInterface create(DatabaseAdapter databaseAdapter) {
        SQLStatementBuilderImpl statementBuilder = new SQLStatementBuilderImpl(ProgramTableInfo.TABLE_INFO.name(),
                ProgramTableInfo.TABLE_INFO.columns());
        SQLStatementWrapper statementWrapper = new SQLStatementWrapper(statementBuilder, databaseAdapter);

        return new ProgramStore(databaseAdapter, statementWrapper, statementBuilder);
    }

    @Override
    public List<String> queryWithoutRegistrationProgramUids() throws RuntimeException {
        String whereClause = new WhereClauseBuilder()
                .appendKeyStringValue(ProgramTableInfo.Columns.PROGRAM_TYPE,
                        ProgramType.WITHOUT_REGISTRATION.toString()).build();
        return selectStringColumnsWhereClause(IdentifiableColumns.UID, whereClause);
    }
}