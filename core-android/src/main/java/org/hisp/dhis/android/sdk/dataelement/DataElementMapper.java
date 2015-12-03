/*
 * Copyright (c) 2015, University of Oslo
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

package org.hisp.dhis.android.sdk.dataelement;

import org.hisp.dhis.android.sdk.common.base.AbsMapper;
import org.hisp.dhis.android.sdk.flow.DataElement$Flow;
import org.hisp.dhis.java.sdk.models.dataelement.DataElement;

public class DataElementMapper extends AbsMapper<DataElement, DataElement$Flow> {

    public DataElementMapper() {
        // empty constructor
    }

    @Override
    public DataElement$Flow mapToDatabaseEntity(DataElement dataElement) {
        if (dataElement == null) {
            return null;
        }

        DataElement$Flow dataElementFlow = new DataElement$Flow();
        dataElementFlow.setId(dataElement.getId());
        dataElementFlow.setUId(dataElement.getUId());
        dataElementFlow.setCreated(dataElement.getCreated());
        dataElementFlow.setLastUpdated(dataElement.getLastUpdated());
        dataElementFlow.setName(dataElement.getName());
        dataElementFlow.setDisplayName(dataElement.getDisplayName());
        dataElementFlow.setAccess(dataElement.getAccess());
        dataElementFlow.setType(dataElement.getType());
        dataElementFlow.setZeroIsSignificant(dataElement.isZeroIsSignificant());
        dataElementFlow.setAggregationOperator(dataElement.getAggregationOperator());
        dataElementFlow.setFormName(dataElement.getFormName());
        dataElementFlow.setNumberType(dataElement.getNumberType());
        dataElementFlow.setDomainType(dataElement.getDomainType());
        dataElementFlow.setDimension(dataElement.getDimension());
        dataElementFlow.setDisplayFormName(dataElement.getDisplayFormName());
        dataElementFlow.setOptionSet(dataElement.getOptionSet());
        return dataElementFlow;
    }

    @Override
    public DataElement mapToModel(DataElement$Flow dataElementFlow) {
        if (dataElementFlow == null) {
            return null;
        }

        DataElement dataElement = new DataElement();
        dataElement.setId(dataElementFlow.getId());
        dataElement.setUId(dataElementFlow.getUId());
        dataElement.setCreated(dataElementFlow.getCreated());
        dataElement.setLastUpdated(dataElementFlow.getLastUpdated());
        dataElement.setName(dataElementFlow.getName());
        dataElement.setDisplayName(dataElementFlow.getDisplayName());
        dataElement.setAccess(dataElementFlow.getAccess());
        dataElement.setType(dataElementFlow.getType());
        dataElement.setZeroIsSignificant(dataElementFlow.isZeroIsSignificant());
        dataElement.setAggregationOperator(dataElementFlow.getAggregationOperator());
        dataElement.setFormName(dataElementFlow.getFormName());
        dataElement.setNumberType(dataElementFlow.getNumberType());
        dataElement.setDomainType(dataElementFlow.getDomainType());
        dataElement.setDimension(dataElementFlow.getDimension());
        dataElement.setDisplayFormName(dataElementFlow.getDisplayFormName());
        dataElement.setOptionSet(dataElementFlow.getOptionSet());
        return dataElement;
    }

    @Override
    public Class<DataElement> getModelTypeClass() {
        return DataElement.class;
    }

    @Override
    public Class<DataElement$Flow> getDatabaseEntityTypeClass() {
        return DataElement$Flow.class;
    }
}
