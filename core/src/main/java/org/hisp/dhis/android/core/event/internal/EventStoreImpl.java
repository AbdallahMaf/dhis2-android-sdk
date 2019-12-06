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

package org.hisp.dhis.android.core.event.internal;

import android.database.Cursor;

import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter;
import org.hisp.dhis.android.core.arch.db.cursors.internal.ObjectFactory;
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.SQLStatementBuilderImpl;
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.db.statementwrapper.internal.SQLStatementWrapper;
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.StatementBinder;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableDeletableDataObjectStoreImpl;
import org.hisp.dhis.android.core.common.IdentifiableColumns;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventTableInfo;
import org.hisp.dhis.android.core.event.EventTableInfo.Columns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

public final class EventStoreImpl extends IdentifiableDeletableDataObjectStoreImpl<Event> implements EventStore {

    private static final String QUERY_SINGLE_EVENTS = "SELECT Event.* FROM Event WHERE Event.enrollment IS NULL";

    private static final StatementBinder<Event> BINDER = (o, sqLiteStatement) -> {
        sqLiteBind(sqLiteStatement, 1, o.uid());
        sqLiteBind(sqLiteStatement, 2, o.enrollment());
        sqLiteBind(sqLiteStatement, 3, o.created());
        sqLiteBind(sqLiteStatement, 4, o.lastUpdated());
        sqLiteBind(sqLiteStatement, 5, o.createdAtClient());
        sqLiteBind(sqLiteStatement, 6, o.lastUpdatedAtClient());
        sqLiteBind(sqLiteStatement, 7, o.status());
        sqLiteBind(sqLiteStatement, 8, o.geometry() == null ? null : o.geometry().type());
        sqLiteBind(sqLiteStatement, 9, o.geometry() == null ? null : o.geometry().coordinates());
        sqLiteBind(sqLiteStatement, 10, o.program());
        sqLiteBind(sqLiteStatement, 11, o.programStage());
        sqLiteBind(sqLiteStatement, 12, o.organisationUnit());
        sqLiteBind(sqLiteStatement, 13, o.eventDate());
        sqLiteBind(sqLiteStatement, 14, o.completedDate());
        sqLiteBind(sqLiteStatement, 15, o.dueDate());
        sqLiteBind(sqLiteStatement, 16, o.state());
        sqLiteBind(sqLiteStatement, 17, o.attributeOptionCombo());
        sqLiteBind(sqLiteStatement, 18, o.deleted());
    };

    private EventStoreImpl(DatabaseAdapter databaseAdapter,
                           SQLStatementWrapper statementWrapper,
                           SQLStatementBuilderImpl builder,
                           StatementBinder<Event> binder,
                           ObjectFactory<Event> objectFactory) {
        super(databaseAdapter, statementWrapper, builder, binder, objectFactory);
    }

    @Override
    public Map<String, List<Event>> queryEventsAttachedToEnrollmentToPost() {
        String eventsAttachedToEnrollmentsQuery = new WhereClauseBuilder()
                .appendIsNotNullValue(Columns.ENROLLMENT)
                .appendInKeyStringValues(Columns.STATE, Arrays.asList(
                        State.TO_POST.name(),
                        State.TO_UPDATE.name())).build();

        List<Event> eventList = selectWhere(eventsAttachedToEnrollmentsQuery);

        Map<String, List<Event>> eventsMap = new HashMap<>();
        for (Event event : eventList) {
            addEventsToMap(eventsMap, event);
        }

        return eventsMap;
    }

    @Override
    public List<Event> querySingleEventsToPost() {
        String singleEventsToPostQuery = QUERY_SINGLE_EVENTS +
                " AND (Event.state = '" + State.TO_POST + "' OR Event.state = '" + State.TO_UPDATE + "')";
        return eventListFromQuery(singleEventsToPostQuery);
    }

    @Override
    public List<Event> querySingleEvents() {
        return eventListFromQuery(QUERY_SINGLE_EVENTS);
    }

    @Override
    public List<Event> queryOrderedForEnrollmentAndProgramStage(String enrollmentUid, String programStageUid) {
        String byEnrollmentAndProgramStageQuery = "SELECT Event.* FROM Event " +
                "WHERE Event.enrollment = '" + enrollmentUid + "' " +
                "AND Event.programStage = '" + programStageUid + "' " +
                "ORDER BY Event." + Columns.EVENT_DATE + ", Event." + Columns.LAST_UPDATED;

        return eventListFromQuery(byEnrollmentAndProgramStageQuery);
    }

    @Override
    public Integer countEventsForEnrollment(String enrollmentUid) {
        String countByEnrollment = "SELECT Event.* FROM Event " +
                "WHERE Event.enrollment = '" + enrollmentUid + "'";

        List<Event> events = eventListFromQuery(countByEnrollment);
        return events.size();
    }

    @Override
    public int countTeisWhereEvents(String whereClause) {
        String whereStatement = whereClause == null ? "" : " WHERE " + whereClause;
        String query = "SELECT COUNT(DISTINCT a." + EnrollmentTableInfo.Columns.TRACKED_ENTITY_INSTANCE + ") " +
                "FROM " + EnrollmentTableInfo.TABLE_INFO.name() + " a " +
                "INNER JOIN " +
                "(SELECT DISTINCT " + Columns.ENROLLMENT +
                    " FROM " + EventTableInfo.TABLE_INFO.name() + whereStatement + ") b " +
                "ON a." + IdentifiableColumns.UID + " = b." + Columns.ENROLLMENT;

        return processCount(databaseAdapter.query(query));
    }

    private List<Event> eventListFromQuery(String query) {
        List<Event> eventList = new ArrayList<>();
        Cursor cursor = databaseAdapter.query(query);
        addObjectsToCollection(cursor, eventList);
        return eventList;
    }

    private void addEventsToMap(Map<String, List<Event>> eventsMap, Event event) {
        if (eventsMap.get(event.enrollment()) == null) {
            eventsMap.put(event.enrollment(), new ArrayList<>());
        }

        eventsMap.get(event.enrollment()).add(event);
    }

    public static EventStore create(DatabaseAdapter databaseAdapter) {
        SQLStatementBuilderImpl statementBuilder = new SQLStatementBuilderImpl(
                EventTableInfo.TABLE_INFO.name(),
                EventTableInfo.TABLE_INFO.columns());
        SQLStatementWrapper statementWrapper = new SQLStatementWrapper(statementBuilder, databaseAdapter);

        return new EventStoreImpl(
                databaseAdapter,
                statementWrapper,
                statementBuilder,
                BINDER,
                Event::create
        );
    }
}