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
package org.hisp.dhis.android.core.enrollment

import org.hisp.dhis.android.core.event.NewTrackerImporterEventTransformer
import org.hisp.dhis.android.core.note.NewTrackerImporterNoteTransformer
import org.hisp.dhis.android.core.relationship.NewTrackerImporterRelationshipTransformer
import org.hisp.dhis.android.core.trackedentity.NewTrackerImporterTrackedEntityAttributeValueTransformer
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

internal object NewTrackerImporterEnrollmentTransformer {
    fun transform(
        o: Enrollment,
        teiAttributes: List<TrackedEntityAttributeValue>?,
        programAttributeMap: Map<String, List<String>>,
    ): NewTrackerImporterEnrollment {
        val programAttributeUids = programAttributeMap[o.program()] ?: emptyList()
        val enrollmentAttributeValues = teiAttributes
            ?.filter { programAttributeUids.contains(it.trackedEntityAttribute()) }
            ?.map { NewTrackerImporterTrackedEntityAttributeValueTransformer.transform(it) }
            ?: emptyList()

        return NewTrackerImporterEnrollment.builder()
            .id(o.id())
            .uid(o.uid())
            .deleted(o.deleted())
            .createdAt(o.created())
            .updatedAt(o.lastUpdated())
            .createdAtClient(o.createdAtClient())
            .updatedAtClient(o.lastUpdatedAtClient())
            .organisationUnit(o.organisationUnit())
            .program(o.program())
            .enrolledAt(o.enrollmentDate())
            .occurredAt(o.incidentDate())
            .completedAt(o.completedDate())
            .followUp(o.followUp())
            .status(o.status())
            .trackedEntity(o.trackedEntityInstance())
            .geometry(o.geometry())
            .syncState(o.syncState())
            .aggregatedSyncState(o.aggregatedSyncState())
            .notes(
                o.notes()?.map {
                    NewTrackerImporterNoteTransformer.transform(it)
                },
            )
            .attributes(enrollmentAttributeValues)
            .build()
    }

    fun deTransform(
        o: NewTrackerImporterEnrollment,
    ): Enrollment {
        val notes = o.notes()?.map { NewTrackerImporterNoteTransformer.deTransform(it) }
        val events = o.events()?.map { NewTrackerImporterEventTransformer.deTransform(it) }
        val relationships = o.relationships()?.map { NewTrackerImporterRelationshipTransformer.deTransform(it) }

        return Enrollment.builder()
            .id(o.id())
            .uid(o.uid())
            .deleted(o.deleted())
            .created(o.createdAt())
            .lastUpdated(o.updatedAt())
            .createdAtClient(o.createdAtClient())
            .lastUpdatedAtClient(o.updatedAtClient())
            .organisationUnit(o.organisationUnit())
            .program(o.program())
            .enrollmentDate(o.enrolledAt())
            .incidentDate(o.occurredAt())
            .completedDate(o.completedAt())
            .followUp(o.followUp())
            .status(o.status())
            .trackedEntityInstance(o.trackedEntity())
            .geometry(o.geometry())
            .syncState(o.syncState())
            .aggregatedSyncState(o.aggregatedSyncState())
            .notes(notes)
            .events(events)
            .relationships(relationships)
            .build()
    }
}
