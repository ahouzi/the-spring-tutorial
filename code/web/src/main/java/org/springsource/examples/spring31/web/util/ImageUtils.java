package org.springsource.examples.spring31.web.util;

import org.apache.commons.codec.binary.Hex;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Josh Long
 */
abstract public class ImageUtils {

    private static Map<String, MediaType> mapOfHeaderCodesToTypes = new ConcurrentHashMap<String, MediaType>();
    private static int maximumHeaderCodeLength;

    static {
        mapOfHeaderCodesToTypes.put("FFD8FFE", MediaType.IMAGE_JPEG);
        mapOfHeaderCodesToTypes.put("49492A", new MediaType("image", "tiff"));
        mapOfHeaderCodesToTypes.put("424D", new MediaType("image", "bmp"));
        mapOfHeaderCodesToTypes.put("474946", MediaType.IMAGE_GIF);
        mapOfHeaderCodesToTypes.put("89504E470D0A1A0A", MediaType.IMAGE_PNG);

        List<String> listOfHeaderCodes = new ArrayList<String>(mapOfHeaderCodesToTypes.keySet());
        Collections.sort(listOfHeaderCodes);
        maximumHeaderCodeLength = listOfHeaderCodes.get(0).length();
    }

    public static MediaType deriveImageMediaType(byte imgBytes[]) throws Throwable {
        byte[] first20Bytes = new byte[maximumHeaderCodeLength];
        System.arraycopy(imgBytes, 0, first20Bytes, 0, first20Bytes.length);
        char[] hexRepresentationOfFile = Hex.encodeHex(first20Bytes);
        String headerCode = new String(hexRepresentationOfFile).toUpperCase();
        for (String headerCodeOption : mapOfHeaderCodesToTypes.keySet())
            if (headerCode.toLowerCase().startsWith(headerCodeOption.toLowerCase()))
                return mapOfHeaderCodesToTypes.get(headerCodeOption);
        return null;
    }

  /*  public static void main(String[] args) throws Throwable {
        File desktop = new File(SystemUtils.getUserHome(), "Desktop");
        File[] file = {
                new File(desktop, "foo.png"),
                new File(desktop, "doggy.jpg"),
                new File(desktop, "doggy.gif"),
                new File(desktop, "doggy.tif"),
                new File(desktop, "doggy.bmp")
        };
        for (File f : file) {
            InputStream inputStream = new FileInputStream(f);
            byte[] bytesForImage = IOUtils.toByteArray(inputStream);
            System.out.println(StringUtils.repeat("-", 100));
            System.out.println("file:" + f.getAbsolutePath());
            System.out.println("media type " + deriveImageMediaType(bytesForImage));
        }
    }*/
}
