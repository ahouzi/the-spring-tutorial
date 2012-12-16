package org.springsource.examples.spring31.backoffice.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Code mostly taken from http://www.mkyong.com/java/how-to-resize-an-image-in-java/
 *
 * @author Josh Long
 */
public abstract class ImageResizeUtil {

    private static Logger logger = Logger.getLogger(ImageResizeUtil.class.getName());

    private static String CONVERT_COMMAND_PATH = "/usr/local/bin/convert";


    public static <C> void resizeToWidth(File file, File output, int width, C contextDataFromOriginalRequestForCorrelation) throws Throwable {
        List<String> listOfString = Arrays.asList(CONVERT_COMMAND_PATH,
                file.getAbsolutePath(),
                "-resize " + width + "x" ,
                output.getAbsolutePath());

        String totalCommand = StringUtils.join(listOfString, " ");

        if (logger.isDebugEnabled())
            logger.debug("the Image Magick command line invocation is '" + totalCommand + "'");

        Process process = Runtime.getRuntime().exec(totalCommand);
        int retCode = process.waitFor();
        assert retCode == 0 && output.exists() :
                "Something went wrong with running the 'convert'" +
                        " command. The return / exit code is " + retCode +
                        " and the full output is:" + IOUtils.toString(process.getErrorStream()) +
                        ". There should be a file at " + output.getAbsolutePath();

    }

}
