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
package org.hisp.dhis.android.core.arch.repositories.collection;

import org.hisp.dhis.android.core.arch.repositories.object.ReadOnlyObjectRepository;
import org.hisp.dhis.android.core.common.CoreObject;
import org.hisp.dhis.android.core.common.ObjectWithUidInterface;

import java.util.List;

import io.reactivex.Single;

public interface ReadOnlyWithUidAndTransformerCollectionRepository<M extends CoreObject & ObjectWithUidInterface, T>
        extends ReadOnlyWithTransformerCollectionRepository<M, T> {

    /**
     * Returns a new {@link ReadOnlyObjectRepository} whose scope is the one of the current repository plus the
     * equal filter applied to the uid. This method is equivalent to byUid().eq(uid).one().
     * @param uid to compare
     * @return the {@link ReadOnlyObjectRepository}
     */
    ReadOnlyObjectRepository<M> uid(String uid);

    /**
     * Get the list of uids of objects in scope in an asynchronous way, returning a {@code Single<List<String>>}.
     *
     * @return A {@code Single} object with the list of uids.
     */
    Single<List<String>> getUids();

    /**
     * Get the list of uids of objects in scope in a synchronous way. Important: this is a blocking method and it should
     * not be executed in the main thread. Consider the asynchronous version {@link #getUids()}.
     *
     * @return List of uids
     */
    List<String> blockingGetUids();

}