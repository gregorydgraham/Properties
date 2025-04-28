/*
 * Copyright 2013 Gregory Graham.
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
package nz.co.gregs.properties.examples;

import nz.co.gregs.properties.PropertyContainer;

/**
 * An example implementation representing a Foreign Key relationship in an ORM layer
 *
 * @author gregorygraham
 */
public @interface DBForeignKey {
  
  /**
   * The class of the referenced table.
   * 
   * <p>as this is an example implementation, we have assumed a simple FK system
   * and only need the class of PropertyContainer</p>
   * 
   * @return the class of the referenced property container
   */
  Class<? extends PropertyContainer> value() default PropertyContainer.class;
}
