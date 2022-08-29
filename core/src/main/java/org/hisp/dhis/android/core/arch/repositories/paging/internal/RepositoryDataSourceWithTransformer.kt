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
package org.hisp.dhis.android.core.arch.repositories.paging.internal

import androidx.paging.ItemKeyedDataSource
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.OrderByClauseBuilder
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder
import org.hisp.dhis.android.core.arch.db.stores.internal.ReadableStore
import org.hisp.dhis.android.core.arch.handlers.internal.TwoWayTransformer
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppenderExecutor
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.arch.repositories.scope.internal.WhereClauseFromScopeBuilder
import org.hisp.dhis.android.core.common.CoreObject

class RepositoryDataSourceWithTransformer<M : CoreObject, T : Any>(
        private val store: ReadableStore<M>,
        private val scope: RepositoryScope,
        private val childrenAppenders: Map<String, ChildrenAppender<M>>,
        private val transformer: TwoWayTransformer<M, T>
) : ItemKeyedDataSource<M, T>() {


    override fun loadInitial(params: LoadInitialParams<M>, callback: LoadInitialCallback<T>) {
        val whereClause = WhereClauseFromScopeBuilder(WhereClauseBuilder()).getWhereClause(scope)
        val withoutChildren = store.selectWhere(whereClause,
                OrderByClauseBuilder.orderByFromItems(scope.orderBy(), scope.pagingKey()), params.requestedLoadSize)
        callback.onResult(appendChildren(withoutChildren))
    }

    override fun loadAfter(params: LoadParams<M>, callback: LoadCallback<T>) {
        loadPages(params, callback, false)
    }

    override fun loadBefore(params: LoadParams<M>, callback: LoadCallback<T>) {
        loadPages(params, callback, true)
    }

    private fun loadPages(params: LoadParams<M>, callback: LoadCallback<T>, reversed: Boolean) {
        val whereClauseBuilder = WhereClauseBuilder()
        OrderByClauseBuilder.addSortingClauses(whereClauseBuilder, scope.orderBy(),
                params.key.toContentValues(), reversed, scope.pagingKey())
        val whereClause = WhereClauseFromScopeBuilder(whereClauseBuilder).getWhereClause(scope)
        val withoutChildren = store.selectWhere(whereClause,
                OrderByClauseBuilder.orderByFromItems(scope.orderBy(), scope.pagingKey()),
                params.requestedLoadSize)
        callback.onResult(appendChildren(withoutChildren))
    }

    override fun getKey(item: T): M {
        return transformer.deTransform(item)
    }

    private fun appendChildren(withoutChildren: List<M>): List<T> {
        return ChildrenAppenderExecutor.appendInObjectCollection(
                withoutChildren, childrenAppenders, scope.children()).map { transformer.transform(it) }
    }
}