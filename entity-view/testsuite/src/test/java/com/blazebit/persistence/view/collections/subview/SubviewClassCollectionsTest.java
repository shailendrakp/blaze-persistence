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
package com.blazebit.persistence.view.collections.subview;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.collections.entity.DocumentForCollections;
import com.blazebit.persistence.view.collections.entity.PersonForCollections;
import static com.blazebit.persistence.view.collections.subview.SubviewAssert.assertSubviewEquals;
import com.blazebit.persistence.view.collections.subview.model.SubviewClassDocumentForCollectionsView;
import com.blazebit.persistence.view.collections.subview.model.SubviewPersonForCollectionsView;
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
public class SubviewClassCollectionsTest extends AbstractEntityViewTest {

    private final String viewConstructorName;

    private DocumentForCollections doc1;
    private DocumentForCollections doc2;

    public SubviewClassCollectionsTest(String viewConstructorName) {
        this.viewConstructorName = viewConstructorName;
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

    @Parameterized.Parameters(name = "{0}")
    public static Collection entityViewCombinations() {
        return Arrays.asList(new Object[][]{
            { SubviewClassDocumentForCollectionsView.STRING_MAP_SET_LIST_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.STRING_MAP_LIST_SET_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.STRING_SET_MAP_LIST_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.STRING_SET_LIST_MAP_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.STRING_LIST_SET_MAP_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.STRING_LIST_MAP_SET_CONSTRUCTOR },

            { SubviewClassDocumentForCollectionsView.MAP_SET_STRING_LIST_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.MAP_SET_LIST_STRING_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.MAP_LIST_SET_STRING_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.MAP_LIST_STRING_SET_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.MAP_STRING_SET_LIST_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.MAP_STRING_LIST_SET_CONSTRUCTOR },

            { SubviewClassDocumentForCollectionsView.LIST_MAP_STRING_SET_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.LIST_MAP_SET_STRING_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.LIST_SET_MAP_STRING_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.LIST_SET_STRING_MAP_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.LIST_STRING_SET_MAP_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.LIST_STRING_MAP_SET_CONSTRUCTOR },

            { SubviewClassDocumentForCollectionsView.SET_MAP_LIST_STRING_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.SET_MAP_STRING_LIST_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.SET_LIST_MAP_STRING_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.SET_LIST_STRING_MAP_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.SET_STRING_LIST_MAP_CONSTRUCTOR },
            { SubviewClassDocumentForCollectionsView.SET_STRING_MAP_LIST_CONSTRUCTOR }
        });
    }

    @Test
    public void testCollections() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(SubviewClassDocumentForCollectionsView.class);
        cfg.addEntityView(SubviewPersonForCollectionsView.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        CriteriaBuilder<DocumentForCollections> criteria = cbf.create(em, DocumentForCollections.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<SubviewClassDocumentForCollectionsView> cb = evm.applySetting(EntityViewSetting.create(SubviewClassDocumentForCollectionsView.class, viewConstructorName), criteria);
        List<SubviewClassDocumentForCollectionsView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertSubviewEquals(doc1.getContacts(), results.get(0).getContacts());
        assertSubviewEquals(doc1.getPartners(), results.get(0).getPartners());
        assertSubviewEquals(doc1.getPersonList(), results.get(0).getPersonList());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertSubviewEquals(doc2.getContacts(), results.get(1).getContacts());
        assertSubviewEquals(doc2.getPartners(), results.get(1).getPartners());
        assertSubviewEquals(doc2.getPersonList(), results.get(1).getPersonList());
    }
}
