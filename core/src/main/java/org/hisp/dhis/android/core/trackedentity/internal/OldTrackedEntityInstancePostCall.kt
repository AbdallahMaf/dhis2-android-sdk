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
package org.hisp.dhis.android.core.trackedentity.internal

import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import javax.inject.Inject
import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.arch.call.internal.D2ProgressManager
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.internal.TEIWebResponse
import org.hisp.dhis.android.core.imports.internal.TEIWebResponseHandler
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.relationship.internal.RelationshipPostCall
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

@Reusable
internal class OldTrackedEntityInstancePostCall @Inject internal constructor(
    private val payloadGenerator29: TrackedEntityInstancePostPayloadGenerator29,
    private val stateManager: TrackerPostStateManager,
    private val versionManager: DHISVersionManager,
    private val trackedEntityInstanceService: TrackedEntityInstanceService,
    private val teiWebResponseHandler: TEIWebResponseHandler,
    private val apiCallExecutor: APICallExecutor,
    private val relationshipPostCall: RelationshipPostCall,
    private val fileResourcePostCall: OldTrackerImporterFileResourcesPostCall,
    private val oldTrackerImporterPostCall: OldTrackerImporterPostCall
) {

    fun uploadTrackedEntityInstances(
        trackedEntityInstances: List<TrackedEntityInstance>
    ): Observable<D2Progress> {
        return if (versionManager.is2_29) {
            uploadTrackedEntityInstances29(trackedEntityInstances)
        } else {
            oldTrackerImporterPostCall.uploadTrackedEntityInstances(trackedEntityInstances)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun uploadTrackedEntityInstances29(
        filteredTrackedEntityInstances: List<TrackedEntityInstance>
    ): Observable<D2Progress> {
        return Observable.create { emitter: ObservableEmitter<D2Progress> ->
            val teiPartitions = payloadGenerator29.getTrackedEntityInstancesPartitions29(filteredTrackedEntityInstances)
                .map { partition -> fileResourcePostCall.uploadTrackedEntityFileResources(partition).blockingGet() }
                .filter { it.first.isNotEmpty() }

            val progressManager = D2ProgressManager(teiPartitions.size)
            for (partition in teiPartitions) {
                stateManager.setPayloadStates(
                    trackedEntityInstances = partition.first,
                    forcedState = State.UPLOADING
                )
                val thisPartition = relationshipPostCall.postDeletedRelationships29(partition.first)
                try {
                    val trackedEntityInstancePayload = TrackedEntityInstancePayload.create(thisPartition)
                    val webResponse = apiCallExecutor.executeObjectCallWithAcceptedErrorCodes(
                        trackedEntityInstanceService.postTrackedEntityInstances(
                            trackedEntityInstancePayload, "CREATE_AND_UPDATE"
                        ),
                        @Suppress("MagicNumber")
                        listOf(409),
                        TEIWebResponse::class.java
                    )
                    teiWebResponseHandler.handleWebResponse(webResponse, thisPartition, partition.second)
                    emitter.onNext(progressManager.increaseProgress(TrackedEntityInstance::class.java, false))
                } catch (e: Exception) {
                    stateManager.restorePayloadStates(
                        trackedEntityInstances = thisPartition,
                        fileResources = partition.second
                    )
                    if (e is D2Error && e.isOffline) {
                        emitter.onError(e)
                        break
                    } else {
                        emitter.onNext(
                            progressManager.increaseProgress(
                                TrackedEntityInstance::class.java,
                                false
                            )
                        )
                    }
                }
            }
            emitter.onComplete()
        }
    }
}