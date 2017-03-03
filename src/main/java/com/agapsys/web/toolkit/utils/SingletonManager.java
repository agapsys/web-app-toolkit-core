/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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
package com.agapsys.web.toolkit.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Singleton manager.
 *
 * @param <T> Managed super-type
 */
public class SingletonManager<T> {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    /**
     * Returns an object instance of a given class using its default constructor.
     *
     * @param <I> instance type
     * @param clazz instance class.
     * @return an object instance of a given class using its default constructor.
     */
    private static <I> I getDefaultObjInstance(Class<I> clazz) {
        try {
            I obj = clazz.getConstructor().newInstance();
            return obj;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(String.format("Error instantiating class: '%s'", clazz), ex);
        }
    }
    // =========================================================================
    // </editor-fold>

    private final Class<T> thisClass;
    private final Map<Class<? extends T>, T> instanceMap = new LinkedHashMap<>();
    private final Set<T> instanceSet = new LinkedHashSet<>();

    private final Set<T> readOnlyInstanceSet = Collections.unmodifiableSet(instanceSet);
    private final Set<Class<? extends T>> readOnlyClassSet = Collections.unmodifiableSet(instanceMap.keySet());

    /**
     * Constructor.
     * @param superClass managed super class
     */
    public SingletonManager(Class<T> superClass) {
        thisClass = superClass;
    }

    /**
     * Clear all managed data.
     */
    public void clear() {
        synchronized(instanceMap) {
            instanceMap.clear();
            instanceSet.clear();
        }
    }

    /**
     * Returns a boolean indicating there are no managed instances.
     *
     * @return a boolean indicating there are no managed instances.
     */
    public boolean isEmpty() {
        synchronized(instanceMap) {
            return instanceMap.isEmpty();
        }
    }

    /**
     * Returns all managed instances.
     *
     * @return all managed instances.
     */
    public Set<T> getInstances() {
        synchronized(instanceMap) {
            return readOnlyInstanceSet;
        }
    }

    /**
     * Returns all managed classes.
     *
     * @return all managed classes.
     */
    public Set<Class<? extends T>> getClasses() {
        synchronized(instanceMap) {
            return readOnlyClassSet;
        }
    }

    /**
     * Registers an instance.
     *
     * @param instance instance to be managed.
     * @param overrideClassHierarchy defines class hierachy should be overriden.
     */
    public void registerInstance(T instance, boolean overrideClassHierarchy) {
        synchronized (instanceMap) {

            if (instance == null)
                throw new IllegalArgumentException("Instance cannot be null");

            instanceMap.put((Class<? extends T>) instance.getClass(), instance);
            instanceSet.add(instance);
            
            if (!overrideClassHierarchy)
                return;

            Class<?> tmpClass = instance.getClass();
            while(true) {
                // Register entire class hierachy up first subclass of 'thisClass'...
                Class<?> superClass = tmpClass.getSuperclass();

                int modifiers = superClass.getModifiers();
                boolean isConcreteClass = !Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers);

                if (isConcreteClass && thisClass.isAssignableFrom(superClass)) {
                    instanceMap.put((Class<? extends T>) superClass, instance);
                    tmpClass = superClass;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Registers an instance.
     * 
     * this is a convenience method for registerInstance(instance, true).
     * 
     * @param instance instance to be managed.
     */
    public final void registerInstance(T instance) {
        registerInstance(instance, true);
    }
    
    /**
     * Registers an instance using given class default constructor.
     *
     * @param <I> instance class.
     * @param instanceClass class used to instantiate an object using its default constructor.
     * @param overrideClassHierarchy defines class hierachy should be overriden.
     * @return created instance.
     */
    public <I extends T> I registerClass(Class<I> instanceClass, boolean overrideClassHierarchy) {
        synchronized(instanceMap) {
            if (instanceClass == null)
                throw new IllegalArgumentException("Class cannot be null");

            I instance = getDefaultObjInstance(instanceClass);
            registerInstance(instance, overrideClassHierarchy);
            return instance;
        }
    }

    /**
     * Registers an instance using given class default constructor.
     * 
     * This is a convenience method for registerClass(instanceClass, true).
     *
     * @param <I> instance class.
     * @param instanceClass class used to instantiate an object using its default constructor.
     * @return created instance.
     */    
    public final <I extends T> I registerClass(Class<I> instanceClass) {
        return registerClass(instanceClass, true);
    }
    
    /**
     * Returns an instance singleton.
     *
     * @param <I> instance type
     * @param instanceClass instance class
     * @param autoRegistration defines if an instance shall be created and
     * automatically registered if required. Passing false, implies returning
     * null if there is no associated instance.
     * @param overrideClassHierarchy defines class hierachy should be overriden. If autoRegistration is false and a instance is not registered, this argument will be ignored.
     * @return instance singleton.
     */
    public <I extends T> I getInstance(Class<I> instanceClass, boolean autoRegistration, boolean overrideClassHierarchy) {
        synchronized(instanceMap) {
            if (instanceClass == null)
                throw new IllegalArgumentException("Instance class cannot be null");

            I instance = (I) instanceMap.get(instanceClass);
            if (instance == null && autoRegistration) {
                instance = registerClass(instanceClass, overrideClassHierarchy);
            }

            return instance;
        }
    }
    
    /**
     * Returns an instance singleton.
     * 
     * This is a convenience method for getInstance(instanceClass, autoRegistration, true).
     *
     * @param <I> instance type
     * @param instanceClass instance class
     * @param autoRegistration defines if an instance shall be created and
     * automatically registered if required. Passing false, implies returning
     * null if there is no associated instance.
     * @return instance singleton.
     */
    public final <I extends T> I getInstance(Class<I> instanceClass, boolean autoRegistration) {
        return getInstance(instanceClass, autoRegistration, true);
    }

    /**
     * Returns an instance singleton.
     * 
     * This is a convenience method for getInstance(instanceClass, false).
     * @param <I> instance type
     * @param instanceClass instance class
     * @return instance singleton. If there is no associated instance, returns null.
     */
    public final <I extends T> I getInstance(Class<I> instanceClass) {
        return getInstance(instanceClass, false);
    }

}
