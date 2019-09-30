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

package org.hisp.dhis.android.core.dataset.internal;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.arch.call.internal.D2ProgressManager;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.imports.internal.DataValueImportSummary;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.systeminfo.internal.SystemInfoModuleDownloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Reusable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;

@Reusable
public final class DataSetCompleteRegistrationPostCall {

    private final DataSetCompleteRegistrationService dataSetCompleteRegistrationService;
    private final DataSetCompleteRegistrationStore dataSetCompleteRegistrationStore;
    private final APICallExecutor apiCallExecutor;
    private final SystemInfoModuleDownloader systemInfoDownloader;

    @Inject
    DataSetCompleteRegistrationPostCall(
            @NonNull DataSetCompleteRegistrationService dataSetCompleteRegistrationService,
            @NonNull DataSetCompleteRegistrationStore dataSetCompleteRegistrationStore,
            @NonNull APICallExecutor apiCallExecutor,
            @NonNull SystemInfoModuleDownloader systemInfoDownloader) {

        this.dataSetCompleteRegistrationService = dataSetCompleteRegistrationService;
        this.dataSetCompleteRegistrationStore = dataSetCompleteRegistrationStore;
        this.apiCallExecutor = apiCallExecutor;
        this.systemInfoDownloader = systemInfoDownloader;
    }

    public Observable<D2Progress> uploadDataSetCompleteRegistrations() {
        return Observable.defer(() -> {
            List<DataSetCompleteRegistration> toPostDataSetCompleteRegistrations = new ArrayList<>();
            List<DataSetCompleteRegistration> toDeleteDataSetCompleteRegistrations = new ArrayList<>();

            appendPostableDataValues(toPostDataSetCompleteRegistrations);
            appendToDeleteRegistrations(toDeleteDataSetCompleteRegistrations);

            if (toPostDataSetCompleteRegistrations.isEmpty() && toDeleteDataSetCompleteRegistrations.isEmpty()) {
                return Observable.empty();
            } else {
                D2ProgressManager progressManager = new D2ProgressManager(2);

                Single<D2Progress> systemInfoDownload = systemInfoDownloader.downloadMetadata().toSingle(() ->
                        progressManager.increaseProgress(SystemInfo.class, false));

                return systemInfoDownload.flatMapObservable(systemInfoProgress -> Observable.create(emitter -> {
                    emitter.onNext(systemInfoProgress);
                    uploadInternal(progressManager, emitter, toPostDataSetCompleteRegistrations,
                            toDeleteDataSetCompleteRegistrations);

                }));
            }
        });
    }

    private void uploadInternal(D2ProgressManager progressManager,
                                ObservableEmitter<D2Progress> emitter,
                                List<DataSetCompleteRegistration> toPostDataSetCompleteRegistrations,
                                List<DataSetCompleteRegistration> toDeleteDataSetCompleteRegistrations) throws D2Error {
        DataValueImportSummary dataValueImportSummary = DataValueImportSummary.EMPTY;

        DataSetCompleteRegistrationPayload dataSetCompleteRegistrationPayload
                = new DataSetCompleteRegistrationPayload(toPostDataSetCompleteRegistrations);
        if (!toPostDataSetCompleteRegistrations.isEmpty()) {
            dataValueImportSummary = apiCallExecutor.executeObjectCall(
                    dataSetCompleteRegistrationService.postDataSetCompleteRegistrations(
                            dataSetCompleteRegistrationPayload));
        }

        List<DataSetCompleteRegistration> deletedDataSetCompleteRegistrations = new ArrayList<>();
        List<DataSetCompleteRegistration> withErrorDataSetCompleteRegistrations = new ArrayList<>();
        if (!toDeleteDataSetCompleteRegistrations.isEmpty()) {
            for (DataSetCompleteRegistration dataSetCompleteRegistration
                    : toDeleteDataSetCompleteRegistrations) {
                try {
                    apiCallExecutor.executeObjectCallWithEmptyResponse(
                            dataSetCompleteRegistrationService.deleteDataSetCompleteRegistration(
                                    dataSetCompleteRegistration.dataSet(),
                                    dataSetCompleteRegistration.period(),
                                    dataSetCompleteRegistration.organisationUnit(),
                                    false));
                    deletedDataSetCompleteRegistrations.add(dataSetCompleteRegistration);
                } catch (D2Error d2Error) {
                    withErrorDataSetCompleteRegistrations.add(dataSetCompleteRegistration);
                }
            }
        }

        handleImportSummary(dataSetCompleteRegistrationPayload, dataValueImportSummary,
                deletedDataSetCompleteRegistrations, withErrorDataSetCompleteRegistrations);

        emitter.onNext(progressManager.increaseProgress(DataSetCompleteRegistration.class, true));
        emitter.onComplete();
    }

    private void appendToDeleteRegistrations(
            Collection<DataSetCompleteRegistration> toDeleteDataSetCompleteRegistrations) {
        toDeleteDataSetCompleteRegistrations.addAll(
                dataSetCompleteRegistrationStore.getDeletedRegistrationsPendingToSync());
    }

    private void appendPostableDataValues(Collection<DataSetCompleteRegistration> dataSetCompleteRegistrations) {
        dataSetCompleteRegistrations.addAll(
                dataSetCompleteRegistrationStore.getNonDeletedRegistrationsPendingToSync());
    }

    private DataValueImportSummary handleImportSummary(
            DataSetCompleteRegistrationPayload dataSetCompleteRegistrationPayload,
            DataValueImportSummary dataValueImportSummary,
            List<DataSetCompleteRegistration> deletedDataSetCompleteRegistrations,
            List<DataSetCompleteRegistration> withErrorDataSetCompleteRegistrations) {

        DataSetCompleteRegistrationImportHandler dataSetCompleteRegistrationImportHandler =
                new DataSetCompleteRegistrationImportHandler(dataSetCompleteRegistrationStore);

        return dataSetCompleteRegistrationImportHandler.handleImportSummary(
                dataSetCompleteRegistrationPayload, dataValueImportSummary, deletedDataSetCompleteRegistrations,
                withErrorDataSetCompleteRegistrations);
    }
}