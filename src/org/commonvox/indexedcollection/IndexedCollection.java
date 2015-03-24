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
 * Provides for composite indexing of a collection of objects (often referred
 * to here as Values) belonging to class denoted by type-parameter {@literal <V>}. 
 * The specific class indexed by the IndexedCollection 
 * ({@link IndexedCollection#getValueClass() VALUE_CLASS}) may optionally be a 
 * subclass of the type-parameter {@literal <V>}.
 * The keys of the composite index are defined in the 
 * {@link IndexedCollection#getKeyClassArray() KEY_CLASS_ARRAY}, and all 
 * classes within that array are required to be implementers of the 
 * {@link Key} interface. 
 * An IndexedCollection works most effectively when "autofill"
 * functioning is enabled. Autofill is enabled if the 
 * {@link IndexedCollection#getValueClass() VALUE_CLASS}
 * has a non-private get method (returning a single object or a collection)
 * for each type denoted in 
 * {@link IndexedCollection#getKeyClassArray() KEY_CLASS_ARRAY}.
 * @author Daniel Vimont
 * @param <V> Class (or superclass) of values that are to be indexed 
 * (e.g., {@literal <Book>} class in a library catalog application).
 * <i>(Note that, depending upon IDE and compiler settings, it may be
 * advisable to avoid using the usually-preferred "diamond operator" 
 * when instantiating instances of IndexedCollection, in order to avoid 
 * "unchecked generic array creation for varargs parameter" warnings 
 * during compilation.)</i>
 */
public class IndexedCollection<V> {
    private static long nodeCount;
    private final Class<?> VALUE_CLASS;
    private final Class<? extends Key>[] KEY_CLASS_ARRAY;
    private final Method[][] AUTOFILL_ARRAY_OF_GET_METHODS;
    private String TITLE;
    private final Map<Key, IndexedCollection<V>> middleMap = new TreeMap<>();
    private final Map<Key, V> bottomMap = new TreeMap<>();
    private final boolean TOP_LEVEL_MAP;

    /** Construct an IndexedCollection object for mapping of objects of the class
     * specified by valueClass. It is required that valueClass must be the same
     * class (or a subclass of the class) denoted by the type-parameter {@literal <V>}.
     * The components of the IndexedCollection's composite index are defined by 
     * the classes passed in the keyClassArray parameter, each of which is
     * required to be an implementer of the {@link Key} interface. 
     * An optional title may be submitted for auditing and development purposes.
     * @param valueClass Class object denoting the class of objects that will
     * be mapped by this IndexedCollection instance. This parameter must be 
     * of class (or subclass of the class) established by the type variable V 
     * used in instantiation of the IndexedCollection.
     * @param title {@link IndexedCollection#TITLE TITLE} of IndexedCollection, 
     * used mainly for reporting, development, and debugging purposes.
     * @param keyClassArray Array of {@link Key} Class objects defining order 
     * and type of {@link Key}s to which all {@link KeyArray}s in entries 
     * submitted to the {@link IndexedCollection#put(org.commonvox.indexedcollection.MultiKeyEntry) put}
     * method of this IndexedCollection must adhere.
     */
    @SafeVarargs
    public IndexedCollection(Class<? extends V> valueClass,
                        String title, 
                        Class<? extends Key>... keyClassArray) {
        this(true, valueClass, title, keyClassArray);
    }
    
    /**
     * This constructor is used for instantiation of an IndexedCollection with
     * VALUE_CLASS which extends the {@link IndexedKey} interface.
     * For full description, see 
     * {@link IndexedCollection#IndexedCollection(java.lang.Class, java.lang.String, java.lang.Class...) }
     * @param title
     * @param valueClass
     * @param keyClassArray Array of {@link Key} Class objects defining order 
     * and type of {@link Key}s to which all {@link KeyArray}s in entries 
     * submitted to the {@link IndexedCollection#put(org.commonvox.indexedcollection.MultiKeyEntry) put}
     * method of this IndexedCollection must adhere.
     */
    @SafeVarargs
    public IndexedCollection(String title, 
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
     * be mapped by this IndexedCollection instance.
     * @param title {@link IndexedCollection#TITLE TITLE} of IndexedCollection, 
     * used mainly for reporting, development, and debugging purposes.
     * @param keyClassArray Array of {@link Key} Class objects defining order 
     * and type of {@link Key}s to which all {@link KeyArray}s in entries 
     * submitted to the {@link IndexedCollection#put(org.commonvox.indexedcollection.MultiKeyEntry) put}
     * method of this IndexedCollection must adhere.
     */
    @SafeVarargs
    protected IndexedCollection(int dummyInt,
                        Class<?> valueClass,
                        String title, 
                        Class<? extends Key>... keyClassArray) {
        this(true, valueClass, title, keyClassArray);
    }
    
    /**  
     * @param isTopLevelMap Indicates whether or not the IndexedCollection being 
     * instantiated is a "top level" map (i.e., accessible by a non-IndexedCollection 
     * object). Lower level IndexedCollection objects are only instantiated and 
     * accessed by higher level IndexedCollection objects.
     * @param valueClass Class object denoting the class of objects that will
     * be mapped by this IndexedCollection instance. The type variable must be of class
     * or subclass established by the type variable V used in instantiation
     * of the {@link org.commonvox.indexedcollection.IndexedCollection}.
     * @param title {@link IndexedCollection#TITLE TITLE} of IndexedCollection, 
     * used mainly for reporting, development, and debugging purposes.
     * @param keyClassArray Array of {@link Key} Class objects defining order 
     * and type of {@link Key}s to which all {@link KeyArray}s in entries 
     * submitted to the {@link IndexedCollection#put(org.commonvox.indexedcollection.MultiKeyEntry) put}
     * method of this IndexedCollection must adhere.
     */
    @SafeVarargs
    private IndexedCollection(boolean isTopLevelMap,
                        Class<?> valueClass,
                        String title, 
                        Class<? extends Key>... keyClassArray) {
        IndexedCollection.checkVarargs(keyClassArray);
        this.VALUE_CLASS = valueClass;
        this.TOP_LEVEL_MAP = isTopLevelMap;
        this.KEY_CLASS_ARRAY = keyClassArray;
        if (TOP_LEVEL_MAP) {
            this.TITLE = title;
            this.AUTOFILL_ARRAY_OF_GET_METHODS = buildKeyClassGetMethodArray();
        } else {
            this.AUTOFILL_ARRAY_OF_GET_METHODS = null;
        }
        nodeCount++;
    }

    /** This private constructor only usable by a IndexedCollection instance to 
     * create other (lower level) IndexedCollection instances.
     * @param title {@link IndexedCollection#TITLE TITLE} of IndexedCollection, 
     * used mainly for reporting, development, and debugging purposes.
     * @param keyClassArray array of Class objects defining order and type
     * of {@link Key}s which all key arrays submitted in a {@link MultiKeyEntry} 
     * to this IndexedCollection must adhere to. 
     * @param multiKeyEntries one or more {@link MultiKeyEntry} objects to be put into
     * the newly created IndexedCollection.
     * @throws InvalidMultiKeyException if any {@link Key} object in the
     * {@link MultiKeyEntry}'s {@link KeyArray} is not of the correct type 
     * and position as denoted by this IndexedCollection's 
     * {@link IndexedCollection#getKeyClassArray() KEY_CLASS_ARRAY}
     */
    @SafeVarargs
    private IndexedCollection (Class<?> valueClass,
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
     * Get total number of active nodes in all IndexedCollections
     * @return Total node count
     */
    public static long getNodeCount() {
        return nodeCount;
    }
    
    /**
     * This method may be invoked by any method which accepts vararg parameters,
     * but for which it is intended that a null or empty vararg array is invalid.
     * @param objectArray array of objects
     * @throws IllegalArgumentException if objectArray is null
     */
    public static void checkVarargs (Object[] objectArray) 
            throws IllegalArgumentException {
        if (objectArray == null || objectArray.length == 0) {
            throw new IllegalArgumentException 
                ("Null or zero-length array submitted for varargs parameters.");
        }
    }
    
    /**
     * Add an entry to this IndexedCollection. (Analogous to put method of a 
     * Map, except that in this case there are multiple keys.)
     * @param value value
     * @param keyArray an array of {@link Key}s
     * @return <code>true</code> if put succeeds <code>false</code> if put fails
     * @throws InvalidMultiKeyException if any {@link Key} object in the
     * {@link MultiKeyEntry}'s {@link KeyArray} is not of the correct type 
     * and position as denoted by this IndexedCollection's 
     * {@link IndexedCollection#getKeyClassArray() KEY_CLASS_ARRAY}
     */
    @SafeVarargs
    public final boolean put (V value, Key... keyArray) 
            throws InvalidMultiKeyException {
        return put(new MultiKeyEntry<>(value, keyArray));
    }
    
    /**
     * Add an entry to this IndexedCollection. (Analogous to put method of a 
     * Map, except that in this case there are multiple keys.)
     * @param multiKeyEntry containing value and an array of {@link Key}s
     * @return <code>true</code> if put succeeds <code>false</code> if put fails
     * @throws InvalidMultiKeyException if any {@link Key} object in the
     * {@link MultiKeyEntry}'s {@link KeyArray} is not of the correct type 
     * and position as denoted by this IndexedCollection's 
     * {@link IndexedCollection#getKeyClassArray() KEY_CLASS_ARRAY}
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
                    + "IndexedCollection <" + getTitle() + " LEVEL " 
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
            IndexedCollection<V> lowerIndexNode 
                    = this.middleMap.get(multiKeyEntry.getTopKey());
            MultiKeyEntry<V> lowerMultiKeyEntry 
                    = multiKeyEntry.getLowerMultiKeyEntry();
            if (lowerIndexNode == null) {
                this.middleMap.put
                    (multiKeyEntry.getTopKey(), 
                        new IndexedCollection<V>(this.VALUE_CLASS, this.TITLE, 
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
     * Get the "depth" of this IndexedCollection as denoted by the number of 
     * entries in its {@link IndexedCollection#KEY_CLASS_ARRAY KEY_CLASS_ARRAY}.
     * @return The "depth" of this IndexedCollection as denoted by the number of 
     * entries in its {@link IndexedCollection#KEY_CLASS_ARRAY KEY_CLASS_ARRAY}.
     */
    public int getDepth () {
        return this.KEY_CLASS_ARRAY.length;
    }

    /**
     * Get this IndexedCollection's {@link IndexedCollection#TITLE TITLE}
     * @return {@link IndexedCollection#TITLE TITLE}
     */
    public String getTitle () {
        return this.TITLE;
    }
    
    /**
     * Get this IndexedCollection's {@link IndexedCollection#KEY_CLASS_ARRAY KEY_CLASS_ARRAY}
     * @return {@link IndexedCollection#KEY_CLASS_ARRAY KEY_CLASS_ARRAY}
     */
    public Class<? extends Key>[] getKeyClassArray() {
        return this.KEY_CLASS_ARRAY;
    }
    
    /**
     * Invoked to get all values contained in this IndexedCollection, in the 
     * order in which they are indexed.
     * @return All values contained in this IndexedCollection, in the order in
     * which they are indexed.
     */
    public List<V> selectAll () {
        List<V> vList = new ArrayList<>();
        if (bottomMap != null && !bottomMap.isEmpty()) {
            vList.addAll(this.bottomMap.values());
        }
        if (middleMap != null && !middleMap.isEmpty()) {
            for (IndexedCollection<V> lowerIndexNode : this.middleMap.values()) {
                vList.addAll(lowerIndexNode.selectAll());
            }
        }
        return vList;
    }
    
    /**
     * Invoked to get the first value with index matching the submitted keyArray
     * @param keyArray full or partial {@link Key} array
     * @return the first value with index matching the submitted keyArray 
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
     * Invoked to get all values with index matching the submitted keyArray
     * @param keyArray full or partial {@link Key} array
     * @return all values with index matching the submitted keyArray 
     */
    @SafeVarargs
    public final List<V> get (Key... keyArray) {
        return get(new KeyArray(keyArray));
    }
    
    /**
     * Invoked to get all values with index matching the submitted keyArray
     * @param keyArray full or partial {@link KeyArray} 
     * @return all values with index matching the submitted keyArray 
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
                for (IndexedCollection<V> lowerIndexNode : this.middleMap.values()) {
                    vList.addAll(lowerIndexNode.get(keyArray.getLowerKeyArray()));
                }
            } else {
                if (this.middleMap.containsKey(keyArray.getTopKey())) {
                    IndexedCollection<V> lowerIndexNode 
                            = this.middleMap.get(keyArray.getTopKey());
                    vList.addAll
                        (lowerIndexNode.get(keyArray.getLowerKeyArray()));
                }
            }
        }
        return vList;
    }
    
    /**
     * Get Class of the values indexed in this IndexedCollection
     * @return Class of the values indexed in this IndexedCollection
     */
    public Class<?> getValueClass() {
        return this.VALUE_CLASS;
    }
    
    /**
     * Size of this IndexedCollection, as denoted by the number of unique value
     * objects it contains
     * @return Size of this IndexedCollection, as denoted by the number of 
     * unique value objects it contains
     */
    public int size() {
        int size = 0;
        if (bottomMap != null && !bottomMap.isEmpty()) {
            size += this.bottomMap.size();
        } 
        if (middleMap != null && !middleMap.isEmpty()) {
            for (IndexedCollection<V> lowerIndex : this.middleMap.values()) {
                size += lowerIndex.size();
            }
        }
        return size;
    }

    private Set<MultiKeyEntry<V>> getMultiKeyEntrySet() {
        Set<MultiKeyEntry<V>> multiKeyEntrySet = new LinkedHashSet<>();
        if (bottomMap != null && !bottomMap.isEmpty()) {
            for (Entry<Key, V> entry : bottomMap.entrySet()) {
                multiKeyEntrySet.add
                    (new MultiKeyEntry<>(entry.getValue(), entry.getKey()));
            }
        } 
        if (middleMap != null && !middleMap.isEmpty()) {
            for (Entry<Key, IndexedCollection<V>> entry 
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
            if (typeOfObjectsInList.getTypeName().equals(keyClass.getTypeName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Indicates whether this IndexedCollection is enabled for auto-fill.
     * It should be if the {@link IndexedCollection#getValueClass() VALUE_CLASS}
     * has a non-private get method (returning a single object or a collection)
     * for each type denoted in {@link IndexedCollection#getKeyClassArray() KEY_CLASS_ARRAY}
     * @return <code>true</code> if this is auto-fill enabled,
     * <code>false</code> if not
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
            + "     SIZE OF INDEXED COLLECTION = " + this.size()
            + " ; DEPTH OF INDEXED COLLECTION = " + this.KEY_CLASS_ARRAY.length;
    }
    
    /**
     * Prints verbose listing of IndexedCollection contents; 
     * mainly for test/debug.
     */
    public void dumpContents() {
        StringBuilder output;
        boolean firstItemPrinted;
        printHeadingWithTimestamp
            ("Dump of <" + this.getTitle() + "> IndexedCollection", this.size());
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
        output.append("Key classes for this IndexedCollection are: {");
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
    
    private static void printHeading(String headingTitle) {
        String headingBorder 
            = new String(new char[headingTitle.length()]).replace("\0", "=");

        System.out.println(headingBorder);
        System.out.println(headingTitle);
        System.out.println(headingBorder);
    }
    
    private static void printHeadingWithTimestamp
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
