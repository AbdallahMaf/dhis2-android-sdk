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
package org.hisp.dhis.android.core.datavalue.internal

import org.hisp.dhis.android.core.arch.api.fields.internal.Fields
import org.hisp.dhis.android.core.arch.api.filters.internal.Which
import org.hisp.dhis.android.core.datavalue.DataValue
import org.hisp.dhis.android.core.imports.internal.DataValueImportSummary
import org.hisp.dhis.android.core.imports.internal.DataValueImportSummaryWebResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@Suppress("LongParameterList")
internal interface DataValueService {

    @GET("dataValueSets")
    suspend fun getDataValues(
        @Query("fields") @Which fields: Fields<DataValue>,
        @Query("lastUpdated") lastUpdated: String?,
        @Query("dataSet") dataSetUids: String,
        @Query("period") periodIds: String,
        @Query("orgUnit") orgUnitUids: String,
        @Query("children") children: Boolean,
        @Query("paging") paging: Boolean,
        @Query("includeDeleted") includeDeleted: Boolean?,
    ): DataValueSet

    @POST("dataValueSets")
    suspend fun postDataValues(@Body dataValueSet: DataValueSet): DataValueImportSummary

    @POST("dataValueSets")
    suspend fun postDataValuesWebResponse(@Body dataValueSet: DataValueSet): DataValueImportSummaryWebResponse
}