package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;
import edu.usc.irds.sparkler.SparklerConfig;
import edu.usc.irds.sparkler.SparklerException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Set;


public class UrlFilterProps implements BaseConfig {
    /*******************
     * URL FILTER CONFIG
     ******************/
    @NotNull(message = "urlFilter.regexFile cannot be null")
    private File regexFile;

    /**
     * @param sparklerConfig
     * @return UrlFilterProps Object
     * @throws SparklerException if UrlFilterProps is not active
     * @apiNote This function helps in parsing UrlFilterProps object from sparkler
     * config
     */
    public static UrlFilterProps getUrlFilterProps(SparklerConfig sparklerConfig) throws SparklerException {
        if (!isUrlFilterPropsActive(sparklerConfig))
            throw new SparklerException("urlFilter is not an active plugin");
        return (UrlFilterProps) sparklerConfig.getPluginProps("urlFilter", UrlFilterProps.class);
    }

    /**
     * @return true if UrlFilterProps is valid
     * @throws SparklerException if some exception in the UrlFilterProps
     */
    public Boolean validateUrlFilterProps() throws SparklerException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UrlFilterProps>> constraintViolations = validator.validate(this);
        for (ConstraintViolation constraintViolation : constraintViolations) {
            throw new SparklerException(constraintViolation.getMessage());
        }
        return true;
    }

    /**
     * @param sparklerConfig
     * @return true if UrlFilterProps is active
     */
    public static Boolean isUrlFilterPropsActive(SparklerConfig sparklerConfig) {
        if (sparklerConfig.getActivePlugins().contains("urlFilter")) return true;
        else return false;
    }

    /*********************
     * GETTERS AND SETTERS
     ********************/
    public File getRegexFile() {
        return regexFile;
    }

    public void setRegexFile(File regexFile) {
        this.regexFile = regexFile;
    }
}
