package hsegment.JObject.Swing.Text.xml.handlerImpl;
import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.xml.Content;
import hsegment.JObject.Swing.Text.xml.ContentModel;
import hsegment.JObject.Swing.Text.xml.Element;
import hsegment.JObject.Swing.Text.xml.handler.ElementHandler;
import hsegment.JObject.Swing.Text.xml.handler.ErrorHandler;
import hsegment.JObject.util.FunctionUtils;

public class ElementHandlerImpl implements ElementHandler {
    private final ErrorHandler errorHandler;

    public ElementHandlerImpl(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    @Override
    public void handleContentModel(ContentModel contentModel, int rowIndex, int columnIndex) {
        Content content;
        if(!contentModel.getValues().isEmpty()){
            content = contentModel.getValues().get(0);
            String elementType = content.getName();
            System.out.println("Handle content model element name :"+elementType);
            if(!(verifyElementType(elementType) || verifyName(elementType, rowIndex, columnIndex))){
                errorHandler.errorHandler(FunctionUtils.getSourceError(rowIndex, columnIndex),
                        "Invalid element type : "+elementType, "Remove or correct the type", ErrorType.FatalError);
            }
            for(int i = 1; i < contentModel.getValues().size(); i++){
                content = contentModel.getValues().get(i);
                System.out.println("Handle content model element name :"+content.getName());
                verifyName(content.getName(), rowIndex, columnIndex);
            }
        }

    }

    @Override
    public void handleElement(Element element) {
        verifyName(element.getName(), element.getRowIndex(), element.getColIndex());
    }

    @Override
    public void handleElementType(String elementType, int rowIndex, int columnIndex) {
        if(!FunctionUtils.verifyElementType(elementType)){
            errorHandler.errorHandler(FunctionUtils.getSourceError(rowIndex, columnIndex),
                    "Invalid element type : "+elementType, "Remove or correct the type", ErrorType.FatalError);
        }
    }

    private boolean verifyName(String name, int rowIndex, int columnIndex) {
        if(!FunctionUtils.verifyStartName(name)){
            errorHandler.errorHandler(FunctionUtils.getSourceError(rowIndex, columnIndex),
                    "Invalid start character of element name : "+name,"Add correct character !", ErrorType.FatalError);
        }
        if(!FunctionUtils.verifyName(name)){
            errorHandler.errorHandler(FunctionUtils.getSourceError(rowIndex, columnIndex),
                    "Invalid element name : "+name,"Add correct characters !", ErrorType.FatalError);
        }
        return true;
    }

    private boolean verifyElementType(String elementType){
        return FunctionUtils.verifyElementType(elementType);
    }
}
