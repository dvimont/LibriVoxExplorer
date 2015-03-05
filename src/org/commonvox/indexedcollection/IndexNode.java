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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Daniel Vimont
 * @param <V> Class of values that are to be indexed 
 * (e.g., {@literal <Book>} class in a library catalog application).
 Note that, depending upon IDE and compiler settings, it may be
 advisable to avoid using the usually-preferred "diamond operator" 
 when instantiating instances of IndexNode, in order to avoid 
 "unchecked generic array creation for varargs parameter" warnings 
 during compilation.
 */
public class IndexNode<V> {
    public static long count;
    protected final Class<?> VALUE_CLASS;
    protected final Class<? extends Key>[] KEY_CLASS_ARRAY;
    protected final Method[][] AUTOFILL_ARRAY_OF_GET_METHODS;
    protected String TITLE;
    protected final Map<Key, IndexNode<V>> middleMap = new TreeMap<>();
    protected final Map<Key, V> bottomMap = new TreeMap<>();
    protected final boolean TOP_LEVEL_MAP;

    /** Construct an IndexNode object for mapping of objects of the class
     * specified by valueClass. The IndexNode will only accept entries 
     * which include an array of Keys which are of the type and in the order 
     * specified by keyClassArray.
     * @param valueClass Class object denoting the class of objects that will
     * be mapped by this IndexNode instance. The type variable must be of class
     * or subclass established by the type variable V used in instantiation
     * of the {@link org.commonvox.indexedcollection.IndexNode}.
     * @param title TITLE of IndexNode, used mainly for reporting, development,
     * and debugging purposes.
     * @param keyClassArray array of Class objects defining order and type
     * of Keys to which all key arrays submitted in a 
     * {@link org.commonvox.indexedcollection.MultiKeyEntry} parameter to this 
     * IndexNode must adhere. 
     */
    @SafeVarargs
    public IndexNode(Class<? extends V> valueClass,
                        String title, 
                        Class<? extends Key>... keyClassArray) {
        this(true, valueClass, title, keyClassArray);
    }
    
    /**
     *
     * @param title
     * @param valueClass
     * @param keyClassArray
     */
    @SafeVarargs
    public IndexNode(String title, 
                        Class<? extends IndexedKey> valueClass,
                        Class<? extends Key>... keyClassArray) {
        this(true, valueClass, title, keyClassArray);
    }
    
    /** This protected version of the constructor is only invoked directly 
     * within the package for building of metamaps (as used in the 
     * {@link org.commonvox.indexedcollection.IndexedCollectionManager} class). 
     * Note that, with this constructor signature, valueClass is not limited 
     * to a (sub)class of V.
     * @param dummyInt This dummy parameter exists only to make the signature
     * of this protected constructor differ from one of the public constructors.
     * Any value may be submitted, and it will be ignored.
     * @param valueClass Class object denoting the class of objects that will
     * be mapped by this IndexNode instance.
     * @param title TITLE of IndexNode, used mainly for reporting, development,
     * and debugging purposes.
     * @param keyClassArray array of Class objects defining order and type
     * of Keys which all key arrays submitted in a 
     * {@link org.commonvox.indexedcollection.MultiKeyEntry} to this IndexNode must
     * adhere to. 
     */
    @SafeVarargs
    protected IndexNode(int dummyInt,
                        Class<?> valueClass,
                        String title, 
                        Class<? extends Key>... keyClassArray) {
        this(true, valueClass, title, keyClassArray);
    }
    
    /**  
     * @param isTopLevelMap Indicates whether or not the IndexNode being 
     * instantiated is a "top level" map (i.e., accessible by a non-IndexNode 
     * object). Lower level IndexNode objects are only instantiated and 
     * accessed by higher level IndexNode objects.
     * @param valueClass Class object denoting the class of objects that will
     * be mapped by this IndexNode instance. The type variable must be of class
     * or subclass established by the type variable V used in instantiation
     * of the {@link org.commonvox.indexedcollection.IndexNode}.
     * @param title TITLE of IndexNode, used mainly for reporting, development,
     * & debugging purposes.
     * @param keyClassArray array of Class objects defining order and type
     * of Keys which all key arrays submitted in a 
     * {@link org.commonvox.indexedcollection.MultiKeyEntry} to this IndexNode must
     * adhere to. 
     */
    @SafeVarargs
    private IndexNode(boolean isTopLevelMap,
                        Class<?> valueClass,
                        String title, 
                        Class<? extends Key>... keyClassArray) {
        IndexNode.checkVarargs(keyClassArray);
        this.VALUE_CLASS = valueClass;
        this.TOP_LEVEL_MAP = isTopLevelMap;
        this.KEY_CLASS_ARRAY = keyClassArray;
        if (TOP_LEVEL_MAP) {
            this.TITLE = title;
            this.AUTOFILL_ARRAY_OF_GET_METHODS = buildKeyClassGetMethodArray();
        } else {
            this.AUTOFILL_ARRAY_OF_GET_METHODS = null;
        }
        count++;
    }

    /** This private constructor only usable by a IndexNode instance to 
     * create other (lower level) IndexNode instances.
     * @param title TITLE of IndexNode, used mainly for reporting, development,
     * & debugging purposes.
     * @param keyClassArray array of Class objects defining order and type
     * of Keys which all key arrays submitted in a 
     * {@link org.commonvox.indexedcollection.MultiKeyEntry} to this IndexNode must
     * adhere to. 
     * @param multiKeyEntries one or more MultiKeyEntry objects to be put into
     * the newly created IndexNode.
     * @throws InvalidMultiKeyException 
     */
    @SafeVarargs
    private IndexNode (Class<?> valueClass,
                            String title, 
                            Class<? extends Key>[] keyClassArray, 
                            MultiKeyEntry<V>... multiKeyEntries)
            throws InvalidMultiKeyException {
        this(false, valueClass, title, keyClassArray);
        for (MultiKeyEntry<V> multiKeyEntry : multiKeyEntries) {
            put(multiKeyEntry);
        }
    }
    
    /**
     * This method may be invoked by any method which accepts vararg parameters,
     * but for which it is intended that a null or empty vararg array is invalid.
     * @param objectArray
     * @throws IllegalArgumentException
     */
    public static void checkVarargs (Object[] objectArray) 
            throws IllegalArgumentException {
        if (objectArray == null || objectArray.length == 0) {
            throw new IllegalArgumentException 
                ("Null or zero-length array submitted for varargs parameters.");
        }
    }
    
    /**
     *
     * @param value
     * @param keyArray
     * @return
     * @throws InvalidMultiKeyException
     */
    @SafeVarargs
    public final boolean put (V value, Key... keyArray) 
            throws InvalidMultiKeyException {
        return put(new MultiKeyEntry<>(value, keyArray));
    }
    
    /**
     *
     * @param multiKeyEntry
     * @return <code>true</code> if ______ <code>false</code> if ______
     * @throws InvalidMultiKeyException
     */
    public final boolean put (MultiKeyEntry<V> multiKeyEntry) 
            throws InvalidMultiKeyException {
        if (multiKeyEntry.containsNulls() 
                || multiKeyEntry.getKeyArrayLength() == 0) {
            return false; // throw exception?
        }
        if (!KEY_CLASS_ARRAY[0].isAssignableFrom(multiKeyEntry.getTopKey().getClass())) {
            throw new InvalidMultiKeyException
                ("Invalid Key object submitted for 'put' into "
                    + "IndexNode <" + getTitle() + " LEVEL " 
                    + this.KEY_CLASS_ARRAY.length
                    + ">. Requires a Key object of class (or subclass of) = " 
                    + KEY_CLASS_ARRAY[0].getSimpleName() 
                    + ". Class of the invalid submitted object is = "
                    + multiKeyEntry.getTopKey().getClass().getSimpleName() + ".");
        }
        if (multiKeyEntry.getKeyArrayLength() == 1) {
            V selectValue = this.bottomMap.get(multiKeyEntry.getTopKey());
            if (selectValue == null) {
                this.bottomMap.put
                    (multiKeyEntry.getTopKey(), multiKeyEntry.getValue());
                return true;
            } else {
                return false; // no overwriting of bottomMap values accepted
            }
        } else {
            IndexNode<V> lowerIndexNode 
                    = this.middleMap.get(multiKeyEntry.getTopKey());
            MultiKeyEntry<V> lowerMultiKeyEntry 
                    = multiKeyEntry.getLowerMultiKeyEntry();
            if (lowerIndexNode == null) {
                this.middleMap.put
                    (multiKeyEntry.getTopKey(), 
                        new IndexNode<V>(this.VALUE_CLASS, this.TITLE, 
                                            this.getLowerKeyClassArray(),
                                            lowerMultiKeyEntry));
                return true;
            } else {
                return lowerIndexNode.put(lowerMultiKeyEntry);
            }
        }
    }
    
    private Class<? extends Key>[] getLowerKeyClassArray () {
        if (KEY_CLASS_ARRAY.length < 1) {
            return null;
        }
        return Arrays.copyOfRange(KEY_CLASS_ARRAY, 1, KEY_CLASS_ARRAY.length);
    }

    /**
     *
     * @return
     */
    public int getDepth () {
        return this.KEY_CLASS_ARRAY.length;
    }

    /**
     *
     * @return
     */
    public String getTitle () {
        return this.TITLE;
    }
    
    /**
     *
     * @return
     */
    public Class<? extends Key>[] getKeyClassArray() {
        return this.KEY_CLASS_ARRAY;
    }
    
    /**
     *
     * @return
     */
    public List<V> selectAll () {
        List<V> vList = new ArrayList<>();
        if (bottomMap != null && !bottomMap.isEmpty()) {
            vList.addAll(this.bottomMap.values());
        }
        if (middleMap != null && !middleMap.isEmpty()) {
            for (IndexNode<V> lowerIndexNode : this.middleMap.values()) {
                vList.addAll(lowerIndexNode.selectAll());
            }
        }
        return vList;
    }
    
    /**
     *Invoked to fetch the first value with top-level key(s) == submitted key(s)
     * @param keyArray
     * @return
     */
    public V getFirst (Key... keyArray) {
        List<V> returnedList = get(keyArray);
        if (returnedList.isEmpty()) {
            return null;
        } else {
            return returnedList.get(0);
        }
    }
    
    /**
     * Invoked to fetch value(s) associated with the submitted array of Key objects
     * @param keyArray
     * @return
     */
    @SafeVarargs
    public final List<V> get (Key... keyArray) {
        return get(new KeyArray(keyArray));
    }
    
    /**
     * Invoked to fetch value(s) associated with the submitted KeyArray object
     * @param keyArray
     * @return
     */
    public List<V> get (KeyArray keyArray){
        List<V> vList = new ArrayList<>();

        if (bottomMap != null && !bottomMap.isEmpty()) {
            if (keyArray.getTopKey() == null) {
                vList.addAll(selectAll());
            } else {
                vList.add(this.bottomMap.get(keyArray.getTopKey()));
            }
        } 
        if (middleMap != null && !middleMap.isEmpty()) {
            if (keyArray.getTopKey() == null) {
                for (IndexNode<V> lowerIndexNode : this.middleMap.values()) {
                    vList.addAll(lowerIndexNode.get(keyArray.getLowerKeyArray()));
                }
            } else {
                if (this.middleMap.containsKey(keyArray.getTopKey())) {
                    IndexNode<V> lowerIndexNode 
                            = this.middleMap.get(keyArray.getTopKey());
                    vList.addAll
                        (lowerIndexNode.get(keyArray.getLowerKeyArray()));
                }
            }
        }
        return vList;
    }
    
    /**
     *
     * @return
     */
    public Class<?> getValueClass() {
        return this.VALUE_CLASS;
    }
    
    /**
     *
     * @return
     */
    public int size() {
        int size = 0;
        if (bottomMap != null && !bottomMap.isEmpty()) {
            size += this.bottomMap.size();
        } 
        if (middleMap != null && !middleMap.isEmpty()) {
            for (IndexNode<V> lowerIndex : this.middleMap.values()) {
                size += lowerIndex.size();
            }
        }
        return size;
    }

    /**
     *
     * @return
     */
    public Set<MultiKeyEntry<V>> getMultiKeyEntrySet() {
        Set<MultiKeyEntry<V>> multiKeyEntrySet = new LinkedHashSet<>();
        if (bottomMap != null && !bottomMap.isEmpty()) {
            for (Entry<Key, V> entry : bottomMap.entrySet()) {
                multiKeyEntrySet.add
                    (new MultiKeyEntry<>(entry.getValue(), entry.getKey()));
            }
        } 
        if (middleMap != null && !middleMap.isEmpty()) {
            for (Entry<Key, IndexNode<V>> entry 
                                        : this.middleMap.entrySet()) {
                Set<MultiKeyEntry<V>> lowerMultiKeyEntrySet 
                                        = entry.getValue().getMultiKeyEntrySet();
                for (MultiKeyEntry<V> lowerMultiKeyEntry 
                                                    : lowerMultiKeyEntrySet) {
                    lowerMultiKeyEntry.putTopKey(entry.getKey());
                    multiKeyEntrySet.add(lowerMultiKeyEntry);
                }
            }
        }
        return multiKeyEntrySet;
    }
    
    private Method[][] buildKeyClassGetMethodArray() {
        Method[][] arrayOfGetMethods = new Method[this.KEY_CLASS_ARRAY.length][];
        keyClassLoop:
        for (int i=0 ; i < this.KEY_CLASS_ARRAY.length ; i++) {
            if (this.KEY_CLASS_ARRAY[i].equals(this.VALUE_CLASS)
                    || this.KEY_CLASS_ARRAY[i].isAssignableFrom(VALUE_CLASS)) {
                continue;
            }
            List<Method> listOfGetMethods = new ArrayList<>();
            for (Method method : this.VALUE_CLASS.getMethods()) {
                if (methodGetsKeyClass(method, this.KEY_CLASS_ARRAY[i])) {
                    listOfGetMethods.add(method);
                }
            }
            arrayOfGetMethods[i] = new Method[listOfGetMethods.size()];
            for (int k=0; k < listOfGetMethods.size(); k++) {
                arrayOfGetMethods[i][k] = listOfGetMethods.get(k);
            }
        }
        return arrayOfGetMethods;
    }
    
    private boolean methodGetsKeyClass
            (Method method, Class<? extends Key> keyClass) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (method.getReturnType().equals(keyClass)) {
            return true;
        } else if (List.class.isAssignableFrom(method.getReturnType())) {
            ParameterizedType listType 
                    = (ParameterizedType)method.getGenericReturnType();
            Type typeOfObjectsInList 
                    = listType.getActualTypeArguments()[0];
            if (typeOfObjectsInList.getTypeName()
                    .equals(keyClass.getTypeName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     *
     * @return
     */
    protected final boolean autofillEnabled () {
        if (this.AUTOFILL_ARRAY_OF_GET_METHODS == null) {
            return false;
        }
        for (int i=0 ; i < this.KEY_CLASS_ARRAY.length ; i++){
            if (this.KEY_CLASS_ARRAY[i].isAssignableFrom(VALUE_CLASS)) {
                continue;
            }
            if (this.AUTOFILL_ARRAY_OF_GET_METHODS[i] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param value
     * @param valueKey
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InvalidMultiKeyException
     */
    public final boolean autoFill (V value, Key valueKey) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException {
        LinkedList<Method[]> listOfGetMethods 
                = new LinkedList<>(Arrays.asList(this.AUTOFILL_ARRAY_OF_GET_METHODS));
        LinkedList<Key> keyList = new LinkedList<>(); 
        return buildAndPutKeyArray(value, valueKey, listOfGetMethods, keyList);
    }
    
    private boolean buildAndPutKeyArray
            (V value, Key valueKey, 
                LinkedList<Method[]> listOfGetMethods, LinkedList<Key> keyList)
            throws IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException {
                
        if (listOfGetMethods.isEmpty()) {
            return put(value, keyList.toArray(new Key[keyList.size()]));
        }
        
        Method[] nextGetMethods = listOfGetMethods.removeFirst();
        /** The following logic likely needs explanatory comment here. 
         * See method buildKeyClassGetMethodArray above. */
        if (nextGetMethods == null || nextGetMethods.length == 0) {
            keyList.add(valueKey);
            return buildAndPutKeyArray(value, valueKey, 
                                            listOfGetMethods, keyList);
        }
        
        for (int i=0; i < nextGetMethods.length; i++) {
            if (List.class.isAssignableFrom(nextGetMethods[i].getReturnType())) {
                @SuppressWarnings("unchecked")
                List<Key> retrievedKeyList = (List<Key>)nextGetMethods[i].invoke(value);
                if (retrievedKeyList == null) {
                    continue;
                }
                boolean allPutsSuccessful = true;
                for (Key key : retrievedKeyList) {
                    LinkedList<Method[]> listOfGetMethodsCopy 
                            = new LinkedList<>(listOfGetMethods);
                    LinkedList<Key> keyListCopy = new LinkedList<>(keyList);
                    keyListCopy.add(key);
                    boolean putsSuccessful =
                        buildAndPutKeyArray(value, valueKey, 
                                                listOfGetMethodsCopy, keyListCopy);
                    if (!putsSuccessful) {
                        allPutsSuccessful = false;
                    }
                }
            } else { 
                assert Key.class.isAssignableFrom(nextGetMethods[i].getReturnType());
                Key retrievedKey = (Key)nextGetMethods[i].invoke(value);
                if (retrievedKey == null) {
                    continue;
                }
                keyList.add(retrievedKey);
                buildAndPutKeyArray(value, valueKey, 
                                                    listOfGetMethods, keyList);
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "<" + this.TITLE + "> multi-key map -- "
            + "Maps objects of the <" + VALUE_CLASS.getSimpleName() + "> class.\n" 
            + "     " + keyClassArrayToString() + ".\n"
            + "     SIZE OF MULTI-KEY MAP = " + this.size()
            + " ; DEPTH OF MULTI-KEY MAP = " + this.KEY_CLASS_ARRAY.length;
    }
    
    /**
     * Prints verbose listing of IndexNode contents; mainly for test/debug.
     */
    public void dumpContents() {
        StringBuilder output;
        boolean firstItemPrinted;
        printHeadingWithTimestamp
            ("Dump of <" + this.getTitle() + "> IndexNode", this.size());
        System.out.println(keyClassArrayToString());
        printHeading("<VALUE toString> : {KEY1; KEY2; KEY3...}");
        for (MultiKeyEntry<V> multiKeyEntry : this.getMultiKeyEntrySet()) {
            output = new StringBuilder();
            output.append(multiKeyEntry.getValue()).append(" : {");
            firstItemPrinted = false;
            for (Key key : multiKeyEntry.getKeyArray()) {
                if (firstItemPrinted) {
                    output.append("; ");
                } else {
                    firstItemPrinted = true;
                }
                output.append("<").append(key.getKeyItem()).append(">");
            }
            output.append("}");
            System.out.println(output);
        }
    }

    private String keyClassArrayToString() {
        StringBuilder output = new StringBuilder();
        output.append("Key classes for this IndexNode are: {");
        boolean firstItemPrinted = false;
        for (Class<? extends Key> multiKeyClass 
                                        : this.getKeyClassArray()) {
            if (firstItemPrinted) {
                output.append("; ");
            } else {
                firstItemPrinted = true;
            }
            output.append("<").append(multiKeyClass.getSimpleName()).append(">");
        }
        output.append("}");
        if (this.autofillEnabled()) {
            output.append(" [AUTOFILL is ENABLED]");
        }
        return output.toString();
    }
    
    /**
     *
     * @param headingTitle
     */
    public static void printHeading(String headingTitle) {
        String headingBorder 
            = new String(new char[headingTitle.length()]).replace("\0", "=");

        System.out.println(headingBorder);
        System.out.println(headingTitle);
        System.out.println(headingBorder);
    }
    
    /**
     *
     * @param headingTitle
     * @param indexSize
     */
    public static void printHeadingWithTimestamp
                        (String headingTitle, int indexSize) {
        String headingBorder 
            = new String(new char[headingTitle.length()]).replace("\0", "=");

        System.out.println(headingBorder);
        System.out.println(headingTitle + "  (" 
                    + new Timestamp(System.currentTimeMillis()) + ")");
        if (indexSize > 0) {
            System.out.println("(SIZE OF INDEX = " + indexSize + " ENTRIES)");
        }
        System.out.println(headingBorder);
    }
}
