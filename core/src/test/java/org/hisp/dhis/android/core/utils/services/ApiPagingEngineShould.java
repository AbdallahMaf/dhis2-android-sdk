/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
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

package org.hisp.dhis.android.core.utils.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ApiPagingEngineShould {

    @Test
    public void calculate_a_paging_list() throws IllegalArgumentException, IllegalStateException {
        List<Paging> calculatedPagingList = ApiPagingEngine.getPaginationList(50, 179);
        Paging paging1 = Paging.create(1,50,0, 0);
        Paging paging2 = Paging.create(2,50,0, 0);
        Paging paging3 = Paging.create(3,50,0, 0);
        Paging lastPaging = Paging.create(6,30,0, 1);

        List<Paging> expectedPagingList = new ArrayList<>(Arrays.asList(paging1, paging2, paging3, lastPaging));

        assertThat(expectedPagingList).isEqualTo(calculatedPagingList);
    }

    @Test
    public void calculate_a_paging_list_if_only_one_page() throws IllegalArgumentException, IllegalStateException {
        List<Paging> calculatedPagingList = ApiPagingEngine.getPaginationList(50, 33);
        Paging paging = Paging.create(1,33,0, 0);
        List<Paging> expectedPagingList = new ArrayList<>(Collections.singletonList(paging));

        assertThat(expectedPagingList).isEqualTo(calculatedPagingList);
    }

    @Test
    public void calculate_paging() throws IllegalArgumentException, IllegalStateException {
        Paging calculatedPaging = ApiPagingEngine.calculateLastPagination(50, 179, 4);
        assertThat(calculatedPaging).isEqualTo(Paging.create(6,30, 0, 1));

        calculatedPaging = ApiPagingEngine.calculateLastPagination(10, 5, 1);
        assertThat(calculatedPaging).isEqualTo(Paging.create(1,5,0, 0));

        calculatedPaging = ApiPagingEngine.calculateLastPagination(100, 32, 1);
        assertThat(calculatedPaging).isEqualTo(Paging.create(1,32,0, 0));

        calculatedPaging = ApiPagingEngine.calculateLastPagination(50, 1688, 34);
        assertThat(calculatedPaging).isEqualTo(Paging.create(36,47,5, 4));

        calculatedPaging = ApiPagingEngine.calculateLastPagination(130, 536,5);
        assertThat(calculatedPaging).isEqualTo(Paging.create(27,20,0, 4));

        calculatedPaging = ApiPagingEngine.calculateLastPagination(50, 50,1);
        assertThat(calculatedPaging).isEqualTo(Paging.create(1,50,0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_current_page_is_negative() throws IllegalArgumentException {
        ApiPagingEngine.getPaginationList(-30, 179);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_page_is_negative() throws IllegalArgumentException {
        ApiPagingEngine.getPaginationList(50, -50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_current_page_is_zero() throws IllegalArgumentException {
        ApiPagingEngine.getPaginationList(0, 30);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_page_is_zero() throws IllegalArgumentException {
        ApiPagingEngine.getPaginationList(120, 0);
    }
}