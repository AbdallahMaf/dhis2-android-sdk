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
package org.hisp.dhis.android.core.arch.repositories.`object`

import io.reactivex.Completable
import org.hisp.dhis.android.core.common.CoreObject
import org.hisp.dhis.android.core.maintenance.D2Error

interface ReadWriteObjectRepository<M : CoreObject> : ReadOnlyObjectRepository<M> {
    /**
     * Removes the object in scope in an asynchronous way. See the implementation JavaDoc for details on how deletion
     * is performed. It returns a `Completable` that completes as soon as the object is deleted in the database.
     * The `Completable` fails if the object doesn't exist.
     * @return the `Completable` which notifies the completion
     */
    fun delete(): Completable

    /**
     * Removes the object in scope in a synchronous way. See the implementation JavaDoc for details on how deletion
     * is performed. It blocks the thread and finishes as soon as the object is deleted in the database.
     * It throws an exception if the object doesn't exist.
     * @throws D2Error if any errors occur, including when the object doesn't exist.
     */
    @Throws(D2Error::class)
    fun blockingDelete()

    /**
     * Removes the object in scope in a synchronous way. See the implementation JavaDoc for details on how deletion
     * is performed. Unlike [.delete], it doesn't throw an exception if the object doesn't exist.
     * It returns a `Completable` that completes as soon as the object is deleted in the database.
     * @return the `Completable` which notifies the completion
     */
    fun deleteIfExist(): Completable

    /**
     * Removes the object in scope in an asynchronous way. See the implementation JavaDoc for details on how deletion
     * is performed. Unlike [.blockingDelete], it doesn't throw an exception if the object doesn't exist.
     * It blocks the thread and finishes as soon as the object is deleted in the database.
     */
    fun blockingDeleteIfExist()
}
