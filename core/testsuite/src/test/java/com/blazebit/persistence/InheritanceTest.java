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

import com.blazebit.persistence.entity.JuniorProjectLeader;
import com.blazebit.persistence.entity.LargeProject;
import com.blazebit.persistence.entity.Project;
import com.blazebit.persistence.entity.ProjectLeader;
import com.blazebit.persistence.entity.SeniorProjectLeader;
import com.blazebit.persistence.entity.SmallProject;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class InheritanceTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            ProjectLeader.class,
            JuniorProjectLeader.class,
            SeniorProjectLeader.class,
            Project.class,
            SmallProject.class,
            LargeProject.class
        };
    }
    
    @Test
    public void testInheritanceWithEntityName() {
        CriteriaBuilder<Project> cb = cbf.create(em, Project.class, "p");
        String expectedQuery = "SELECT p FROM Projects p";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testJoinPolymorphicEntity() {
        CriteriaBuilder<Project> cb = cbf.create(em, Project.class, "p")
                .leftJoinFetch("leader", "l");
        String expectedQuery = "SELECT p FROM Projects p LEFT JOIN FETCH p.leader l";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testImplicitJoinPolymorphicEntity() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Project.class, "p")
                .select("leader.id");
        String expectedQuery = "SELECT p.leader.id FROM Projects p";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
