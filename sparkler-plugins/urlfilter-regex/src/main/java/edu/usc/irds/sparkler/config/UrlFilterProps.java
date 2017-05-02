package edu.usc.irds.sparkler.config;


import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.plugin.RegexURLFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Set;


public class UrlFilterProps implements BaseConfig {

    private static final Logger LOG = LoggerFactory.getLogger(UrlFilterProps.class);


    /*******************
     * URL FILTER CONFIG
     ******************/
    @NotNull(message = "regexFile cannot be null")
    private File regexFile;

    /*********************
     * GETTERS AND SETTERS
     ********************/
    public File getRegexFile() {
        return regexFile;
    }

    public void setRegexFile(File regexFile) {
        this.regexFile = regexFile;
    }

    /**
     * @param sparklerConfig
     * @return UrlFilterProps Object
     * @throws SparklerException if UrlFilterProps is not active
     * @apiNote This function helps in parsing UrlFilterProps object from sparkler
     * config
     */
    public static UrlFilterProps getUrlFilterProps(SparklerConfig sparklerConfig) throws SparklerException {
        if (!isUrlFilterPropsActive(sparklerConfig)) {
            throw new SparklerException("urlFilter is not an active plugin");
        }
        return sparklerConfig.getPluginProps(RegexURLFilter.PLUGIN_ID, UrlFilterProps.class);
    }

    /**
     * @return true if UrlFilterProps is valid
     * @throws SparklerException if some exception in the UrlFilterProps
     */
    public Boolean validateUrlFilterProps() throws SparklerException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UrlFilterProps>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            String message = "Found " + violations.size() + " constraint violations";
            LOG.error("Violations : {}", violations);
            throw new SparklerException(message);
        }
        return true;
    }

    /**
     * @param sparklerConfig
     * @return true if UrlFilterProps is active
     */
    public static Boolean isUrlFilterPropsActive(SparklerConfig sparklerConfig) {
        return sparklerConfig.getActivePlugins().contains(RegexURLFilter.PLUGIN_ID);
    }

}
