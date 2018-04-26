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
package org.hisp.dhis.android.core.user;

import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import static org.hisp.dhis.android.core.utils.Utils.isDeleted;

public class UserHandler {
    private final UserStore userStore;

    UserHandler(UserStore userStore) {
        this.userStore = userStore;
    }

    public void handleUser(User user) {
        if (user == null) {
            return;
        }

        deleteOrPersistUser(user);
    }

    private void deleteOrPersistUser(User user) {
        if (isDeleted(user)) {
            userStore.delete(user.uid());
        } else {
            int updatedRow = userStore.update(user.uid(), user.code(), user.name(), user.displayName(),
                    user.created(), user.lastUpdated(), user.birthday(), user.education(),
                    user.gender(), user.jobTitle(), user.surname(), user.firstName(),
                    user.introduction(), user.employer(), user.interests(), user.languages(),
                    user.email(), user.phoneNumber(), user.nationality(), user.uid());

            if (updatedRow <= 0) {
                        userStore.insert(user.uid(), user.code(), user.name(), user.displayName(), user.created(),
                        user.lastUpdated(), user.birthday(), user.education(),
                        user.gender(), user.jobTitle(), user.surname(), user.firstName(),
                        user.introduction(), user.employer(), user.interests(), user.languages(),
                        user.email(), user.phoneNumber(), user.nationality());
            }
        }
    }

    static UserHandler create(DatabaseAdapter databaseAdapter) {
        return new UserHandler(new UserStoreImpl(databaseAdapter));
    }
}
