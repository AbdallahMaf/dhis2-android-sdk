/*
 *  Copyright (c) 2004-2021, University of Oslo
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.android.core.event.internal

import dagger.Reusable
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableDataObjectStore
import org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.getSyncState
import org.hisp.dhis.android.core.arch.handlers.internal.HandleAction
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.internal.DataStatePropagator
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentStore
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventTableInfo
import org.hisp.dhis.android.core.fileresource.FileResource
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.imports.internal.BaseImportSummaryHelper.getReferences
import org.hisp.dhis.android.core.imports.internal.EventImportSummary
import org.hisp.dhis.android.core.imports.internal.TrackerImportConflictParser
import org.hisp.dhis.android.core.imports.internal.TrackerImportConflictStore
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityDataValueStore
import org.hisp.dhis.android.core.tracker.importer.internal.JobReportEventHandler
import java.util.*
import javax.inject.Inject

@Reusable
internal class EventImportHandler @Inject constructor(
    private val eventStore: EventStore,
    private val enrollmentStore: EnrollmentStore,
    private val trackerImportConflictStore: TrackerImportConflictStore,
    private val trackerImportConflictParser: TrackerImportConflictParser,
    private val jobReportEventHandler: JobReportEventHandler,
    private val dataStatePropagator: DataStatePropagator,
    private val trackedEntityDataValueStore: TrackedEntityDataValueStore,
    private val fileResourceStore: IdentifiableDataObjectStore<FileResource>
) {

    fun handleEventImportSummaries(
        eventImportSummaries: List<EventImportSummary?>?,
        events: List<Event>,
        fileResources: List<String>
    ) {
        eventImportSummaries?.filterNotNull()?.forEach { eventImportSummary ->
            eventImportSummary.reference()?.let { eventUid ->
                val enrollmentUid = events.find { it.uid() == eventUid }?.enrollment()

                val event = events.find { it.uid() == eventUid }
                val state = getSyncState(eventImportSummary.status())
                trackerImportConflictStore.deleteEventConflicts(eventUid)

                val handleAction = eventStore.setSyncStateOrDelete(eventUid, state)

                if (state == State.ERROR || state == State.WARNING) {
                    setEventFileResourceStates(event, fileResources, State.TO_POST)
                } else {
                    setEventFileResourceStates(event, fileResources, State.SYNCED)
                }

                if (handleAction !== HandleAction.Delete) {
                    storeEventImportConflicts(eventImportSummary, enrollmentUid)
                    dataStatePropagator.refreshEventAggregatedSyncState(eventUid)
                }

                if (state == State.SYNCED &&
                    (handleAction == HandleAction.Update || handleAction == HandleAction.Insert)
                ) {
                    handleIfSynced(eventUid, state)
                }
            }
        }

        processIgnoredEvents(eventImportSummaries, events, fileResources)

        val enrollmentUids = events.mapNotNull { it.enrollment() }.distinct()
        val teiUids = enrollmentUids.mapNotNull { enrollmentStore.selectByUid(it)?.trackedEntityInstance() }.distinct()

        enrollmentUids.forEach {
            dataStatePropagator.refreshEnrollmentAggregatedSyncState(it)
        }

        teiUids.forEach {
            dataStatePropagator.refreshTrackedEntityInstanceAggregatedSyncState(it)
        }
    }

    private fun processIgnoredEvents(
        eventImportSummaries: List<EventImportSummary?>?,
        events: List<Event>,
        fileResources: List<String>
    ) {
        val processedEvents = getReferences(eventImportSummaries)

        events.filterNot { processedEvents.contains(it.uid()) }.forEach { event ->
            val state = State.TO_UPDATE
            trackerImportConflictStore.deleteEventConflicts(event.uid())
            eventStore.setSyncStateOrDelete(event.uid(), state)
            setEventFileResourceStates(event, fileResources, State.TO_POST)
        }
    }

    private fun handleIfSynced(
        eventUid: String,
        state: State
    ) {
        jobReportEventHandler.handleEventNotes(eventUid, state)
        trackedEntityDataValueStore.removeDeletedDataValuesByEvent(eventUid)
    }

    private fun storeEventImportConflicts(
        importSummary: EventImportSummary,
        enrollmentUid: String?
    ) {
        val trackerImportConflicts: MutableList<TrackerImportConflict> = ArrayList()

        if (importSummary.description() != null) {
            trackerImportConflicts.add(
                getConflictBuilder(enrollmentUid, importSummary)
                    .conflict(importSummary.description())
                    .displayDescription(importSummary.description())
                    .value(importSummary.reference())
                    .build()
            )
        }

        importSummary.conflicts()?.forEach { importConflict ->
            trackerImportConflicts.add(
                trackerImportConflictParser
                    .getEventConflict(importConflict, getConflictBuilder(enrollmentUid, importSummary))
            )
        }

        trackerImportConflicts.forEach { trackerImportConflictStore.insert(it) }
    }

    private fun setEventFileResourceStates(event: Event?,
                                           fileResources: List<String>,
                                           state: State) {
        event?.let {
            val values = event.trackedEntityDataValues()?.mapNotNull { it.value() }

            fileResources.filter { values?.contains(it) ?: false }.forEach {
                fileResourceStore.setSyncStateIfUploading(it, state)
            }
        }
    }

    private fun getConflictBuilder(
        enrollmentUid: String?,
        eventImportSummary: EventImportSummary
    ): TrackerImportConflict.Builder {
        val trackedEntityInstanceUid = enrollmentUid?.let {
            enrollmentStore.selectByUid(it)?.trackedEntityInstance()
        }

        return TrackerImportConflict.builder()
            .trackedEntityInstance(trackedEntityInstanceUid)
            .enrollment(enrollmentUid)
            .event(eventImportSummary.reference())
            .tableReference(EventTableInfo.TABLE_INFO.name())
            .status(eventImportSummary.status())
            .created(Date())
    }
}
