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
package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.entity.Workflow;
import com.blazebit.persistence.model.DocumentViewModel;
import static com.googlecode.catchexception.CatchException.verifyException;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class PaginationTest extends AbstractCoreTest {

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("doc1");
            Document doc2 = new Document("Doc2");
            Document doc3 = new Document("doC3");
            Document doc4 = new Document("dOc4");
            Document doc5 = new Document("DOC5");
            Document doc6 = new Document("bdoc");
            Document doc7 = new Document("adoc");

            Person o1 = new Person("Karl1");
            Person o2 = new Person("Karl2");
            Person o3 = new Person("Moritz");
            o1.getLocalized().put(1, "abra kadabra");
            o2.getLocalized().put(1, "ass");

            doc1.setOwner(o1);
            doc2.setOwner(o1);
            doc3.setOwner(o1);
            doc4.setOwner(o2);
            doc5.setOwner(o2);
            doc6.setOwner(o2);
            doc7.setOwner(o2);

            doc1.getContacts().put(1, o1);
            doc1.getContacts().put(2, o2);

            doc4.getContacts().put(1, o3);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);
            em.persist(doc4);
            em.persist(doc5);
            em.persist(doc6);
            em.persist(doc7);

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void simpleTest() {
        CriteriaBuilder<DocumentViewModel> crit = cbf.create(em, Document.class, "d")
                .selectNew(DocumentViewModel.class)
                .with("d.name")
                .with("CONCAT(d.owner.name, ' user')")
                .with("COALESCE(d.owner.localized[1],'no item')")
                .with("d.owner.partnerDocument.name")
                .end();
        crit.where("d.name").like(false).value("doc%").noEscape();
        crit.where("d.owner.name").like().value("%arl%").noEscape();
        crit.where("d.owner.localized[1]").like(false).value("a%").noEscape();
        crit.orderByAsc("d.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN owner_1.localized localized_1_1 "
                + ON_CLAUSE + " KEY(localized_1_1) = 1 "
                + "WHERE UPPER(d.name) LIKE UPPER(:param_0) AND owner_1.name LIKE :param_1 AND UPPER(" + joinAliasValue("localized_1_1")
                + ") LIKE UPPER(:param_2)";

        // limit this query using setFirstResult() and setMaxResult() according to the parameters passed to page()
        String expectedIdQuery = "SELECT d.id FROM Document d JOIN d.owner owner_1 LEFT JOIN owner_1.localized localized_1_1 "
                + ON_CLAUSE + " KEY(localized_1_1) = 1 "
                + "WHERE UPPER(d.name) LIKE UPPER(:param_0) AND owner_1.name LIKE :param_1 AND UPPER(" + joinAliasValue("localized_1_1")
                + ") LIKE UPPER(:param_2) "
                + "GROUP BY d.id "
                + "ORDER BY d.id ASC NULLS LAST";

        String expectedObjectQuery = "SELECT d.name, CONCAT(owner_1.name,' user'), COALESCE(" + joinAliasValue("localized_1_1")
                + ",'no item'), partnerDocument_1.name FROM Document d "
                + "JOIN d.owner owner_1 LEFT JOIN owner_1.localized localized_1_1 " + ON_CLAUSE
                + " KEY(localized_1_1) = 1 LEFT JOIN owner_1.partnerDocument partnerDocument_1 "
                + "WHERE d.id IN :ids "
                + "ORDER BY d.id ASC NULLS LAST";

        PaginatedCriteriaBuilder<DocumentViewModel> pcb = crit.page(0, 2);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<DocumentViewModel> result = pcb.getResultList();
        assertEquals(2, result.size());
        assertEquals(5, result.getTotalSize());
        assertEquals("doc1", result.get(0).getName());
        assertEquals("Doc2", result.get(1).getName());

        result = crit.page(2, 2).getResultList();
        assertEquals("doC3", result.get(0).getName());
        assertEquals("dOc4", result.get(1).getName());

        result = crit.page(4, 2).getResultList();
        assertEquals(result.size(), 1);
        assertEquals("DOC5", result.get(0).getName());
    }

    @Test
    public void testSelectIndexedWithParameter() {
        String expectedCountQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d JOIN d.owner owner_1 WHERE owner_1.name = :param_0";
        String expectedIdQuery = "SELECT d.id FROM Document d JOIN d.owner owner_1 WHERE owner_1.name = :param_0 GROUP BY d.id ORDER BY d.id ASC NULLS LAST";
        String expectedObjectQuery = "SELECT contacts_contactNr_1.name FROM Document d LEFT JOIN d.contacts contacts_contactNr_1 " + ON_CLAUSE
                + " KEY(contacts_contactNr_1) = :contactNr WHERE d.id IN :ids ORDER BY d.id ASC NULLS LAST";
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .where("owner.name").eq("Karl1")
                .select("contacts[:contactNr].name")
                .orderByAsc("id")
                .page(0, 1);
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.setParameter("contactNr", 1).getResultList();
    }

    @Test
    public void testSelectEmptyResultList() {
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .where("name").isNull()
                .orderByAsc("name")
                .orderByAsc("id")
                .page(0, 1);
        assertEquals(0, cb.getResultList().size());
        cb.getResultList();
    }

    @Test
    public void testPaginationWithReferenceObject() {
        // TODO: Maybe report that EclipseLink can't seem to handle subqueries in functions
        Document reference = cbf.create(em, Document.class).where("name").eq("adoc").getSingleResult();
        String expectedCountQuery =
                "SELECT COUNT(DISTINCT d.id), "
                + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                        + "FROM Document _page_position_d "
                        + "GROUP BY _page_position_d.id, _page_position_d.name "
                        + "ORDER BY _page_position_d.name ASC NULLS LAST, _page_position_d.id ASC NULLS LAST)",
                        ":_entityPagePositionParameter")
                    + " "
                + "FROM Document d";
        
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .orderByAsc("name")
                .orderByAsc("id")
                .page(reference.getId(), 1);
        
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        PagedList<Document> list = cb.getResultList();
        assertEquals(2, list.getFirstResult());
        assertEquals(3, list.getPage());
        assertEquals(7, list.getTotalPages());
        assertEquals(7, list.getTotalSize());
        assertEquals(1, list.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPaginationWithInvalidReferenceObject() {
        cbf.create(em, Document.class, "d").page("test", 1);
    }

    @Test
    public void testPaginationWithNotExistingReferenceObject() {
        // TODO: Maybe report that EclipseLink can't seem to handle subqueries in functions
        Document reference = cbf.create(em, Document.class).where("name").eq("adoc").getSingleResult();
        String expectedCountQuery =
                "SELECT COUNT(DISTINCT d.id), "
                + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                        + "FROM Document _page_position_d "
                        + "WHERE _page_position_d.name <> :param_0 "
                        + "GROUP BY _page_position_d.id, _page_position_d.name "
                        + "ORDER BY _page_position_d.name ASC NULLS LAST, _page_position_d.id ASC NULLS LAST)",
                        ":_entityPagePositionParameter")
                    + " "
                + "FROM Document d "
                + "WHERE d.name <> :param_0";
        
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .where("name").notEq("adoc")
                .orderByAsc("name")
                .orderByAsc("id")
                .page(reference.getId(), 1);
        PaginatedCriteriaBuilder<Document> firstPageCb = cbf.create(em, Document.class, "d")
                .where("name").notEq("adoc")
                .orderByAsc("name")
                .orderByAsc("id")
                .page(0, 1);
        
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        PagedList<Document> expectedList = firstPageCb.getResultList();
        PagedList<Document> list = cb.getResultList();
        assertEquals(expectedList, list);
        
        assertEquals(-1, list.getFirstResult());
        assertEquals(1, list.getPage());
        assertEquals(6, list.getTotalPages());
        assertEquals(6, list.getTotalSize());
        assertEquals(1, list.size());
    }

    @Test
    public void testPaginatedWithGroupBy1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("COUNT(contacts.id)").groupBy("id");
        verifyException(cb, IllegalStateException.class).page(0, 1);
    }

    @Test
    public void testPaginatedWithGroupBy2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("COUNT(contacts.id)");
        cb.page(0, 1);
        try {
            cb.groupBy("id");
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // OK, we expected that
        }
    }

    @Test
    public void testPaginatedWithGroupBy3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("COUNT(contacts.id)");
        cb.page(0, 1);
        try {
            cb.groupBy("id", "name");
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // OK, we expected that
        }
    }

    @Test
    public void testPaginatedWithDistinct1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("COUNT(contacts.id)").distinct();
        verifyException(cb, IllegalStateException.class).page(0, 1);
    }

    @Test
    public void testPaginatedWithDistinct2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("COUNT(contacts.id)");
        cb.page(0, 1);
        try {
            cb.distinct();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // OK, we expected that
        }
    }

    @Test
    public void testOrderByExpression() {
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .orderByAsc("contacts[:contactNr].name")
                .orderByAsc("id")
                .setParameter("contactNr", 1)
                .page(0, 1);
        String expectedIdQuery = "SELECT d.id FROM Document d LEFT JOIN d.contacts contacts_contactNr_1 " + ON_CLAUSE
                + " KEY(contacts_contactNr_1) = :contactNr GROUP BY d.id, contacts_contactNr_1.name ORDER BY contacts_contactNr_1.name ASC NULLS LAST, d.id ASC NULLS LAST";
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        // TODO: enable as soon as #45 is fixed
//        cb.getResultList();
    }

    @Test
    public void testOrderBySelectAlias() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("contacts[:contactNr].name", "contactName")
                .orderByAsc("contactName")
                .orderByAsc("id")
                .setParameter("contactNr", 1)
                .page(0, 1);
        String expectedIdQuery = "SELECT d.id FROM Document d LEFT JOIN d.contacts contacts_contactNr_1 " + ON_CLAUSE
                + " KEY(contacts_contactNr_1) = :contactNr GROUP BY d.id, contacts_contactNr_1.name ORDER BY contacts_contactNr_1.name ASC NULLS LAST, d.id ASC NULLS LAST";
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        // TODO: enable as soon as #45 is fixed
        //cb.getResultList();
    }

    @Test
    public void testOrderBySubquery() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectSubquery("contactCount")
                .from(Document.class, "d2")
                .select("COUNT(d2.contacts.id)")
                .where("d2.id").eqExpression("d.id")
                .end()
                .orderByAsc("contactCount")
                .orderByAsc("id")
                .page(0, 1);
        String expectedIdQuery = "SELECT d.id, (SELECT COUNT(contacts_1.id) FROM Document d2 LEFT JOIN d2.contacts contacts_1 WHERE d2.id = d.id) AS contactCount FROM Document d GROUP BY d.id, (SELECT COUNT(contacts_1.id) FROM Document d2 LEFT JOIN d2.contacts contacts_1 WHERE d2.id = d.id) ORDER BY contactCount ASC NULLS LAST, d.id ASC NULLS LAST";
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        cb.getResultList();
    }

    // TODO: don't know what to do with this, order by should only allow simple paths as of #72 so maybe turn that into a negative test
    @Ignore
    @Test
    public void testOrderBySize() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)")
                .orderByAsc("SIZE(d.contacts)")
                .orderByAsc("id")
                .page(0, 1);
        String expectedIdQuery = "SELECT d.id FROM Document d GROUP BY d.id ORDER BY SIZE(d.contacts) ASC NULLS LAST, d.id ASC NULLS LAST";
        String expectedCountQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d";
        String expectedObjectQuery = "SELECT COUNT(contacts_1) FROM Document d LEFT JOIN d.contacts contacts_1 WHERE d.id IN :ids GROUP BY d.id ORDER BY SIZE(d.contacts) ASC NULLS LAST, d.id ASC NULLS LAST";

        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testOrderBySizeAlias() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)", "contactCount")
                .orderByAsc("contactCount")
                .orderByAsc("id")
                .page(0, 1);
        String expectedIdQuery = "SELECT d.id, COUNT(contacts_1) AS contactCount FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY d.id ORDER BY contactCount ASC NULLS LAST, d.id ASC NULLS LAST";
        String expectedCountQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d";
        String expectedObjectQuery = "SELECT COUNT(contacts_1) AS contactCount FROM Document d LEFT JOIN d.contacts contacts_1 WHERE d.id IN :ids GROUP BY d.id ORDER BY contactCount ASC NULLS LAST, d.id ASC NULLS LAST";

        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testSelectOnlyPropagationForWithJoins1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d");
        PaginatedCriteriaBuilder<Tuple> pcb = cb.select("d.contacts[d.owner.age]").where("d.contacts").isNull().orderByAsc("id").page(0, 1);

        String expectedCountQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d LEFT JOIN d.contacts contacts_1 WHERE contacts_1 IS NULL";
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        pcb.getPageCountQueryString();
        cb.getResultList();
    }

    @Test
    public void testSelectOnlyPropagationForWithJoins2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d");
        PaginatedCriteriaBuilder<Tuple> pcb = cb
                .select("c")
                .leftJoinOn("d.contacts", "c")
                .on("KEY(c)").eqExpression("d.owner.age")
                .end()
                .where("c").isNull().orderByAsc("id").page(0, 1);

        String expectedCountQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d JOIN d.owner owner_1 LEFT JOIN d.contacts c " + ON_CLAUSE + " KEY(c) = owner_1.age WHERE c IS NULL";
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testCountQueryWhereClauseConjuncts() {
        // TODO: Maybe report that EclipseLink has a bug in case when rendering
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w");
        PaginatedCriteriaBuilder<Tuple> pcb = cb
            .select("w.id", "Workflow_id")
            .select("w.defaultLanguage", "Workflow_defaultLanguage")
            .select("COALESCE(NULLIF(COALESCE(w.localized[:language].name, w.localized[w.defaultLanguage].name), ''), ' - ')", "Workflow_name")
            .select("COALESCE(NULLIF(COALESCE(w.localized[:language].description, w.localized[w.defaultLanguage].description), ''), ' - ')", "Workflow_description")
            .select("COALESCE(NULLIF(SUBSTRING(COALESCE(w.localized[:language].description, w.localized[w.defaultLanguage].description), 1, 20), ''), ' - ')", "Workflow_descriptionPreview")
            .select("CASE WHEN w.localized[:language].name IS NULL THEN 0 ELSE 1 END", "Workflow_localizedValue")
            .orderByAsc("Workflow_name")
            .orderByAsc("Workflow_id")
            .page(0, 1);

        String expectedCountQuery = "SELECT COUNT(DISTINCT w.id) FROM Workflow w";

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        pcb.getPageCountQueryString();
        cb.setParameter("language", Locale.GERMAN).getResultList();
    }
    
    @Test
    public void testPaginationWithoutOrderBy(){
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d").page(0, 10);
        verifyException(cb, IllegalStateException.class).getResultList();
    }
    
    @Test
    public void testPaginationWithoutUniqueLastOrderBy(){
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .orderByAsc("d.id").orderByAsc("d.name").page(0, 10);
        verifyException(cb, IllegalStateException.class).getResultList();
    }
    
    @Test
    public void testPaginationObjectQueryClauseExclusions(){
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("id")
                .innerJoinDefault("contacts", "c")
                .where("c.name").eq("Karl1")
                .orderByAsc("d.id").page(0, 10);
        String query = "SELECT d.id FROM Document d JOIN d.contacts c WHERE d.id IN :ids ORDER BY d.id ASC NULLS LAST";
        assertEquals(query, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testPaginationWithExplicitRestrictingJoin(){
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("id").select("c.name")
                .innerJoinDefaultOn("contacts", "c")
                    .on("KEY(c)").eqExpression("1")
                .end()
                .orderByAsc("d.id")
                .page(0, 10);
        
        String countQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d JOIN d.contacts c " + ON_CLAUSE + " KEY(c) = 1";
        String idQuery = "SELECT d.id FROM Document d JOIN d.contacts c " + ON_CLAUSE + " KEY(c) = 1 GROUP BY d.id ORDER BY d.id ASC NULLS LAST";
        String objectQuery = "SELECT d.id, c.name FROM Document d JOIN d.contacts c " + ON_CLAUSE + " KEY(c) = 1 WHERE d.id IN :ids ORDER BY d.id ASC NULLS LAST";
        assertEquals(countQuery, cb.getPageCountQueryString());
        assertEquals(idQuery, cb.getPageIdQueryString());
        assertEquals(objectQuery, cb.getQueryString());
        cb.getResultList();
    }
}
