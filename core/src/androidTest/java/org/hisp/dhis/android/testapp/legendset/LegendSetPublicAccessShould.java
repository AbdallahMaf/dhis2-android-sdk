package org.hisp.dhis.android.testapp.legendset;

import android.support.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.legendset.LegendSet;
import org.hisp.dhis.android.testapp.arch.BasePublicAccessShould;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(AndroidJUnit4.class)
public class LegendSetPublicAccessShould extends BasePublicAccessShould<LegendSet> {

    @Mock
    private LegendSet object;

    @Override
    public LegendSet object() {
        return object;
    }

    @Override
    public void has_public_create_method() {
        LegendSet.create(null);
    }

    @Override
    public void has_public_builder_method() {
        LegendSet.builder();
    }

    @Override
    public void has_public_to_builder_method() {
        object().toBuilder();
    }
}