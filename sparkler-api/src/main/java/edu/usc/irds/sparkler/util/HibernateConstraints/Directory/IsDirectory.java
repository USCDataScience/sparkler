package edu.usc.irds.sparkler.util.HibernateConstraints.Directory;



import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Created by shashank on 4/3/17.
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = IsDirectoryValidator.class)
public @interface IsDirectory {
    String message() default "The String is not a directory";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}