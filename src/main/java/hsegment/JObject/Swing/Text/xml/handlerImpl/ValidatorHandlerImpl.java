/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.handler.ValidatorHandler;

/**
 *
 * @author DELL
 */
public class ValidatorHandlerImpl implements ValidatorHandler {

    @Override
    public String getValidatorName() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getLocationType() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String ValidatorFilePath() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void handleValidator(String ValidatorName, String Location, String validatorFilePath) {
        System.out.println("Call handleDoctype : \n ==> ValidatorName = "+ValidatorName+
        "; Location = "+Location+"; validatorFilePath = "+validatorFilePath);
    }
    
}
