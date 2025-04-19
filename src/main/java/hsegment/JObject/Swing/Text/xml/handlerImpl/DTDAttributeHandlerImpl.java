package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.xml.dtd.DTDAttribute;
import hsegment.JObject.Swing.Text.xml.dtd.DTDAttributeContent;
import hsegment.JObject.Swing.Text.xml.error.SourceError;
import hsegment.JObject.Swing.Text.xml.handler.DTDAttributeHandler;
import hsegment.JObject.Swing.Text.xml.handler.ErrorHandler;
import hsegment.JObject.util.FunctionUtils;

public class DTDAttributeHandlerImpl implements DTDAttributeHandler {
    private final ErrorHandler errorHandler;

    public DTDAttributeHandlerImpl(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    @Override
    public void handleDTDAttribute(DTDAttribute dtdAttribute, SourceError sourceError) {
        verifyName(dtdAttribute.getElementName(), sourceError);
        for(DTDAttributeContent content : dtdAttribute.getAttributeList()){
            verifyName(content.getAttributeName(), sourceError);
        }
    }

    private void verifyName(String name, SourceError sourceError) {
        if(!FunctionUtils.verifyStartName(name)){
            errorHandler.errorHandler(FunctionUtils.getSourceError(sourceError.getRowIndex(), sourceError.getColumnIndex())
                    ,"Invalid start character of element name or attribute name : "+name,
                    "Correct the character !", ErrorType.FatalError);
        }
        if(!FunctionUtils.verifyName(name)){
            errorHandler.errorHandler(FunctionUtils.getSourceError(sourceError.getRowIndex(), sourceError.getColumnIndex()),
                    "Invalid name of element name or attribute name : "+name,
                    "Correct the name !", ErrorType.FatalError);
        }
    }
}
