/*
 * Copyright (C) 2014 Daniel Vimont
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commonvox.indexedcollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Daniel Vimont
 * @param <V>
 */
public class IndexedCollectionManager<V> extends IndexedMetaCollection<V> {
    
    private IndexedMetaCollection<IndexedKey> mappedKeyDirectory;
    private IndexNode<IndexNode<IndexedKey>> metamapOfMappedKeyMaps;
    private final List<Method> ACTIVE_GET_METHODS = new ArrayList<>();
    
    /**
     *
     * @param masterClass
     */
    public IndexedCollectionManager (Class<V> masterClass) {
        super(masterClass);
    }

    /**
     *
     * @param title
     * @param indexNodeArray
     * @throws InvalidMultiKeyException
     * @throws IndexedCollectionBuildFailureException
     */
    @SafeVarargs
    public final void buildMappedKeyDirectory 
        (String title, IndexNode<IndexedKey>... indexNodeArray) 
                throws InvalidMultiKeyException,
                        IndexedCollectionBuildFailureException {
        IndexNode.checkVarargs(indexNodeArray);
        mappedKeyDirectory 
                = new IndexedKeyManager<IndexedKey>(IndexedKey.class);
        mappedKeyDirectory.build(title, indexNodeArray);
        
        buildDirectoryOfGetMethodsForMappedKeys();
        
        //mappedKeysDirectory.dumpContents();
    }

    private final void buildDirectoryOfGetMethodsForMappedKeys()
                throws InvalidMultiKeyException {
        
        Map<String, Method> methodsToGetMappedKey = new TreeMap<>();
        Map<String, Method> methodsToGetList = new TreeMap<>();

        for (Method method : MASTER_CLASS.getMethods()) {
            if (method.getParameterCount() == 0) {
                if (IndexedKey.class.isAssignableFrom(method.getReturnType())) {
                    methodsToGetMappedKey
                            .put(method.getReturnType().getName(), method);
                } else if (List.class.isAssignableFrom(method.getReturnType())) {
                    ParameterizedType listType 
                            = (ParameterizedType)method.getGenericReturnType();
                    Type typeOfObjectsInList 
                            = listType.getActualTypeArguments()[0];
                    methodsToGetList.put(typeOfObjectsInList.getTypeName(), method);
                }
            }
        }

        metamapOfMappedKeyMaps 
            = new IndexNode<IndexNode<IndexedKey>>
                    (0, IndexNode.class, 
                        "Map to match MASTER_CLASS getMethods with MappedKey maps",
                        MethodKey.class, IntegerKey.class);
        int uniqueId = 0;
        for (IndexNode<IndexedKey> indexNode 
                                : mappedKeyDirectory.metamap.selectAll()) {
            if (!indexNode.autofillEnabled()) {
                continue;
            }
            Method methodToGetMappedKey 
                = methodsToGetMappedKey.get(indexNode.getValueClass().getName());
            if (methodToGetMappedKey != null) {
                ACTIVE_GET_METHODS.add(methodToGetMappedKey);
                metamapOfMappedKeyMaps
                        .put(indexNode,
                                new MethodKey(methodToGetMappedKey),
                                new IntegerKey(++uniqueId));
            } else {
                for (Map.Entry<String,Method> entry : methodsToGetList.entrySet()) {
                    if (entry.getKey()
                            .equals(indexNode.getValueClass().getTypeName())) {
                        ACTIVE_GET_METHODS.add(entry.getValue());
                        metamapOfMappedKeyMaps
                                .put(indexNode,
                                        new MethodKey(entry.getValue()),
                                        new IntegerKey(++uniqueId));
                        break;
                    }
                }
            }
        }
    }
    
    /**
     *
     * @return
     */
    public boolean allMappedKeyMapsAutofillEnabled () {
        return mappedKeyDirectory.allMapsAutofillEnabled();
    }

    /**
     *
     * @param value
     * @param valueKey
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InvalidMultiKeyException
     */
    public final void autoFill (V value, Key valueKey) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException {
        if (value == null || valueKey == null) {
            return;
        }
        for (IndexNode<V> indexNode : this.metamap.selectAll()) {
            if (indexNode.autofillEnabled() 
                    && indexNode.getValueClass().isAssignableFrom
                                                        (value.getClass())) {
                indexNode.autoFill(value, valueKey);
            }
        }
        autoFillMappedKeyMaps(value);
    }
    
    private void autoFillMappedKeyMaps (V value) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException {
        
        for (Method thisGetMethod : ACTIVE_GET_METHODS) {
            List<IndexNode<IndexedKey>> mapsRelatedToThisGetMethod 
                    = metamapOfMappedKeyMaps
                            .get(new MethodKey(thisGetMethod));
            if (mapsRelatedToThisGetMethod == null) {
                continue;
            }
            
            if (List.class.isAssignableFrom(thisGetMethod.getReturnType())) {
                @SuppressWarnings("unchecked")
                List<IndexedKey> mappedKeyObjects 
                        = (List<IndexedKey>)thisGetMethod.invoke(value);
                if (mappedKeyObjects == null) {
                    continue;
                }
                for (IndexNode<IndexedKey> indexNode 
                                        : mapsRelatedToThisGetMethod) {
                    for (IndexedKey mappedKeyObject : mappedKeyObjects) {
                        indexNode.autoFill(mappedKeyObject, mappedKeyObject);
                    }
                }
            } else { // get a single instance of mappedKeyObject
                IndexedKey mappedKeyObject 
                        = (IndexedKey)thisGetMethod.invoke(value);
                if (mappedKeyObject == null) {
                    continue;
                }
                for (IndexNode<IndexedKey> indexNode 
                                        : mapsRelatedToThisGetMethod) {
                    indexNode.autoFill(mappedKeyObject, mappedKeyObject);
                }
            }
        }
    }
    
    /**
     *
     * @return
     */
    public IndexedMetaCollection<IndexedKey> getMappedKeyDirectory () {
        return this.mappedKeyDirectory;
    }
    
    private class MethodKey 
        implements KeyWrapper<Method>, Comparable<MethodKey> 
    {
        private final Method wrappedMethod;

        public MethodKey(Method wrappedMethod) {
            this.wrappedMethod = wrappedMethod;
        }

        @Override
        public String getKeyItem() {
            return wrappedMethod.getName(); 
        }

        @Override
        public int compareTo (MethodKey otherMethodKey) {
            return this.getKeyItem().compareTo(otherMethodKey.getKeyItem());
        }
    }
}
