/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 *
 * @author FIDELE
 */
public class DomException extends RuntimeException {

    public short code;

    public DomException(short code, String message) {
        super(message);
        this.code = code;
    }

    // ExceptionCode
    /**
     * If index or size is negative, or greater than the allowed value.
     */
    public static final short INDEX_SIZE_ERR = 1;
    /**
     * If the specified range of text does not fit into a
     * <code>DOMString</code>.
     */
    public static final short DOMSTRING_SIZE_ERR = 2;
    /**
     * If any <code>Node</code> is inserted somewhere it doesn't belong.
     */
    public static final short HIERARCHY_REQUEST_ERR = 3;
    /**
     * If a <code>Node</code> is used in a different document than the one that
     * created it (that doesn't support it).
     */
    public static final short WRONG_DOCUMENT_ERR = 4;
    /**
     * If an invalid or illegal character is specified, such as in an XML name.
     */
    public static final short INVALID_CHARACTER_ERR = 5;
    /**
     * If data is specified for a <code>Node</code> which does not support data.
     */
    public static final short NO_DATA_ALLOWED_ERR = 6;
    /**
     * If an attempt is made to modify an object where modifications are not
     * allowed.
     */
    public static final short NO_MODIFICATION_ALLOWED_ERR = 7;
    /**
     * If an attempt is made to reference a <code>Node</code> in a context where
     * it does not exist.
     */
    public static final short NOT_FOUND_ERR = 8;
    /**
     * If the implementation does not support the requested type of object or
     * operation.
     */
    public static final short NOT_SUPPORTED_ERR = 9;
    /**
     * If an attempt is made to add an attribute that is already in use
     * elsewhere.
     */
    public static final short INUSE_ATTRIBUTE_ERR = 10;
    /**
     * If an attempt is made to use an object that is not, or is no longer,
     * usable.
     *
     * @since 1.4, DOM Level 2
     */
    public static final short INVALID_STATE_ERR = 11;
    /**
     * If an invalid or illegal string is specified.
     *
     * @since 1.4, DOM Level 2
     */
    public static final short SYNTAX_ERR = 12;
    /**
     * If an attempt is made to modify the type of the underlying object.
     *
     * @since 1.4, DOM Level 2
     */
    public static final short INVALID_MODIFICATION_ERR = 13;
    /**
     * If an attempt is made to create or change an object in a way which is
     * incorrect with regard to namespaces.
     *
     * @since 1.4, DOM Level 2
     */
    public static final short NAMESPACE_ERR = 14;
    /**
     * If a parameter or an operation is not supported by the underlying object.
     *
     * @since 1.4, DOM Level 2
     */
    public static final short INVALID_ACCESS_ERR = 15;
    /**
     * If a call to a method such as <code>insertBefore</code> or
     * <code>removeChild</code> would make the <code>Node</code> invalid with
     * respect to "partial validity", this exception would be raised and the
     * operation would not be done. This code is used in
     * [<a href='http://www.w3.org/TR/2004/REC-DOM-Level-3-Val-20040127/'>DOM
     * Level 3 Validation</a>] . Refer to this specification for further
     * information.
     *
     * @since 1.5, DOM Level 3
     */
    public static final short VALIDATION_ERR = 16;
    /**
     * If the type of an object is incompatible with the expected type of the
     * parameter associated to the object.
     *
     * @since 1.5, DOM Level 3
     */
    public static final short TYPE_MISMATCH_ERR = 17;

    // Added serialVersionUID to preserve binary compatibility
    static final long serialVersionUID = 6627732366795969916L;
}
