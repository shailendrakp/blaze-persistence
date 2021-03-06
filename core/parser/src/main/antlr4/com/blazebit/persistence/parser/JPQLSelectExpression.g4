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
grammar JPQLSelectExpression;

//TODO: add antlr issue to illustrate that import of grammar containing left-recursive rules leads to error in antlr 4.3 (arithmetic_term)
import JPQL_lexer;

@parser::members {
private boolean allowOuter = false;
private boolean allowCaseWhen = false;
public JPQLSelectExpressionParser(TokenStream input, boolean allowCaseWhen){
       this(input);
       this.allowCaseWhen = allowCaseWhen;
}      

}

parseOrderByClause : state_field_path_expression EOF
                   | single_element_path_expression EOF
                   | key_value_expression EOF
                   ;

parsePath : state_field_path_expression EOF
          | single_element_path_expression EOF
          ;

parseSimpleExpression
    : simple_expression EOF 
    ;

parseSimpleSubqueryExpression
@init{
      allowOuter = true;
}
    : simple_subquery_expression EOF
    ;

parseScalarExpression
    : scalar_expression;

parseArithmeticExpression
    : arithmetic_expression;

parseStringExpression
    : string_expression;

parseCaseOperandExpression
    : case_operand;

simple_expression : single_valued_path_expression
                  | scalar_expression 
                  | aggregate_expression
                  ;

simple_subquery_expression : scalar_expression
                           | aggregate_expression
                           | single_valued_path_expression
                           ;
 
key_value_expression : name=KEY '('collection_valued_path_expression')'
                     | name=VALUE '('collection_valued_path_expression')'
                     ;

qualified_identification_variable : name=ENTRY '('collection_valued_path_expression')' # EntryFunction
                                  | key_value_expression #KeyValueExpression
                                  ;

 single_valued_path_expression 
     : qualified_identification_variable
     | state_field_path_expression
     | single_element_path_expression
     ;
 
 general_path_start : general_path_element
                    ;

 simple_path_element : identifier
                     ;
  
 general_path_element : simple_path_element
                      | array_expression
                      ;
 
 //TODO: allow only in certain clauses??
 array_expression : simple_path_element '[' arithmetic_expression ']'
                  ;
      
 general_subpath : general_path_start('.'general_path_element)*
                 ;

 state_field_path_expression : path
                             | {allowOuter == true}? outer_expression
                             ;

 single_valued_object_path_expression : path
                                      | {allowOuter == true}? outer_expression
                                      ;
 
 path : general_subpath'.'general_path_element
      ;

 collection_valued_path_expression : single_element_path_expression 
                                   | path
                                   ;
 
 single_element_path_expression : general_path_start
                                ;

 aggregate_expression : funcname=( AVG | MAX | MIN | SUM | COUNT) '('(distinct=DISTINCT)? aggregate_argument ')'  # AggregateExpression
                      | funcname=COUNT '(' Star_operator ')' # CountStar
                      ;
 
 aggregate_argument : single_element_path_expression
                    | path
                    ;

 scalar_expression : arithmetic_expression
                   | string_expression 
                   | enum_expression 
                   | datetime_expression 
                   | boolean_expression 
                   | coalesce_expression 
                   | nullif_expression 
                   | entity_type_expression 
                   | case_expression
                   ;
 
 outer_expression : Outer_function '(' single_valued_path_expression  ')'
                  ;

 arithmetic_expression : arithmetic_term # ArithmeticExpressionTerm
                       | arithmetic_expression op=( '+' | '-' ) arithmetic_term # ArithmeticExpressionPlusMinus
                       ;

 arithmetic_term : arithmetic_factor # ArithmeticTermFactor
                 | term=arithmetic_term op=( '*' | '/' ) factor=arithmetic_factor # ArithmeticMultDiv
                 ;

 arithmetic_factor : signum=( '+' | '-' )? arithmetic_primary;

 arithmetic_primary : state_field_path_expression # ArithmeticPrimary
                    | single_element_path_expression # ArithmeticPrimary
                    | Numeric_literal # ArithmeticPrimary
                    | '('arithmetic_expression')' # ArithmeticPrimaryParanthesis
                    | Input_parameter # ArithmeticPrimary
                    | functions_returning_numerics # ArithmeticPrimary
                    | aggregate_expression # ArithmeticPrimary
                    | case_expression # ArithmeticPrimary
                    | function_invocation # ArithmeticPrimary
                    ;

 string_expression : state_field_path_expression 
                   | single_element_path_expression 
                   | string_literal 
                   | Input_parameter 
                   | functions_returning_strings 
                   | aggregate_expression 
                   | case_expression 
                   | function_invocation
                   ;

 datetime_expression : state_field_path_expression
                     | single_element_path_expression 
                     | Input_parameter 
                     | functions_returning_datetime 
                     | aggregate_expression 
                     | case_expression 
                     | function_invocation 
                     | literal_temporal
                     ;

 boolean_expression : state_field_path_expression 
                    | single_element_path_expression 
                    | Boolean_literal 
                    | Input_parameter 
                    | case_expression 
                    | function_invocation
                    ;

 enum_expression : state_field_path_expression 
                 | single_element_path_expression 
                 | enum_literal 
                 | Input_parameter 
                 | case_expression 
                 ;
 
 enum_literal : ENUM '(' path ')'
              ;

 entity_expression : single_valued_object_path_expression
                   | simple_entity_expression
                   ;

 simple_entity_expression : identifier
                          | Input_parameter
                          ;

 entity_type_expression : type_discriminator 
                        | identifier 
                        | Input_parameter
                        ;

 type_discriminator : TYPE '(' type_discriminator_arg ')';
 
 type_discriminator_arg : Input_parameter 
                        | single_valued_object_path_expression 
                        | single_element_path_expression
                        ;

 functions_returning_numerics : LENGTH '('string_expression')' # Functions_returning_numerics_default
                              | LOCATE '('string_expression',' string_expression (',' arithmetic_expression)? ')' # Functions_returning_numerics_default
                              | ABS '('arithmetic_expression')' # Functions_returning_numerics_default
                              | SQRT '('arithmetic_expression')' # Functions_returning_numerics_default
                              | MOD '('arithmetic_expression',' arithmetic_expression')' # Functions_returning_numerics_default
                              | SIZE '('collection_valued_path_expression')' # Functions_returning_numerics_size 
                              | INDEX '('collection_valued_path_expression')' # Functions_returning_numerics_default
                              ;

 functions_returning_datetime : CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP;

 functions_returning_strings : CONCAT '('string_expression',' string_expression (',' string_expression)*')' # StringFunction
                             | SUBSTRING '('string_expression',' arithmetic_expression (',' arithmetic_expression)?')' # StringFunction
                             | TRIM '('((trim_specification)? (trim_character)? FROM)? string_expression')' # TrimFunction
                             | LOWER '('string_expression')' # StringFunction
                             | UPPER '('string_expression')' # StringFunction
                             ;

 trim_specification : LEADING 
                    | TRAILING 
                    | BOTH
                    ;

 function_invocation : FUNCTION '(' string_literal (',' args+=function_arg)*')';

 function_arg : literal
              | state_field_path_expression 
              | Input_parameter 
              | scalar_expression
              ;

 case_expression : {allowCaseWhen == true}? general_case_expression     //for entity view extension only
                 | {allowCaseWhen == true}? simple_case_expression     //for entity view extension only
                 | coalesce_expression 
                 | nullif_expression
                 ;

 coalesce_expression : COALESCE '('scalar_expression (',' scalar_expression)+')';

 nullif_expression : NULLIF '('scalar_expression',' scalar_expression')';

 literal
     : Boolean_literal
     | enum_literal   
     | Numeric_literal
     | string_literal
     ;
 
 string_literal : String_literal
                | Character_literal
                ;

 literal_temporal 
     : Date_literal 
     | Time_literal 
     | Timestamp_literal
     ;

 trim_character : string_literal
                | Input_parameter
                ; 
 /* conditional expression stuff for case when in entity view extension */
 conditional_expression : conditional_term # ConditionalExpression
                        | conditional_expression or=OR conditional_term # ConditionalExpression_or
                        ;

 conditional_term : conditional_factor # ConditionalTerm
                  | conditional_term and=AND conditional_factor # ConditionalTerm_and
                  ;

 conditional_factor : (not=NOT)? conditional_primary
                    ;

 conditional_primary : simple_cond_expression # ConditionalPrimary_simple
                     | '('conditional_expression')' # ConditionalPrimary
                     ;

 simple_cond_expression : comparison_expression |
                            between_expression |
                            like_expression |
                            in_expression |
                            null_comparison_expression |
                            empty_collection_comparison_expression |
                            collection_member_expression |
                        ;

 between_expression : expr=arithmetic_expression (not=NOT)? BETWEEN bound1=arithmetic_expression AND bound2=arithmetic_expression # BetweenArithmetic
                    | expr=string_expression (not=NOT)? BETWEEN bound1=string_expression AND bound2=string_expression # BetweenString
                    | expr=datetime_expression (not=NOT)? BETWEEN bound1=datetime_expression AND bound2=datetime_expression # BetweenDatetime
                    ;

 in_expression : (state_field_path_expression | type_discriminator) (not=NOT)? IN ( '(' inItems+=literal (',' inItems+=literal)* ')' | param=Input_parameter | '(' param=Input_parameter ')' )
               ;

 like_expression : string_expression (not=NOT)? LIKE pattern_value (ESCAPE escape_character)?
                 ;
 
 pattern_value : string_literal
               | Input_parameter
               ;
 
 escape_character : Character_literal
                  | Input_parameter
                  ;

 null_comparison_expression : (single_valued_path_expression | Input_parameter) IS (not=NOT)? NULL
                            ;

 empty_collection_comparison_expression : collection_valued_path_expression Empty_function
                                        ;

 collection_member_expression : entity_or_value_expression (not=NOT)? Member_of_function collection_valued_path_expression
                              ;

 entity_or_value_expression : state_field_path_expression
                            | simple_entity_or_value_expression 
                            | single_element_path_expression
                            ;

 simple_entity_or_value_expression : identifier 
                                   | Input_parameter 
                                   | literal
                                   ;
 
 comparison_expression : left=string_expression comparison_operator right=string_expression # ComparisonExpression_string
                       | left=boolean_expression op=equality_comparison_operator right=boolean_expression # ComparisonExpression_boolean
                       | left=enum_expression op=equality_comparison_operator right=enum_expression # ComparisonExpression_enum
                       | left=datetime_expression comparison_operator right=datetime_expression # ComparisonExpression_datetime
                       | left=entity_expression op=equality_comparison_operator right=entity_expression # ComparisonExpression_entity
                       | left=arithmetic_expression comparison_operator right=arithmetic_expression # ComparisonExpression_arithmetic
                       ;
 
 equality_comparison_operator : '=' # EqPredicate
                              | Not_equal_operator # NeqPredicate
                              ;
 
 comparison_operator : equality_comparison_operator # EqOrNeqPredicate
                     | '>' # GtPredicate
                     | '>=' # GePredicate
                     | '<' # LtPredicate
                     | '<=' # LePredicate
                     ;
 
 general_case_expression : caseTerminal=CASE when_clause (when_clause)* elseTerminal=ELSE scalar_expression endTerminal=END
                         ;

 when_clause : whenTerminal=WHEN conditional_expression thenTerminal=THEN scalar_expression
             ;

 simple_case_expression : caseTerminal=CASE case_operand simple_when_clause (simple_when_clause)* elseTerminal=ELSE scalar_expression endTerminal=END
                        ;

 simple_when_clause : whenTerminal=WHEN scalar_expression thenTerminal=THEN scalar_expression
                    ;
 
 case_operand : state_field_path_expression
              | type_discriminator
              ;
 
 keyword :KEY
        | VALUE
        | ENTRY
        | AVG
        | SUM
        | MAX
        | MIN
        | COUNT
        | DISTINCT
        | ENUM
        | TYPE
        | LENGTH
        | LOCATE
        | ABS
        | SQRT
        | MOD
        | INDEX
        | CURRENT_DATE
        | CURRENT_TIME
        | CURRENT_TIMESTAMP
        | CONCAT
        | SUBSTRING
        | TRIM
        | LOWER
        | UPPER
        | FROM
        | LEADING
        | TRAILING
        | BOTH
        | FUNCTION
        | COALESCE
        | NULLIF
        | NOT
        | OR
        | AND
        | BETWEEN
        | IN
        | LIKE
        | ESCAPE
        | IS
        | NULL
        | CASE
        | ELSE
        | END
        | WHEN
        | THEN
        | SIZE
        | Empty_function
        | Member_of_function
        | Outer_function
        ;
 
 identifier : Identifier
            | keyword
            ;
             