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
package org.hisp.dhis.android.testapp.category

import com.google.common.truth.Truth.assertThat
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestFullDispatcher
import org.junit.Test

class CategoryCollectionRepositoryMockIntegrationShould : BaseMockIntegrationTestFullDispatcher() {
    @Test
    fun find_all() {
        val categories = d2.categoryModule().categories().blockingGet()

        assertThat(categories.size).isEqualTo(4)
    }

    @Test
    fun filter_by_name() {
        val categories = d2.categoryModule().categories()
            .byName().like("e")
            .blockingGet()

        assertThat(categories.size).isEqualTo(3)
    }

    @Test
    fun filter_by_data_dimension_type() {
        val categories = d2.categoryModule().categories()
            .byDataDimensionType().eq("DISAGGREGATION")
            .blockingGet()

        assertThat(categories.size).isEqualTo(4)
    }

    @Test
    fun include_category_options_as_children() {
        val category = d2.categoryModule().categories()
            .withCategoryOptions()
            .uid("vGs6omsRekv")
            .blockingGet()!!

        assertThat(category.categoryOptions()!!.size).isEqualTo(1)
    }
}