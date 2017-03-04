/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.usc.irds.sparkler.util.HibernateConstraints.AnotherFieldTrue;

import edu.usc.irds.sparkler.SparklerConfiguration;

import org.apache.commons.beanutils.BeanUtils;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//TODO: Make this generic
public class AnotherFieldTrueValidator implements ConstraintValidator<AnotherFieldTrue, Object> {
    private String fieldMethod;
    private String dependFieldTrueMethod;

    @Override
    public void initialize(AnotherFieldTrue annotation) {
        fieldMethod = annotation.fieldMethod();
        dependFieldTrueMethod = annotation.dependFieldTrueMethod();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        try {
            if (Boolean.parseBoolean(o.getClass().getMethod(dependFieldTrueMethod).invoke(o).toString())
                    && o.getClass().getMethod(fieldMethod).invoke(o) == null)
                return false;
        } catch (Exception e) {
            return false;
        }


        return true;
    }


}
