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

package org.hisp.dhis.android.core.trackedentity;

import org.hisp.dhis.android.core.arch.fields.FieldsHelper;
import org.hisp.dhis.android.core.common.Access;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectStyleFields;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeRendering;
import org.hisp.dhis.android.core.data.api.Fields;
import org.hisp.dhis.android.core.option.OptionSet;

public final class TrackedEntityAttributeFields {

    static final String PATTERN = "pattern";
    static final String SORT_ORDER_IN_LIST_NO_PROGRAM = "sortOrderInListNoProgram";
    static final String OPTION_SET = "optionSet";
    static final String VALUE_TYPE = "valueType";
    static final String EXPRESSION = "expression";
    static final String SEARCH_SCOPE = "searchScope";
    static final String PROGRAM_SCOPE = "programScope";
    static final String DISPLAY_IN_LIST_NO_PROGRAM = "displayInListNoProgram";
    static final String GENERATED = "generated";
    static final String DISPLAY_ON_VISIT_SCHEDULE = "displayOnVisitSchedule";
    static final String ORG_UNIT_SCOPE = "orgunitScope";
    static final String UNIQUE = "unique";
    static final String INHERIT = "inherit";
    private static final String STYLE = "style";
    private static final String RENDER_TYPE = "renderType";
    private static final String ACCESS = "access";

    private static final FieldsHelper<TrackedEntityAttribute> fh = new FieldsHelper<>();

    public static final Fields<TrackedEntityAttribute> allFields = Fields.<TrackedEntityAttribute>builder()
            .fields(fh.getNameableFields())
            .fields(
                    fh.<String>field(PATTERN),
                    fh.<String>field(SORT_ORDER_IN_LIST_NO_PROGRAM),
                    fh.<ValueType>field(VALUE_TYPE),
                    fh.<String>field(EXPRESSION),
                    fh.<TrackedEntityAttributeSearchScope>field(SEARCH_SCOPE),
                    fh.<Boolean>field(PROGRAM_SCOPE),
                    fh.<Boolean>field(DISPLAY_IN_LIST_NO_PROGRAM),
                    fh.<Boolean>field(GENERATED),
                    fh.<Boolean>field(DISPLAY_ON_VISIT_SCHEDULE),
                    fh.<Boolean>field(ORG_UNIT_SCOPE),
                    fh.<Boolean>field(UNIQUE),
                    fh.<Boolean>field(INHERIT),
                    fh.<OptionSet>nestedFieldWithUid(OPTION_SET),
                    fh.<ObjectStyle>nestedField(STYLE).with(ObjectStyleFields.allFields),
                    fh.<ValueTypeRendering>nestedField(RENDER_TYPE),
                    fh.<Access>nestedField(ACCESS).with(Access.read)
            ).build();

    private TrackedEntityAttributeFields() {
    }
}