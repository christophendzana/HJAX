/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * The {@code Dictionnary} class is a growable array of objects. 
 * like an array it can content object which can be accessible 
 * by integer Index or by string using {@code toString} method's object.  
 * The size of {@code Dictionnary} can grow or shrink as needed to 
 * accomoded adding and removing items after {@code Dictionnary} 
 * has been created.
 * 
 * <p>{@code Dictionnary} class store object by alphabetic order. 
 * for getting object stored, using {@link Dictionnary#get(String)} 
 * exemple: <blockquote> Dictionnary dictionnary = new Dictionnary() 
 * <br/> <pre> dictionnary.get(o.toString)</pre></blockquote> or 
 * accessing to object by index using {@link Dictionnary#get(int) } 
 * exemple: <blockquote> Dictionnary dictionnary = new Dictionnary() 
 * <br/> <pre> dictionnary.get(0)</pre></blockquote> .Insertion of 
 * object into {@code Dictionnary} class take more time than other 
 * collection but time accessibility of object is to low than other 
 * collection cause {@code Dictionnary} don't loop over all stored 
 * object like other collection but only over those which start 
 * with same character.
 * 
 *<p> And then it is important to initialise {@code initialCapacity} 
 * and {@code capacityIncrement} for more performance. {@code initialCapacity} 
 * shouldn't be &gt; 27 otherwise it will return to 27.
 * 
 * <p> this collection can be case sensitive means that if 
 * {@code caseSensitive} egual to true storing and getting element become 
 * case sensitive too. So two or more different objects (<code>O.equal(o1)</code> return false) with same name but different 
 * sensitive case should be considering like different object when {@code caseSensitive} is true 
 * but when {@code caseSensitive} egual to false two or more objects should be considered like a same object 
 * if they have same name.
 * 
 * <p> this collection can store several object with same name but by default collection 
 * do not accept many object with same name and if that behavior want to be change 
 * method {@link Dictionnary#setDuplicateStateTo(boolean) } should be used and 
 * method {@link Dictionnary#isAcceptDuplicate() } should be use to check that 
 * behavior
 * 
 * @author Ndzana Christophe
 * @param <E> Type of component elements
 */
public class Dictionnary<E> 
        implements Collection<E>, List<E>, RandomAccess, Cloneable, java.io.Serializable 
{
    
    
    protected Object[] columns;
    protected Vector<Entry> entries;
    
    
    /**
     * number of non null column already created
     */
    protected int columnCount = 0; 
    
    /**
     * capacity of incrementation
     */
    protected int capacityIncrement = 0; 
    /**
     * Initial Array capacity. initial columns's and Lines's capacity
     */
    protected int initialCapacity = 0;
    
    /**
     * number of element already created
     */
    protected int entryIndex = 0;
    
    /**
     * if this collection accept more object with same name this variable specify 
     * how many object have same name
     */
    private int distance = 0;
    
    
    /**
     * if Dictionnary should be case sensitive or not.
     * if value is true Dictionnary become case sensitive or not if false
     */
    private boolean caseSensitive = false;
    /**
     * If Dictionnary accept more than one object with same name.
     * value is true when Dictionnary accept more element with 
     * same name
     */
    private boolean duplicating = false;
    
    /**
     * Create constructor with initial parameters.
     * @param initialCapacity initial capacyty of this dictionnary
     * @param capacityIncrement encrementation footprint
     * @param caseSensitive true if Dictionnary is case sensitive
     */
    public Dictionnary(int initialCapacity, int capacityIncrement, boolean caseSensitive, boolean duplicating){
        if(initialCapacity < 0 || capacityIncrement < 0)
            throw new IllegalArgumentException("Illegal Capacity. "+initialCapacity);
        
        this.capacityIncrement = Math.max(1, Math.min(5, capacityIncrement));
        this.initialCapacity = Math.max(1, Math.min(27, initialCapacity));
        
        columns = new Object[this.initialCapacity];
        entries = new Vector<Entry>(this.initialCapacity, this.capacityIncrement);
        this.caseSensitive = caseSensitive;
        this.duplicating = duplicating;
        
    }
    
    public Dictionnary(int initialCapacity){
        this(initialCapacity, 1, false, false);
    }
    
    public Dictionnary(){
        this(1, 1, false, false);
    }
    
    public Dictionnary(Collection<? extends E> c){
        this(1, 1, false, false);
        if(c == null){
            throw new NullPointerException("can't add a null list");
        }
        
        for(E e : c)
            add(e);
    }
    
    /**
     * Determine if this collection accept to store several different element with 
     * same name. If <code>duplicateState</code> is true, collection 
     * should be able to store several different object with same name.
     * <code>duplicateState</code> : is false by default.
     * 
     * 
     * 
     * @param duplicateState true if collection accept duplicating object, 
     * false otherwise
     * @see #isAcceptDuplicate() 
     */
    public void setDuplicateStateTo(boolean duplicateState){
        this.duplicating = duplicateState;
    }
    
    /**
     * set this collection to be case sensitive or not. Note that if this collection 
     * is case sensitive to true two or more object with same name but diffentase are 
     * considered different by this collection.à
     * 
     * 
     * 
     * @param sensitivity true if this collection is case sensitive or not
     * @see #isCaseSensitive() 
     */
    public void setCaseSensitiveTo(boolean sensitivity){
        this.caseSensitive = sensitivity;
    }
    
    /**
     * Return true if collection accept several object with same name
     * and return false otherwise
     * @return true if similar object is accepted and false if not
     */
    public boolean isAcceptDuplicate(){
        return this.duplicating;
    }
    
    /**
     * Return true if collection is case sensitive and false otherwise
     * @return true if collection is case sensitive and false otherwise
     */
    public boolean isCaseSensitive(){
        return caseSensitive;
    }
    
    /**
     * Return a first object which name into <code>toString</code> method correspond to
     * <code>name</code>.
     * @param name object's name
     * @return first object with name equal to <code>name</code>
     */
    public E get(String name){
        for(Object column : columns){
            if(((Column)column).isCharIdentification(name.charAt(0))){
                for(Object line : ((Column)column).lines){
                    
                    boolean equal = caseSensitive ? name.equals(((Line)line).element.toString()) : 
                                              name.equalsIgnoreCase(((Line)line).element.toString());
                    if(equal)
                        return (E)((Line)line).element;
                }
                return null;
            }
        }
        return null;
    }
    
    public void get(Collection<E> objects, String name){}
    
    public ListIterator<E> listIterator(char c) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    
    public List<E> subList(char c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {return entries.size();}
    

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {return false;}

    @Override
    public Iterator<E> iterator() {return null;}

    @Override
    public Object[] toArray() {
    
        return null;
    }
    @Override
    public <T> T[] toArray(T[] a) {return null;}

    /**
     * <p>add {@code e} to this collection and return true if adding is been done or false 
     * otherwhise. This collection doesn't accept null objects.If {@code caseSensitive} is 
     * parametred to true two or more objects with same name but with different case are considered 
     * by this collection as different. but if {@code caseSensitive} is 
     * parametred to false two or more object with same name but with 
     * different case are considered as same object.
     * 
     * 
     * @param e object to add to this collection
     * @return true if adding is been done or false otherwise
     * @throws NullPointerException if {@code e} is null
     */
    @Override
    public synchronized boolean add(E e) {
        if(e == null){
            throw new NullPointerException("can't add null object");
        }
        distance = 0;
        int columnIndex = getColumn(e); 
        int lineIndex = getLine(e, columnIndex);
        return addImpl(e, columnIndex, lineIndex);
    }
    
    private boolean addImpl(E e, int columnIndex, int lineIndex){
        
        if(lineIndex == -1)
            return false;
        
        try {
            
                Column column = (Column)columns[columnIndex];//May throw IndexOutOfBoundsException
                
                if(!column.isCharIdentification(e.toString().charAt(0))){ //May throw NullPointerException
                    insertColumnto(columnIndex);
                    columnCount++;
                }
            
        } catch (NullPointerException ex) {
        
            columns[columnIndex] = new Column(initialCapacity);
        } catch(IndexOutOfBoundsException ex){
            growColumns();
            Column newColumn = new Column(initialCapacity);
            columns[columnIndex] = newColumn;
            columnCount++;
        }
        
        Column column = (Column)columns[columnIndex];
        column.addNewLine(e, lineIndex, entryIndex++);
        return entries.add(new Entry(columnIndex, lineIndex));
    }
    
    /**
     * Grow array which contains columns
     */
    private void growColumns(){
        Object[] newColumns = new Object[columns.length + this.capacityIncrement];
        System.arraycopy(columns, 0, newColumns, 0, columns.length);
        columns = newColumns;
    }
    
    /**
     * Insert a new column to index <code>columnIndex</code> and shift to rigth side 
     * column at <code>columnIndex</code>.
     * @param columnIndex column's index where insertion should be done
     * @return true if insertion have been done successfuly false otherwise
     */
    private boolean insertColumnto(int columnIndex){
        Object[] newColumns = null;
        boolean b = false;
        if(columns.length <= columnCount)
            newColumns = new Object[columns.length + this.capacityIncrement];
        
        for(int y = 0, i = 0; i < newColumns.length; i++){
            if(i == columnIndex){
                newColumns[i] = new Column(this.initialCapacity);
                b = true;
            } else {
                newColumns[i] = columns[y];
                y++;
            }
        }
        
        return b;
    }
    
    /**
     * <p>Return true if <code>e</code> should belong to column at index <code>columnIndex</code> 
     * or false otherwise. <code>e</code> belongs to column if it first character is same than 
     * all of element contained to this column or it belongs to this column if column not have 
     * yet element and so <code>e</code> is the first one.
     * 
     * 
     * @param e object to test column belonging
     * @param columnIndex column's index to match
     * @return true if <code>e</code> should belong to column at index <code>columnIndex</code> 
     * or false otherwise
     */
    private boolean isBelongsToColumn(E e, int columnIndex){
        boolean b = false;
        try {
            Column column = (Column)columns[columnIndex];
            
            b = column.isCharIdentification(e.toString().charAt(0));
        } catch (NullPointerException ex) {
            //Normaly when this exception occur it means that next Column will be 
            //created at that index
            b = true;
        } catch(IndexOutOfBoundsException ex){
            b = false;
        }
        
        
        return b;
    }
    
    /**
     * return this character' index into alphabet
     * @param c character we want to know his position into the alphabet
     * @return character's position into alphabet
     */
    private int getPositionIntoAlphabet(char c){
        int position = -1;
        
        switch(c){
            case 'a':  case 'A': position = 1; break;
            case 'b':  case 'B': position = 2; break;
            case 'c':  case 'C': position = 3; break;
            case 'd':  case 'D': position = 4; break;
            case 'e':  case 'E': position = 5; break;
            case 'f':  case 'F': position = 6; break;
            case 'g':  case 'G': position = 7; break;
            case 'h':  case 'H': position = 8; break;
            case 'i':  case 'I': position = 9; break;
            case 'j':  case 'J': position = 10; break;
            case 'k':  case 'K': position = 11; break;
            case 'l':  case 'L': position = 12; break;
            case 'm':  case 'M': position = 13; break;
            case 'n':  case 'N': position = 14; break;
            case 'o':  case 'O': position = 15; break;
            case 'p':  case 'P': position = 16; break;
            case 'q':  case 'Q': position = 17; break;
            case 'r':  case 'R': position = 18; break;
            case 's':  case 'S': position = 19; break;
            case 't':  case 'T': position = 20; break;
            case 'u':  case 'U': position = 21; break;
            case 'v':  case 'V': position = 22; break;
            case 'w':  case 'W': position = 23; break;
            case 'x':  case 'X': position = 24; break;
            case 'y':  case 'Y': position = 25; break;
            case 'z':  case 'Z': position = 26; break;
            default : position = c; break;
        }
        
        return this.caseSensitive ? c : position;
    }
    
    
    private int compare(char x, char y){
        
        return Character.compare(Character.toLowerCase(x), Character.toLowerCase(y));
    }
    /**
     * get this object column's index.this collection is structured like a table there is column 
     * which identifiant is a first letter of object and then this method return column 
     * index which object <code>e</code> belong to.
     * @param e object which column's index is return
     * @return column's index of object <code>e</code>
     */
    private int getColumn(Object e) throws NullPointerException{
        int columnIndex = 0;
        char newEChar = e.toString().trim().charAt(0);
        for(int i = 0; i < columns.length; columnIndex = i++){//here we loop column(s) which can contain line(s)
               
               try {
                    Column  column = (Column)columns[i];
                    char oldEChar = (char)column.getCharIdentification();//may throw NullPointerException
                    
                    int codePoint = (int)Character.toLowerCase(newEChar);
                    int codePoint1 = (int)Character.toLowerCase(oldEChar);
                    
                    if(codePoint <= codePoint1){
                        columnIndex = i;
                        break;
                    }
                    
                } catch (NullPointerException ex){
                    columnIndex = i;
                    break;
                }
               
            }
        
        return columnIndex;
               
    }
    
    /**
     * return index's line of object <code>e</code> into column which index is 
     * <code>columnIndex</code>, if <code>columnIndex</code> is greather than 
     * column array this method return 0 and when <code>columnIndex</code> is 
     * &lt;0 IndexOutOfBoundException is thrown
     * @param e
     * @param columnIndex
     * @return 
     */
    private int getLine(Object e, int columnIndex)
                   throws IndexOutOfBoundsException{
        
        if(columnIndex < 0)
            throw new IndexOutOfBoundsException("ColumnIndex should be greather than 0");
        
        int lineIndex = -1;
        char[] name = e.toString().trim().toCharArray();
        Line line = null;
        char c = '0';
        char c1 = '0';
        boolean differentByCase = false;
        try {
            /**
             * Column is collected at his index. If index <code>columnIndex</code>is greather than column array
             * <code>columns</code> IndexOutOfBoundsException is thrown, this case occur when column array is full
             * and in this case a new column will be created
             */
            
            Column column = (Column)columns[columnIndex];//May throw IndexOutOfBoundsException
            
            /**
             * Lines are collected. NullPointerExeption is thrown when column at index <code>columnIndex</code> 
             * is null, this case occured when column array have some extra space
             */
            Object[] lines = column.getLines();//May throw NullPointerException
            
            for(int i = 0; i < lines.length; i++){
                line = (Line)lines[i];
                lineIndex = -1;
                differentByCase = false;
                
                //this case occur when line array have been initialised with extra space
                if(line == null){
                  lineIndex = i;
                  break;
                }
                    
                
                char[] eName = line.getElement().toString().trim().toCharArray();
                
                /**
                 * comparison between adding object and line object is made only on 
                 * the one which have a smaller character.
                 */
                for(int y = 0; y < Math.min(eName.length, name.length); y++){
                    c = name[y];
                    c1 = eName[y];
                    
                    if(compare(c, c1) < 0){
                        lineIndex = i;
                        i = lines.length;//index's line is found so loop over lines should stop
                        break; //stop loopping over characters
                    } else if(compare(c, c1) > 0){
                        lineIndex = i + 1;
                        break;
                    } else{
                       differentByCase = c1 != c; 
                    }
                }
                if(lineIndex != -1){
                    continue;
                }
                //if lineIndex is different to -1 there is three possibility
                if(eName.length > name.length){//first one is when line's object characters is longer than inserted object
                    lineIndex = i;
                    break;
                } else if(eName.length < name.length){//second one is inverse case
                    lineIndex = i + 1;
                    continue;
                }
                //and the third one is when compared string have egual length
                if(caseSensitive && differentByCase){//in this case the two object are different by this collection logiq
                    lineIndex = i;
                    break;
                } else if((!caseSensitive && isAcceptDuplicate()) || 
                        (caseSensitive && !differentByCase && isAcceptDuplicate())){
                    lineIndex = i;
                    distance = line.distance + 1;
                    break;
                }
                
            }
            
            
        } catch (Exception ex) {
        
            lineIndex = 0;
        }
        
        return lineIndex;
    }

    @Override
    public boolean remove(Object o) {return false;}

    @Override
    public boolean containsAll(Collection<?> c) {return false;}

    @Override
    public boolean addAll(Collection<? extends E> c) {return false;}

    @Override
    public boolean removeAll(Collection<?> c) {return false;}

    @Override
    public boolean retainAll(Collection<?> c) {return false;}

    @Override
    public void clear() {}

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        List.super.replaceAll(operator);
    }

    @Override
    public E get(int index) {
        try {
            Entry entry = entries.get(index);
            Column column = (Column)columns[entry.column];
            Line line = (Line)column.lines[entry.line];
            return (E)line.getElement();
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOf(Object o) {
        
        try {
            int columnIndex = getColumn(o);
            int lineIndex = getLine(o, columnIndex);
            Column column = (Column)columns[columnIndex];
            Line line = (Line)column.lines[lineIndex];
           return line.entryIndex;
            
        } catch (Exception e) {
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        try {
            int columnIndex = getColumn(o);
            int lineIndex = getLine(o, columnIndex);
            Column column = (Column)columns[columnIndex];
            Line line = (Line)column.lines[lineIndex];
            Line clone = (Line)column.lines[lineIndex + line.distance];
            
            return clone.entryIndex;
        } catch (Exception e) {
        }
        
        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    

    @Override
    public List<E> reversed() {
        return List.super.reversed();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return List.super.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return List.super.removeIf(filter);
    }

    @Override
    public Stream<E> stream() {
        return List.super.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return List.super.parallelStream();
    }

    @Override
    public Spliterator<E> spliterator() {
        return List.super.spliterator(); 
    }

    @Override
    public void sort(Comparator<? super E> c) {
        
    }
    

    @Override
    public void addFirst(E e) {
        
    }

    @Override
    public void addLast(E e) {
        
    }

    @Override
    public E getFirst() {
        return null;
    }

    @Override
    public E getLast() {
        return null;
    }

    @Override
    public E removeFirst() {
        return null;
    }

    @Override
    public E removeLast() {
        return null;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
    /**
     * {@code Dictionnary} is a collection which store element by column 
     * and each column contains several lines and each line should 
     * encapsulate column, line and index.
     */
    private class Entry {
        
        private int column = -1, line = -1;
        
        /**
         * Init line with references
         * @param index position into this collection
         * @param column column which this line belongs to
         * @param line index into column
         */
        private Entry(int column, int line){
            this.column = column;
            this.line = line;
        }
        
        
    }
    
    
    private class Line<E> {
        
        E element;
        int entryIndex = -1;
        
        /**
         * 
         */
        int distance = 0;
        private Line(E element, int entryIndex, int distance){
            this.element = element;
            this.entryIndex = entryIndex;
        }
        
        void setEntryIndex(int entryIndex){
            this.entryIndex = entryIndex;
        }
        
        int getEntryIndex(){
            return this.entryIndex;
        }
        
        E getElement(){
            return element;
        }
    }
    
    private class Column{
        Object[] lines;
        /**
         * Number of element already added into column
         */
        int elementCount;
        private Column(int initialCapacity){
            lines = new Object[initialCapacity];
        }
        /**
         * Return this column's lines
         * @return lines contained into this column
         */
        Object[] getLines(){
            return lines;
        }
        /**
         * Rturn line at index <code>index</code>
         * @param index index of line to return
         * @return Line at index <code>index</code>
         * @throws NullPointerException
         */
        Line getLine(int index){
            return (Line)lines[index];
        }
        /**
         * grow this column's line
         */
        void growLines(){
            Object[] newLines = new Object[lines.length + capacityIncrement];
            System.arraycopy(lines, 0, newLines, 0, lines.length);
            lines = newLines;
        }
        /**
         * Return number of element already added into this column
         * @return number of element added into this column;
         */
        int getElementCount(){
            return elementCount;
        }
        
        /**
         * return true if this column contain line with object witch 
         * start with character {@code c}
         * @param c identification caracter
         * @return true if {@code c} is this column character identification 
         * caracter
         */
        boolean isCharIdentification(int c){
            boolean b = false;
            try {
                b = ((Line)lines[0]).element.toString().charAt(0) == (char)c;//May throw NullPointerException
            } catch (NullPointerException e) {
                b = false;
            }
            
            return b;
        }
        /**
         * add new line to index <code>lineIndex</code>
         * @param e object stored to the line
         * @param lineIndex line's index
         * @param entryIndex Index of this stored object into collection
         * @return true if new line is succesfully added
         */
        boolean addNewLine(E e, int lineIndex, int entryIndex){
            try {
                Line line = (Line)lines[lineIndex];
                Object[] newLines = new Object[lines.length <= elementCount ? 
                                                       (lines.length + capacityIncrement) :
                                                             lines.length];
                for(int y = 0, i = 0; i< lines.length; i++){
                    if(i != lineIndex){
                        newLines[i] = lines[y];
                        y++;
                    }
                        
                }
                lines = newLines;
                
            } catch (IndexOutOfBoundsException ex) {
                this.growLines(); 
            }
            
            lines[lineIndex] = new Line(e, entryIndex, distance);
            elementCount++;
            return true;
        }
        /**
         * <p>return character which identify this column. 
         * <p>Every line in this column 
         * have an Object and each object's name start with same character that why the 
         * first line have been chosen to identify which is that character 
         * 
         * 
         * @return this column character Identification
         * @throws NullPointerException if there is no element into line
         */
        int getCharIdentification(){
            return ((Line)lines[0]).element.toString().trim().charAt(0) ;
        }
        
    }
}
