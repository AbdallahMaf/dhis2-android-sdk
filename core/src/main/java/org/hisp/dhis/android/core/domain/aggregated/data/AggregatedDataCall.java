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
package org.hisp.dhis.android.core.domain.aggregated.data;

import org.hisp.dhis.android.core.arch.api.executors.internal.RxAPICallExecutor;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.arch.call.factories.internal.QueryCallFactory;
import org.hisp.dhis.android.core.arch.call.internal.D2ProgressManager;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore;
import org.hisp.dhis.android.core.arch.helpers.CollectionsHelper;
import org.hisp.dhis.android.core.arch.repositories.collection.ReadOnlyWithDownloadObjectRepository;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.category.internal.CategoryOptionComboStore;
import org.hisp.dhis.android.core.dataapproval.DataApproval;
import org.hisp.dhis.android.core.dataapproval.internal.DataApprovalQuery;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetTableInfo;
import org.hisp.dhis.android.core.dataset.internal.DataSetCompleteRegistrationQuery;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.datavalue.internal.DataValueQuery;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.Period;
import org.hisp.dhis.android.core.period.internal.PeriodStore;
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.user.internal.UserOrganisationUnitLinkStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Single;

@SuppressWarnings("PMD.ExcessiveImports")
final class AggregatedDataCall {

    private final ReadOnlyWithDownloadObjectRepository<SystemInfo> systemInfoRepository;
    private final DHISVersionManager dhisVersionManager;
    private final QueryCallFactory<DataValue, DataValueQuery> dataValueCallFactory;
    private final QueryCallFactory<DataSetCompleteRegistration,
            DataSetCompleteRegistrationQuery> dataSetCompleteRegistrationCallFactory;
    private final QueryCallFactory<DataApproval, DataApprovalQuery> dataApprovalCallFactory;
    private final IdentifiableObjectStore<DataSet> dataSetStore;
    private final PeriodStore periodStore;
    private final UserOrganisationUnitLinkStore organisationUnitStore;
    private final CategoryOptionComboStore categoryOptionComboStore;
    private final RxAPICallExecutor rxCallExecutor;

    @Inject
    AggregatedDataCall(@NonNull ReadOnlyWithDownloadObjectRepository<SystemInfo> systemInfoRepository,
                       @NonNull DHISVersionManager dhisVersionManager,
                       @NonNull QueryCallFactory<DataValue, DataValueQuery> dataValueCallFactory,
                       @NonNull QueryCallFactory<DataSetCompleteRegistration, DataSetCompleteRegistrationQuery>
                               dataSetCompleteRegistrationCallFactory,
                       @NonNull QueryCallFactory<DataApproval, DataApprovalQuery> dataApprovalCallFactory,
                       @NonNull IdentifiableObjectStore<DataSet> dataSetStore,
                       @NonNull PeriodStore periodStore,
                       @NonNull UserOrganisationUnitLinkStore organisationUnitStore,
                       @NonNull CategoryOptionComboStore categoryOptionComboStore,
                       @NonNull RxAPICallExecutor rxCallExecutor) {
        this.systemInfoRepository = systemInfoRepository;
        this.dhisVersionManager = dhisVersionManager;
        this.dataValueCallFactory = dataValueCallFactory;
        this.dataSetCompleteRegistrationCallFactory = dataSetCompleteRegistrationCallFactory;
        this.dataApprovalCallFactory = dataApprovalCallFactory;
        this.dataSetStore = dataSetStore;
        this.periodStore = periodStore;
        this.organisationUnitStore = organisationUnitStore;
        this.categoryOptionComboStore = categoryOptionComboStore;
        this.rxCallExecutor = rxCallExecutor;

    }

    Observable<D2Progress> download() {

        int totalCalls = dhisVersionManager.is2_29() ? 3 : 4;

        D2ProgressManager progressManager = new D2ProgressManager(totalCalls);

        Observable<D2Progress> observable = systemInfoRepository.download(true)
                .toSingle(() -> progressManager.increaseProgressAndCompleteWithCount(SystemInfo.class))
                .flatMapObservable(progress -> downloadInternal(progressManager, progress));
        return rxCallExecutor.wrapObservableTransactionally(observable, true);
    }

    private Observable<D2Progress> downloadInternal(D2ProgressManager progressManager, D2Progress systemInfoProgress) {
        List<String> dataSetUids = Collections.unmodifiableList(dataSetStore.selectUids());
        Set<String> periodIds = Collections.unmodifiableSet(selectPeriodIds(periodStore.selectAll()));
        List<String> organisationUnitUids = Collections.unmodifiableList(
                organisationUnitStore.queryRootCaptureOrganisationUnitUids());

        DataValueQuery dataValueQuery = DataValueQuery.create(dataSetUids, periodIds, organisationUnitUids);

        Single<D2Progress> dataValueSingle = Single.fromCallable(dataValueCallFactory.create(dataValueQuery))
                .map(dataValues -> progressManager.increaseProgressAndCompleteWithCount(DataValue.class));

        DataSetCompleteRegistrationQuery dataSetCompleteRegistrationQuery =
                DataSetCompleteRegistrationQuery.create(dataSetUids, periodIds, organisationUnitUids);

        Single<D2Progress> dataSetCompleteRegistrationSingle = Single.fromCallable(
                dataSetCompleteRegistrationCallFactory.create(dataSetCompleteRegistrationQuery)).map(dataValues ->
                        progressManager.increaseProgressAndCompleteWithCount(DataSetCompleteRegistration.class));


        List<DataSet> dataSetsWithWorkflow =
                dataSetStore.selectWhere(DataSetTableInfo.Columns.WORKFLOW + " IS NOT NULL");

        Set<String> workflowUids = getWorkflowsUidsFrom(dataSetsWithWorkflow);
        Set<String> attributeOptionComboUids = getAttributeOptionCombosUidsFrom(dataSetsWithWorkflow);
        List<String> organisationUnitsUids = organisationUnitStore.queryOrganisationUnitUidsByScope(
                                                                    OrganisationUnit.Scope.SCOPE_DATA_CAPTURE);


        DataApprovalQuery dataApprovalQuery = DataApprovalQuery.create(workflowUids,
                organisationUnitsUids, periodIds, attributeOptionComboUids);

        Single<D2Progress> dataApprovalSingle = Single.fromCallable(
                dataApprovalCallFactory.create(dataApprovalQuery)).map(dataApprovals ->
                progressManager.increaseProgressAndCompleteWithCount(DataApproval.class));


        @SuppressWarnings("PMD.NonStaticInitializer")
        ArrayList<Single<D2Progress>> list = new ArrayList<Single<D2Progress>>() {{
            add(Single.just(systemInfoProgress));
            add(dataValueSingle);
            add(dataSetCompleteRegistrationSingle);
        }};

        if (!dhisVersionManager.is2_29()) {
            list.add(dataApprovalSingle);
        }

        return Single.merge(list).toObservable();
    }

    private Set<String> selectPeriodIds(Collection<Period> periods) {
        Set<String> periodIds = new HashSet<>();

        for (Period period : periods) {
            periodIds.add(period.periodId());
        }
        return periodIds;
    }

    private Set<String> getWorkflowsUidsFrom(Collection<DataSet> dataSetsWithWorkflow) {

        Set<String> workflowsUids = new HashSet<>();
        for (DataSet dataSet : dataSetsWithWorkflow) {
            String uid = dataSet.workflow().uid();
            workflowsUids.add(uid);
        }
        return workflowsUids;
    }

    private Set<String> getAttributeOptionCombosUidsFrom(Collection<DataSet> dataSetsWithWorkflow) {

        Set<String> dataSetsWithWorkflowCategoryCombos = new HashSet<>();
        for (DataSet dataSet : dataSetsWithWorkflow) {
            String uid = dataSet.categoryCombo().uid();
            dataSetsWithWorkflowCategoryCombos.add(uid);
        }

        List<CategoryOptionCombo> categoryOptionCombos =
                categoryOptionComboStore.selectWhere("categoryCombo IN ("
                        + CollectionsHelper.commaAndSpaceSeparatedArrayValues(
                        CollectionsHelper.withSingleQuotationMarksArray(dataSetsWithWorkflowCategoryCombos))
                        + ")");

        Set<String> attributeOptionCombosUids = new HashSet<>();
        for (CategoryOptionCombo categoryOptionCombo : categoryOptionCombos) {
            String uid = categoryOptionCombo.uid();
            attributeOptionCombosUids.add(uid);
        }
        return attributeOptionCombosUids;
    }
}