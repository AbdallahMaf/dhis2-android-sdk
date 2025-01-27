/*
 *  Copyright (c) 2004-2023, University of Oslo
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
package org.hisp.dhis.android.core.event

import org.hisp.dhis.android.core.arch.handlers.internal.TwoWayTransformer
import org.hisp.dhis.android.core.note.NewTrackerImporterNoteTransformer
import org.hisp.dhis.android.core.relationship.NewTrackerImporterRelationshipTransformer
import org.hisp.dhis.android.core.trackedentity.NewTrackerImporterTrackedEntityDataValueTransformer
import org.hisp.dhis.android.core.trackedentity.NewTrackerImporterUserInfo

internal object NewTrackerImporterEventTransformer : TwoWayTransformer<Event, NewTrackerImporterEvent> {
    override fun transform(o: Event): NewTrackerImporterEvent {
        return NewTrackerImporterEvent.builder()
            .id(o.id())
            .uid(o.uid())
            .deleted(o.deleted())
            .enrollment(o.enrollment())
            .createdAt(o.created())
            .updatedAt(o.lastUpdated())
            .createdAtClient(o.createdAtClient())
            .updatedAtClient(o.lastUpdatedAtClient())
            .program(o.program())
            .programStage(o.programStage())
            .organisationUnit(o.organisationUnit())
            .occurredAt(o.eventDate())
            .status(o.status())
            .geometry(o.geometry())
            .completedAt(o.completedDate())
            .completedBy(o.completedBy())
            .scheduledAt(o.dueDate())
            .attributeOptionCombo(o.attributeOptionCombo())
            .assignedUser(o.assignedUser()?.let { NewTrackerImporterUserInfo.builder().uid(it).build() })
            .syncState(o.syncState())
            .aggregatedSyncState(o.aggregatedSyncState())
            .trackedEntityDataValues(
                o.trackedEntityDataValues()?.map {
                    NewTrackerImporterTrackedEntityDataValueTransformer.transform(it)
                },
            )
            .notes(
                o.notes()?.map {
                    NewTrackerImporterNoteTransformer.transform(it)
                },
            )
            .build()
    }

    override fun deTransform(t: NewTrackerImporterEvent): Event {
        val notes = t.notes()?.map { NewTrackerImporterNoteTransformer.deTransform(it) }

        val dataValues = t.trackedEntityDataValues()?.map {
            NewTrackerImporterTrackedEntityDataValueTransformer.deTransform(it)
        }
        val relationships = t.relationships()?.map { NewTrackerImporterRelationshipTransformer.deTransform(it) }

        return Event.builder()
            .id(t.id())
            .uid(t.uid())
            .deleted(t.deleted())
            .enrollment(t.enrollment())
            .created(t.createdAt())
            .lastUpdated(t.updatedAt())
            .createdAtClient(t.createdAtClient())
            .lastUpdatedAtClient(t.updatedAtClient())
            .program(t.program())
            .programStage(t.programStage())
            .organisationUnit(t.organisationUnit())
            .eventDate(t.occurredAt())
            .status(t.status())
            .geometry(t.geometry())
            .completedDate(t.completedAt())
            .completedBy(t.completedBy())
            .dueDate(t.scheduledAt())
            .attributeOptionCombo(t.attributeOptionCombo())
            .assignedUser(t.assignedUser()?.uid())
            .syncState(t.syncState())
            .aggregatedSyncState(t.aggregatedSyncState())
            .trackedEntityDataValues(dataValues)
            .notes(notes)
            .relationships(relationships)
            .trackedEntityInstance(t.trackedEntity())
            .build()
    }
}
