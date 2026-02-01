package logic.description;

import logic.description.gen.generatorObj;
import objects.figure.figures;
import java.util.List;
import logic.description.rtranslator.CreateRCode;
import logic.description.rtranslator.RTranslatorClass;
import logic.description.rtranslator.newPrecodeGenerator;
import logic.serialization.model.ConvertedObject;

public class DescriptionService {

    public String generateDescription(ConvertedObject convertedObject) {
        if (convertedObject == null) {
            return "";
        }
        generatorObj genOb = new generatorObj(convertedObject);
        return genOb.generateString();
    }

    public String generateRCode(List<figures> all, String descriptionText) {
        if (all == null) {
            return "";
        }
        newPrecodeGenerator preCode = new newPrecodeGenerator(new java.util.ArrayList<>(all));
        RTranslatorClass newRTC = new RTranslatorClass(preCode.getPrecodeString());
        newRTC.addString(descriptionText == null ? "" : descriptionText);
        return newRTC.getStringRCode();
    }

    public void saveRCodeToFile(String text, String file) {
        CreateRCode.saveInFile(text, file);
    }
}
