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
package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.parser.JPQLSelectExpressionLexer;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author cpbec
 */
public class ParserPerformanceTest {
    
    private static Level originalLevel;
    
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();
    
    @BeforeClass
    public static void beforeClass() {
        Logger log = Logger.getLogger("com.blazebit.persistence.parser");
        originalLevel = log.getLevel();
        log.setLevel(Level.OFF);
    }
    
    @AfterClass
    public static void afterClass() {
        Logger log = Logger.getLogger("com.blazebit.persistence.parser");
        log.setLevel(originalLevel);
    }
    
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 5)
    @Test
    public void testSmallExpression() {
        String expression = "CONCAT(COALESCE(CONCAT(NULLIF(CONCAT(CASE WHEN LENGTH(COALESCE(buyerParty.zip,'')) > 0 OR LENGTH(COALESCE(buyerParty.city,'')) > 0 THEN COALESCE(CONCAT(NULLIF(buyerParty.street,''),', '),'') ELSE COALESCE(buyerParty.street,'') END,CASE WHEN LENGTH(COALESCE(buyerParty.city,'')) > 0 THEN COALESCE(CONCAT(NULLIF(buyerParty.zip,''),' '),'') ELSE COALESCE(buyerParty.zip,'') END,COALESCE(buyerParty.city,'')),''),' - '),''),CASE WHEN LENGTH(COALESCE(buyerParty.region.name,'')) > 0 THEN COALESCE(CONCAT(COALESCE(NULLIF(buyerParty.countryEntry.name,''),NULLIF(buyerParty.country,'')),' / '),'') ELSE COALESCE(buyerParty.countryEntry.name,buyerParty.country,'') END,COALESCE(buyerParty.region.name,''))";
        doTest(expression);
    }
    
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 5)
    @Test
    public void testLargeExpression() {
        String expression = "CONCAT(CONCAT(buyerParty.name,CASE WHEN LENGTH(COALESCE(CONCAT(COALESCE(CONCAT(NULLIF(CONCAT(CASE WHEN LENGTH(COALESCE(buyerParty.zip,'')) > 0 OR LENGTH(COALESCE(buyerParty.city,'')) > 0 THEN COALESCE(CONCAT(NULLIF(buyerParty.street,''),', '),'') ELSE COALESCE(buyerParty.street,'') END,CASE WHEN LENGTH(COALESCE(buyerParty.city,'')) > 0 THEN COALESCE(CONCAT(NULLIF(buyerParty.zip,''),' '),'') ELSE COALESCE(buyerParty.zip,'') END,COALESCE(buyerParty.city,'')),''),' - '),''),CASE WHEN LENGTH(COALESCE(buyerParty.region.name,'')) > 0 THEN COALESCE(CONCAT(COALESCE(NULLIF(buyerParty.countryEntry.name,''),NULLIF(buyerParty.country,'')),' / '),'') ELSE COALESCE(buyerParty.countryEntry.name,buyerParty.country,'') END,COALESCE(buyerParty.region.name,'')),'')) > 0 THEN ': ' ELSE '' END),COALESCE(CONCAT(COALESCE(CONCAT(NULLIF(CONCAT(CASE WHEN LENGTH(COALESCE(buyerParty.zip,'')) > 0 OR LENGTH(COALESCE(buyerParty.city,'')) > 0 THEN COALESCE(CONCAT(NULLIF(buyerParty.street,''),', '),'') ELSE COALESCE(buyerParty.street,'') END,CASE WHEN LENGTH(COALESCE(buyerParty.city,'')) > 0 THEN COALESCE(CONCAT(NULLIF(buyerParty.zip,''),' '),'') ELSE COALESCE(buyerParty.zip,'') END,COALESCE(buyerParty.city,'')),''),' - '),''),CASE WHEN LENGTH(COALESCE(buyerParty.region.name,'')) > 0 THEN COALESCE(CONCAT(COALESCE(NULLIF(buyerParty.countryEntry.name,''),NULLIF(buyerParty.country,'')),' / '),'') ELSE COALESCE(buyerParty.countryEntry.name,buyerParty.country,'') END,COALESCE(buyerParty.region.name,'')),''))";
        doTest(expression);
    }
    
    private void doTest(String expression) {
        JPQLSelectExpressionLexer l = new JPQLSelectExpressionLexer(new ANTLRInputStream(expression));
        CommonTokenStream tokens = new CommonTokenStream(l);
        JPQLSelectExpressionParser p = new JPQLSelectExpressionParser(tokens, true);
        ParserRuleContext ctx = p.parseSimpleExpression();
    }
}
