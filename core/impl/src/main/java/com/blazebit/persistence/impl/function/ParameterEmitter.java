/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import java.util.List;

/**
 *
 * @author Christian
 */
public class ParameterEmitter implements TemplateEmitter {

    private final int index;

    public ParameterEmitter(int index) {
        this.index = index;
    }
    
    @Override
    public void emit(FunctionRenderContext context, List<?> parameters) {
        Object value;
        if (index < parameters.size() && (value = parameters.get(index)) != null) {
            context.addChunk(value.toString());
        }
    }
    
}
