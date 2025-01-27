/*
 *  Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.android.core.domain.metadata

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.arch.api.executors.internal.CoroutineAPICallExecutor
import org.hisp.dhis.android.core.arch.api.executors.internal.CoroutineAPICallExecutorMock
import org.hisp.dhis.android.core.arch.call.BaseD2Progress
import org.hisp.dhis.android.core.arch.storage.internal.CredentialsSecureStore
import org.hisp.dhis.android.core.category.internal.CategoryModuleDownloader
import org.hisp.dhis.android.core.common.BaseCallShould
import org.hisp.dhis.android.core.configuration.internal.MultiUserDatabaseManager
import org.hisp.dhis.android.core.constant.internal.ConstantModuleDownloader
import org.hisp.dhis.android.core.dataset.internal.DataSetModuleDownloader
import org.hisp.dhis.android.core.expressiondimensionitem.internal.ExpressionDimensionItemModuleDownloader
import org.hisp.dhis.android.core.indicator.internal.IndicatorModuleDownloader
import org.hisp.dhis.android.core.legendset.internal.LegendSetModuleDownloader
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.ForeignKeyViolationTableInfo
import org.hisp.dhis.android.core.organisationunit.internal.OrganisationUnitModuleDownloader
import org.hisp.dhis.android.core.program.internal.ProgramIndicatorModuleDownloader
import org.hisp.dhis.android.core.program.internal.ProgramModuleDownloader
import org.hisp.dhis.android.core.settings.internal.GeneralSettingCall
import org.hisp.dhis.android.core.settings.internal.SettingModuleDownloader
import org.hisp.dhis.android.core.sms.SmsModule
import org.hisp.dhis.android.core.sms.domain.interactor.ConfigCase
import org.hisp.dhis.android.core.systeminfo.internal.SystemInfoModuleDownloader
import org.hisp.dhis.android.core.usecase.UseCaseModuleDownloader
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.internal.UserModuleDownloader
import org.hisp.dhis.android.core.visualization.internal.VisualizationModuleDownloader
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class MetadataCallShould : BaseCallShould() {
    private val user: User = mock()
    private val coroutineAPICallExecutor: CoroutineAPICallExecutor = CoroutineAPICallExecutorMock()
    private val systemInfoDownloader: SystemInfoModuleDownloader = mock()
    private val systemSettingDownloader: SettingModuleDownloader = mock()
    private val useCaseModuleDownloader: UseCaseModuleDownloader = mock()
    private val userDownloader: UserModuleDownloader = mock()
    private val categoryDownloader: CategoryModuleDownloader = mock()
    private val programDownloader: ProgramModuleDownloader = mock()
    private val organisationUnitDownloader: OrganisationUnitModuleDownloader = mock()
    private val dataSetDownloader: DataSetModuleDownloader = mock()
    private val visualizationDownloader: VisualizationModuleDownloader = mock()
    private val constantDownloader: ConstantModuleDownloader = mock()
    private val indicatorDownloader: IndicatorModuleDownloader = mock()
    private val programIndicatorModuleDownloader: ProgramIndicatorModuleDownloader = mock()
    private val smsModule: SmsModule = mock()
    private val configCase: ConfigCase = mock()
    private val generalSettingCall: GeneralSettingCall = mock()
    private val multiUserDatabaseManager: MultiUserDatabaseManager = mock()
    private val credentialsSecureStore: CredentialsSecureStore = mock()
    private val legendSetModuleDownloader: LegendSetModuleDownloader = mock()
    private val expressionDimensIndicatorModuleDownloader: ExpressionDimensionItemModuleDownloader = mock()

    private val networkError: D2Error = D2Error.builder()
        .errorCode(D2ErrorCode.UNKNOWN_HOST)
        .errorDescription("Unknown host")
        .build()

    // object to test
    private lateinit var metadataCall: MetadataCall

    @SuppressWarnings("LongMethod")
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        // Calls
        systemInfoDownloader.stub {
            onBlocking { downloadWithProgressManager(any()) }.doReturn(BaseD2Progress.empty(10))
        }
        systemInfoDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        useCaseModuleDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        userDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(user)
        }
        programDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        organisationUnitDownloader.stub {
            onBlocking { downloadMetadata(same(user)) }.doReturn(Unit)
        }

        dataSetDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }

        programIndicatorModuleDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        visualizationDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(emptyList())
        }
        legendSetModuleDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        expressionDimensIndicatorModuleDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        constantDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(emptyList())
        }
        indicatorDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        categoryDownloader.stub {
            onBlocking { downloadMetadata() }.doReturn(Unit)
        }
        whenever(smsModule.configCase()).thenReturn(configCase)
        whenever(configCase.refreshMetadataIdsCallable()).thenReturn(Completable.complete())
        generalSettingCall.stub {
            onBlocking { isDatabaseEncrypted() }.doReturn(false)
        }
        // Metadata call
        metadataCall = MetadataCall(
            coroutineAPICallExecutor,
            systemInfoDownloader,
            systemSettingDownloader,
            useCaseModuleDownloader,
            userDownloader,
            categoryDownloader,
            programDownloader,
            organisationUnitDownloader,
            dataSetDownloader,
            visualizationDownloader,
            constantDownloader,
            indicatorDownloader,
            programIndicatorModuleDownloader,
            smsModule,
            databaseAdapter,
            generalSettingCall,
            multiUserDatabaseManager,
            credentialsSecureStore,
            legendSetModuleDownloader,
            expressionDimensIndicatorModuleDownloader,
        )
    }

    @Test
    fun succeed_when_endpoint_calls_succeed() {
        metadataCall.blockingDownload()
    }

    @Test
    fun fail_when_system_info_call_fail() {
        systemInfoDownloader.stub {
            onBlocking { downloadWithProgressManager(any()) }.doAnswer { throw networkError }
        }
        downloadAndAssertError()
    }

    @Test
    fun fail_when_system_setting_call_fail() = runTest {
        whenever(systemSettingDownloader.downloadMetadata()).doAnswer { throw networkError }

        downloadAndAssertError()
    }

    @Test
    fun fail_when_user_call_fail() {
        userDownloader.stub {
            onBlocking { downloadMetadata() }.doAnswer { throw networkError }
        }
        downloadAndAssertError()
    }

    @Test
    fun fail_when_category_download_call_fail() = runTest {
        whenever(categoryDownloader.downloadMetadata()).doAnswer { throw networkError }
        downloadAndAssertError()
    }

    @Test
    fun fail_when_visualization_download_call_fail() {
        visualizationDownloader.stub {
            onBlocking { downloadMetadata() }.doAnswer { throw networkError }
        }
        downloadAndAssertError()
    }

    @Test
    fun fail_when_program_call_fail() = runTest {
        whenever(programDownloader.downloadMetadata()) doAnswer { throw networkError }
        downloadAndAssertError()
    }

    @Test
    fun fail_when_organisation_unit_call_fail() = runTest {
        whenever(organisationUnitDownloader.downloadMetadata(user)) doAnswer { throw networkError }
        downloadAndAssertError()
    }

    @Test
    fun fail_when_dataset_parent_call_fail() = runTest {
        whenever(dataSetDownloader.downloadMetadata()) doAnswer { throw networkError }
        downloadAndAssertError()
    }

    @Test
    fun fail_when_constant_call_fail() {
        constantDownloader.stub {
            onBlocking { downloadMetadata() }.doAnswer { throw networkError }
        }
        downloadAndAssertError()
    }

    @Test
    fun delete_foreign_key_violations_before_calls() {
        metadataCall.blockingDownload()
        verify(databaseAdapter).delete(ForeignKeyViolationTableInfo.TABLE_INFO.name())
    }

    private fun downloadAndAssertError() = runBlocking {
        try {
            metadataCall.download().collect()
            fail("It should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(D2Error::class.java)
        }
    }
}
