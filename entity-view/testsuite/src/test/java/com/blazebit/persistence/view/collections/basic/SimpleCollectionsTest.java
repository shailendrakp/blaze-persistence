/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.view.collections.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.collections.basic.model.BasicDocumentCollectionsView;
import com.blazebit.persistence.view.collections.basic.model.BasicDocumentListMapSetView;
import com.blazebit.persistence.view.collections.basic.model.BasicDocumentListSetMapView;
import com.blazebit.persistence.view.collections.basic.model.BasicDocumentMapListSetView;
import com.blazebit.persistence.view.collections.basic.model.BasicDocumentMapSetListView;
import com.blazebit.persistence.view.collections.basic.model.BasicDocumentSetListMapView;
import com.blazebit.persistence.view.collections.basic.model.BasicDocumentSetMapListView;
import com.blazebit.persistence.view.collections.entity.DocumentForCollections;
import com.blazebit.persistence.view.collections.entity.PersonForCollections;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@RunWith(Parameterized.class)
public class SimpleCollectionsTest<T extends BasicDocumentCollectionsView> extends AbstractEntityViewTest {

    private final Class<T> viewType;

    private DocumentForCollections doc1;
    private DocumentForCollections doc2;

    public SimpleCollectionsTest(Class<T> viewType) {
        this.viewType = viewType;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            DocumentForCollections.class,
            PersonForCollections.class
        };
    }

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc1 = new DocumentForCollections("doc1");
            doc2 = new DocumentForCollections("doc2");

            PersonForCollections o1 = new PersonForCollections("pers1");
            PersonForCollections o2 = new PersonForCollections("pers2");
            PersonForCollections o3 = new PersonForCollections("pers3");
            PersonForCollections o4 = new PersonForCollections("pers4");
            o1.setPartnerDocument(doc1);
            o2.setPartnerDocument(doc2);
            o3.setPartnerDocument(doc1);
            o4.setPartnerDocument(doc2);

            doc1.setOwner(o1);
            doc2.setOwner(o2);

            doc1.getContacts().put(1, o1);
            doc2.getContacts().put(1, o2);
            doc1.getContacts().put(2, o3);
            doc2.getContacts().put(2, o4);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);
            em.persist(o4);

            doc1.getPartners().add(o1);
            doc1.getPartners().add(o3);
            doc2.getPartners().add(o2);
            doc2.getPartners().add(o4);

            doc1.getPersonList().add(o1);
            doc1.getPersonList().add(o2);
            doc2.getPersonList().add(o3);
            doc2.getPersonList().add(o4);

            em.persist(doc1);
            em.persist(doc2);

            em.flush();
            tx.commit();
            em.clear();

            doc1 = em.find(DocumentForCollections.class, doc1.getId());
            doc2 = em.find(DocumentForCollections.class, doc2.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Parameterized.Parameters
    public static Collection entityViewCombinations() {
        return Arrays.asList(new Object[][]{
            { BasicDocumentListMapSetView.class },
            { BasicDocumentListSetMapView.class },
            { BasicDocumentMapListSetView.class },
            { BasicDocumentMapSetListView.class },
            { BasicDocumentSetListMapView.class },
            { BasicDocumentSetMapListView.class }
        });
    }

    @Test
    public void testCollections() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(viewType);
        EntityViewManager evm = cfg.createEntityViewManager();

        CriteriaBuilder<DocumentForCollections> criteria = cbf.create(em, DocumentForCollections.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc1.getContacts(), results.get(0).getContacts());
        assertEquals(doc1.getPartners(), results.get(0).getPartners());
        assertEquals(doc1.getPersonList(), results.get(0).getPersonList());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(doc2.getContacts(), results.get(1).getContacts());
        assertEquals(doc2.getPartners(), results.get(1).getPartners());
        assertEquals(doc2.getPersonList(), results.get(1).getPersonList());
    }
}
