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

package org.hisp.dhis.android.core.datavalue.internal;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.arch.call.internal.D2ProgressManager;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.imports.internal.DataValueImportSummary;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.systeminfo.internal.SystemInfoModuleDownloader;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Reusable;
import io.reactivex.Observable;
import io.reactivex.Single;

@Reusable
public final class DataValuePostCall {

    private final DataValueService dataValueService;
    private final DataValueStore dataValueStore;
    private final APICallExecutor apiCallExecutor;
    private final SystemInfoModuleDownloader systemInfoDownloader;

    @Inject
    DataValuePostCall(@NonNull DataValueService dataValueService,
                      @NonNull DataValueStore dataValueSetStore,
                      @NonNull APICallExecutor apiCallExecutor,
                      @NonNull SystemInfoModuleDownloader systemInfoDownloader) {

        this.dataValueService = dataValueService;
        this.dataValueStore = dataValueSetStore;
        this.apiCallExecutor = apiCallExecutor;
        this.systemInfoDownloader = systemInfoDownloader;
    }

    public Observable<D2Progress> uploadDataValues() {
        return Observable.defer(() -> {
            Collection<DataValue> toPostDataValues = new ArrayList<>();

            appendPostableDataValues(toPostDataValues);
            appendUpdatableDataValues(toPostDataValues);

            if (toPostDataValues.isEmpty()) {
                return Observable.empty();
            } else {
                D2ProgressManager progressManager = new D2ProgressManager(2);

                Single<D2Progress> systemInfoDownload = systemInfoDownloader.downloadMetadata().toSingle(() ->
                        progressManager.increaseProgress(SystemInfo.class, false));

                return systemInfoDownload.flatMapObservable(systemInfoProgress -> Observable.create(emitter -> {
                    DataValueSet dataValueSet = new DataValueSet(toPostDataValues);

                    DataValueImportSummary dataValueImportSummary = apiCallExecutor.executeObjectCall(
                            dataValueService.postDataValues(dataValueSet));

                    handleImportSummary(dataValueSet, dataValueImportSummary);

                    emitter.onNext(progressManager.increaseProgress(DataValue.class, true));
                    emitter.onComplete();
                }));
            }
        });
    }

    private void appendPostableDataValues(Collection<DataValue> dataValues) {
        dataValues.addAll(dataValueStore.getDataValuesWithState(State.TO_POST));
    }

    private void appendUpdatableDataValues(Collection<DataValue> dataValues) {
        dataValues.addAll(dataValueStore.getDataValuesWithState(State.TO_UPDATE));
    }

    private void handleImportSummary(DataValueSet dataValueSet, DataValueImportSummary dataValueImportSummary) {

        DataValueImportHandler dataValueImportHandler =
                new DataValueImportHandler(dataValueStore);

        dataValueImportHandler.handleImportSummary(dataValueSet, dataValueImportSummary);
    }
}
