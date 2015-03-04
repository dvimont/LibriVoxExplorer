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
package org.commonvox.indexedcollectionmanager;

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
 */
public class IndexedCollectionManager<V> extends IndexedMetaCollection<V> {
    
    private IndexedMetaCollection<IndexedKey> mappedKeyDirectory;
    private IndexedCollection<IndexedCollection<IndexedKey>> metamapOfMappedKeyMaps;
    private final List<Method> ACTIVE_GET_METHODS = new ArrayList<>();
    
    public IndexedCollectionManager (Class<V> masterClass) {
        super(masterClass);
    }

    @SafeVarargs
    public final void buildMappedKeyDirectory 
        (String title, IndexedCollection<IndexedKey>... multiKeyMapArray) 
                throws InvalidMultiKeyException,
                        IndexedCollectionBuildFailureException {
        IndexedCollection.checkVarargs(multiKeyMapArray);
        mappedKeyDirectory 
                = new IndexedKeyManager<IndexedKey>(IndexedKey.class);
        mappedKeyDirectory.build(title, multiKeyMapArray);
        
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
            = new IndexedCollection<IndexedCollection<IndexedKey>>
                    (0, IndexedCollection.class, 
                        "Map to match MASTER_CLASS getMethods with MappedKey maps",
                        MethodKey.class, IntegerKey.class);
        int uniqueId = 0;
        for (IndexedCollection<IndexedKey> multiKeyMap 
                                : mappedKeyDirectory.metamap.selectAll()) {
            if (!multiKeyMap.autofillEnabled()) {
                continue;
            }
            Method methodToGetMappedKey 
                = methodsToGetMappedKey.get(multiKeyMap.getValueClass().getName());
            if (methodToGetMappedKey != null) {
                ACTIVE_GET_METHODS.add(methodToGetMappedKey);
                metamapOfMappedKeyMaps
                        .put(multiKeyMap,
                                new MethodKey(methodToGetMappedKey),
                                new IntegerKey(++uniqueId));
            } else {
                for (Map.Entry<String,Method> entry : methodsToGetList.entrySet()) {
                    if (entry.getKey()
                            .equals(multiKeyMap.getValueClass().getTypeName())) {
                        ACTIVE_GET_METHODS.add(entry.getValue());
                        metamapOfMappedKeyMaps
                                .put(multiKeyMap,
                                        new MethodKey(entry.getValue()),
                                        new IntegerKey(++uniqueId));
                        break;
                    }
                }
            }
        }
    }
    
    public boolean allMappedKeyMapsAutofillEnabled () {
        return mappedKeyDirectory.allMapsAutofillEnabled();
    }

    public final void autoFill (V value, Key valueKey) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException {
        if (value == null || valueKey == null) {
            return;
        }
        for (IndexedCollection<V> multiKeyMap : this.metamap.selectAll()) {
            if (multiKeyMap.autofillEnabled() 
                    && multiKeyMap.getValueClass().isAssignableFrom
                                                        (value.getClass())) {
                multiKeyMap.autoFill(value, valueKey);
            }
        }
        autoFillMappedKeyMaps(value);
    }
    
    private void autoFillMappedKeyMaps (V value) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException {
        
        for (Method thisGetMethod : ACTIVE_GET_METHODS) {
            List<IndexedCollection<IndexedKey>> mapsRelatedToThisGetMethod 
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
                for (IndexedCollection<IndexedKey> multiKeyMap 
                                        : mapsRelatedToThisGetMethod) {
                    for (IndexedKey mappedKeyObject : mappedKeyObjects) {
                        multiKeyMap.autoFill(mappedKeyObject, mappedKeyObject);
                    }
                }
            } else { // get a single instance of mappedKeyObject
                IndexedKey mappedKeyObject 
                        = (IndexedKey)thisGetMethod.invoke(value);
                if (mappedKeyObject == null) {
                    continue;
                }
                for (IndexedCollection<IndexedKey> multiKeyMap 
                                        : mapsRelatedToThisGetMethod) {
                    multiKeyMap.autoFill(mappedKeyObject, mappedKeyObject);
                }
            }
        }
    }
    
    public IndexedMetaCollection<IndexedKey> getMappedKeyDirectory () {
        return this.mappedKeyDirectory;
    }
    
    private class MethodKey 
        implements KeyWrapper<Method>, Comparable<MethodKey> 
    {
        private Method wrappedMethod;

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
