package org.tdl.vireo.model.validation;

import edu.tamu.framework.enums.InputValidationType;
import edu.tamu.framework.validation.BaseModelValidator;
import edu.tamu.framework.validation.InputValidator;

public class NamedSearchFilterValidator extends BaseModelValidator {
    
    public NamedSearchFilterValidator() {
        String nameProperty = "name";
        this.addInputValidator(new InputValidator(InputValidationType.required, "Named Search Filter requires a name", nameProperty, true));
        this.addInputValidator(new InputValidator(InputValidationType.minLength, "Named Search Filter name must be at least 2 characters", nameProperty, 2));
        this.addInputValidator(new InputValidator(InputValidationType.maxLength, "Named Search Filter name cannot be more than 50 characters", nameProperty, 50));
        
        String creatorProperty = "creator";
        this.addInputValidator(new InputValidator(InputValidationType.required, "Field Value requires a creator", creatorProperty, true));
    }
    
}