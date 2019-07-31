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

import com.google.common.collect.Lists;

import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.data.trackedentity.TrackedEntityDataValueSamples;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueStore;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueStoreImpl;
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestMetadataEnqueable;
import org.hisp.dhis.android.core.utils.runner.D2JunitRunner;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(D2JunitRunner.class)
public class EventPostCallMockIntegrationShould extends BaseMockIntegrationTestMetadataEnqueable {

    private static EventPostCall eventPostCall;
    private static EventStore eventStore;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseMockIntegrationTestMetadataEnqueable.setUpClass();
        eventStore = EventStoreImpl.create(objects.databaseAdapter);
        eventPostCall = objects.d2DIComponent.eventPostCall();
    }

    @After
    public void tearDown() throws D2Error {
        d2.wipeModule().wipeData();
    }

    @Test
    public void build_payload_with_different_enrollments() {
        storeEvents();

        List<Event> events = eventPostCall.queryDataToSync(null);

        assertThat(events.size()).isEqualTo(3);

        for (Event event : events) {
            assertThat(event.trackedEntityDataValues().size()).isEqualTo(1);
        }
    }

    @Test
    public void handle_import_conflicts_correctly() {
        storeEvents();

        dhis2MockServer.enqueueMockResponse("imports/web_response_with_event_import_conflicts.json");

        d2.eventModule().events.upload().blockingSubscribe();

        assertThat(d2.importModule().trackerImportConflicts.count()).isEqualTo(3);
    }

    @Test
    public void delete_old_import_conflicts() {
        storeEvents();

        dhis2MockServer.enqueueMockResponse("imports/web_response_with_event_import_conflicts.json");
        d2.eventModule().events.upload().blockingSubscribe();
        assertThat(d2.importModule().trackerImportConflicts.count()).isEqualTo(3);

        eventStore.setState("event1Id", State.TO_POST);
        eventStore.setState("event2Id", State.TO_POST);
        eventStore.setState("event3Id", State.TO_POST);

        dhis2MockServer.enqueueMockResponse("imports/web_response_with_event_import_conflicts2.json");
        d2.eventModule().events.upload().blockingSubscribe();
        assertThat(d2.importModule().trackerImportConflicts.count()).isEqualTo(2);
    }

    @Test
    public void handle_event_deletions() {
        storeEvents();
        assertThat(d2.eventModule().events.count()).isEqualTo(4);

        eventStore.setState("event1Id", State.TO_DELETE);

        dhis2MockServer.enqueueMockResponse("imports/web_response_with_event_import_conflicts2.json");

        d2.eventModule().events.upload().blockingSubscribe();

        assertThat(d2.eventModule().events.count()).isEqualTo(3);
    }

    @Test
    public void recreate_events_with_filters() {
        String event1 = "event1";
        String event2 = "event2";
        String event3 = "event3";
        String event4 = "event4";

        Program program = d2.programModule().programs.one().get();

        storeSingleEvent(event1, program, State.TO_POST);
        storeSingleEvent(event2, program, State.TO_UPDATE);
        storeSingleEvent(event3, program, State.TO_DELETE);
        storeSingleEvent(event4, program, State.SYNCED);

        List<Event> events = eventPostCall.queryDataToSync(
                d2.eventModule().events.byProgramUid().eq(program.uid())
                .byState().in(State.TO_POST, State.TO_UPDATE, State.TO_DELETE).blockingGet());

        assertThat(events.size()).isEqualTo(3);
        assertThat(UidsHelper.getUidsList(events).containsAll(Lists.newArrayList(event1, event2, event3)))
                .isEqualTo(true);
    }

    private void storeEvents() {
        String event1Id = "event1Id";
        String event2Id = "event2Id";
        String event3Id = "event3Id";
        String event4Id = "event4Id";

        OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits.one().get();
        Program program = d2.programModule().programs.one().get();
        ProgramStage programStage = d2.programModule().programStages.one().get();

        TrackedEntityDataValue dataValue1 = TrackedEntityDataValueSamples.get().toBuilder().event(event1Id).build();

        Event event1 = Event.builder()
                .uid(event1Id)
                .organisationUnit(orgUnit.uid())
                .program(program.uid())
                .programStage(programStage.uid())
                .state(State.TO_POST)
                .trackedEntityDataValues(Collections.singletonList(dataValue1))
                .build();

        TrackedEntityDataValue dataValue2 = TrackedEntityDataValueSamples.get().toBuilder().event(event2Id).build();

        Event event2 = Event.builder()
                .uid(event2Id)
                .organisationUnit(orgUnit.uid())
                .program(program.uid())
                .programStage(programStage.uid())
                .state(State.TO_POST)
                .trackedEntityDataValues(Collections.singletonList(dataValue2))
                .build();

        TrackedEntityDataValue dataValue3 = TrackedEntityDataValueSamples.get().toBuilder().event(event3Id).build();

        Event event3 = Event.builder()
                .uid(event3Id)
                .organisationUnit(orgUnit.uid())
                .program(program.uid())
                .programStage(programStage.uid())
                .state(State.TO_POST)
                .trackedEntityDataValues(Collections.singletonList(dataValue3))
                .build();

        TrackedEntityDataValue dataValue4 = TrackedEntityDataValueSamples.get().toBuilder().event(event4Id).build();

        Event event4 = Event.builder()
                .uid(event4Id)
                .organisationUnit(orgUnit.uid())
                .program(program.uid())
                .programStage(programStage.uid())
                .state(State.ERROR)
                .trackedEntityDataValues(Collections.singletonList(dataValue4))
                .build();

        eventStore.insert(event1);
        eventStore.insert(event2);
        eventStore.insert(event3);
        eventStore.insert(event4);

        TrackedEntityDataValueStore tedvStore = TrackedEntityDataValueStoreImpl.create(databaseAdapter);
        tedvStore.insert(dataValue1);
        tedvStore.insert(dataValue2);
        tedvStore.insert(dataValue3);
        tedvStore.insert(dataValue4);

        assertThat(d2.eventModule().events.count()).isEqualTo(4);
    }

    private void storeSingleEvent(String eventUid, Program program, State state) {
        OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits.one().get();
        ProgramStage programStage = d2.programModule().programStages.one().get();

        eventStore.insert(
                Event.builder()
                        .uid(eventUid)
                        .organisationUnit(orgUnit.uid())
                        .program(program.uid())
                        .programStage(programStage.uid())
                        .state(state)
                        .deleted(false)
                        .build());
    }
}