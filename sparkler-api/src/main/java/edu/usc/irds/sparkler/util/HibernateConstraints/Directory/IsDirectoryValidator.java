package edu.usc.irds.sparkler.util.HibernateConstraints.Directory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;

/**
 * Created by shashank on 4/3/17.
 */
public class IsDirectoryValidator implements ConstraintValidator<IsDirectory, String> {


    @Override
    public void initialize(IsDirectory isDirectory) {

    }

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintValidatorContext) {
        if (object == null || !(new File(object.toString()).isDirectory())) return true;
        else return false;
    }
}
