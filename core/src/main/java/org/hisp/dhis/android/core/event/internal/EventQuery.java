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

package org.hisp.dhis.android.core.event.internal;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.arch.call.queries.internal.BaseQuery;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;

import java.util.Collection;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@AutoValue
public abstract class EventQuery extends BaseQuery {

    @Nullable
    public abstract String orgUnit();

    @Nullable
    public abstract String program();

    @Nullable
    public abstract String trackedEntityInstance();

    @NonNull
    public abstract OrganisationUnitMode ouMode();

    @Nullable
    public abstract String lastUpdatedStartDate();

    @Nullable
    public abstract Collection<String> uids();

    public static Builder builder() {
        return new AutoValue_EventQuery.Builder()
                .page(1)
                .pageSize(DEFAULT_PAGE_SIZE)
                .paging(true)
                .ouMode(OrganisationUnitMode.SELECTED)
                .uids(Collections.emptyList());
    }

    @AutoValue.Builder
    public abstract static class Builder extends BaseQuery.Builder<Builder> {
        public abstract Builder orgUnit(String orgUnit);

        public abstract Builder program(String program);

        public abstract Builder trackedEntityInstance(String trackedEntityInstance);

        public abstract Builder ouMode(OrganisationUnitMode ouMode);

        public abstract Builder lastUpdatedStartDate(String lastUpdatedStartDate);

        public abstract Builder uids(Collection<String> uIds);

        public abstract EventQuery build();
    }
}