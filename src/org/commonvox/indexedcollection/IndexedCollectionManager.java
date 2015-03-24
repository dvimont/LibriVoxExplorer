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
    
    private IndexedMetaCollection<IndexedKey> indexedKeyDirectory;
    private IndexedCollection<IndexedCollection<IndexedKey>> metamapOfIndexedKeyMaps;
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
     * @param title title of 
     * @param indexedCollectionArray
     * @throws InvalidMultiKeyException
     * @throws IndexedCollectionBuildFailureException
     */
    @SafeVarargs
    public final void buildIndexedKeyDirectory 
        (String title, IndexedCollection<IndexedKey>... indexedCollectionArray) 
                throws InvalidMultiKeyException,
                        IndexedCollectionBuildFailureException {
        IndexedCollection.checkVarargs(indexedCollectionArray);
        indexedKeyDirectory 
                = new IndexedKeyManager<IndexedKey>(IndexedKey.class);
        indexedKeyDirectory.build(title, indexedCollectionArray);
        
        buildDirectoryOfGetMethodsForIndexedKeys();
        
        //indexedKeysDirectory.dumpContents();
    }

    private final void buildDirectoryOfGetMethodsForIndexedKeys()
                throws InvalidMultiKeyException {
        
        Map<String, Method> methodsToGetIndexedKey = new TreeMap<>();
        Map<String, Method> methodsToGetList = new TreeMap<>();

        for (Method method : MASTER_CLASS.getMethods()) {
            if (method.getParameterCount() == 0) {
                if (IndexedKey.class.isAssignableFrom(method.getReturnType())) {
                    methodsToGetIndexedKey
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

        metamapOfIndexedKeyMaps 
            = new IndexedCollection<IndexedCollection<IndexedKey>>
                    (0, IndexedCollection.class, 
                        "Map to match MASTER_CLASS getMethods with IndexedKey maps",
                        MethodKey.class, IntegerKey.class);
        int uniqueId = 0;
        for (IndexedCollection<IndexedKey> indexedCollection 
                                : indexedKeyDirectory.metamap.selectAll()) {
            if (!indexedCollection.autofillEnabled()) {
                continue;
            }
            Method methodToGetIndexedKey 
                = methodsToGetIndexedKey.get(indexedCollection.getValueClass().getName());
            if (methodToGetIndexedKey != null) {
                ACTIVE_GET_METHODS.add(methodToGetIndexedKey);
                metamapOfIndexedKeyMaps
                        .put(indexedCollection,
                                new MethodKey(methodToGetIndexedKey),
                                new IntegerKey(++uniqueId));
            } else {
                for (Map.Entry<String,Method> entry : methodsToGetList.entrySet()) {
                    if (entry.getKey()
                            .equals(indexedCollection.getValueClass().getTypeName())) {
                        ACTIVE_GET_METHODS.add(entry.getValue());
                        metamapOfIndexedKeyMaps
                                .put(indexedCollection,
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
    public boolean allIndexedKeyMapsAutofillEnabled () {
        return indexedKeyDirectory.allMapsAutofillEnabled();
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
        for (IndexedCollection<V> indexedCollection : this.metamap.selectAll()) {
            if (indexedCollection.autofillEnabled() 
                    && indexedCollection.getValueClass().isAssignableFrom
                                                        (value.getClass())) {
                indexedCollection.autoFill(value, valueKey);
            }
        }
        autoFillIndexedKeyMaps(value);
    }
    
    private void autoFillIndexedKeyMaps (V value) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException {
        
        for (Method thisGetMethod : ACTIVE_GET_METHODS) {
            List<IndexedCollection<IndexedKey>> mapsRelatedToThisGetMethod 
                    = metamapOfIndexedKeyMaps
                            .get(new MethodKey(thisGetMethod));
            if (mapsRelatedToThisGetMethod == null) {
                continue;
            }
            
            if (List.class.isAssignableFrom(thisGetMethod.getReturnType())) {
                @SuppressWarnings("unchecked")
                List<IndexedKey> indexedKeyObjects 
                        = (List<IndexedKey>)thisGetMethod.invoke(value);
                if (indexedKeyObjects == null) {
                    continue;
                }
                for (IndexedCollection<IndexedKey> indexedCollection 
                                        : mapsRelatedToThisGetMethod) {
                    for (IndexedKey indexedKeyObject : indexedKeyObjects) {
                        indexedCollection.autoFill(indexedKeyObject, indexedKeyObject);
                    }
                }
            } else { // get a single instance of indexedKeyObject
                IndexedKey indexedKeyObject 
                        = (IndexedKey)thisGetMethod.invoke(value);
                if (indexedKeyObject == null) {
                    continue;
                }
                for (IndexedCollection<IndexedKey> indexedCollection 
                                        : mapsRelatedToThisGetMethod) {
                    indexedCollection.autoFill(indexedKeyObject, indexedKeyObject);
                }
            }
        }
    }
    
    /**
     *
     * @return
     */
    public IndexedMetaCollection<IndexedKey> getIndexedKeyDirectory () {
        return this.indexedKeyDirectory;
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
