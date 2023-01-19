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
package org.hisp.dhis.android.core.parser.internal.service.utils

import java.lang.NumberFormatException
import org.hisp.dhis.android.core.datavalue.DataValue
import org.hisp.dhis.android.core.parser.internal.service.dataobject.DataElementObject
import org.hisp.dhis.android.core.parser.internal.service.dataobject.DataElementOperandObject
import org.hisp.dhis.android.core.parser.internal.service.dataobject.DimensionalItemObject

internal object ExpressionHelper {

    @JvmStatic
    fun getValueMap(dataValues: List<DataValue>): Map<DimensionalItemObject, Double> {
        val valueMap: MutableMap<DimensionalItemObject, Double> = HashMap()
        for (dataValue in dataValues) {
            dataValue.dataElement()?.let { deId ->
                val dataElementItem: DimensionalItemObject = DataElementObject(deId)
                addDimensionalItemValueToMap(dataElementItem, dataValue.value(), valueMap)

                dataValue.categoryOptionCombo()?.let { cocId ->
                    val dataElementOperandItem: DimensionalItemObject = DataElementOperandObject(deId, cocId)
                    addDimensionalItemValueToMap(dataElementOperandItem, dataValue.value(), valueMap)
                }
            }
        }
        return valueMap
    }

    private fun addDimensionalItemValueToMap(
        item: DimensionalItemObject,
        value: String?,
        valueMap: MutableMap<DimensionalItemObject, Double>
    ) {
        try {
            value?.toDouble()?.let { newValue ->
                val existingValue = valueMap[item]
                val result = (existingValue ?: 0.0) + newValue
                valueMap[item] = result
            }
        } catch (e: NumberFormatException) {
            // Ignore non-numeric values
        }
    }
}
