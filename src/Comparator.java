import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

public class Comparator {
    //amount of possible colors for each additive primitive color,
    //should be such that information about the color of each pixel fits into 1 byte
    private byte hashDetailing = HashDetailing.HASH_DETAILING_3.getHashDetailing();
    //amount of possible colors for each additive primitive color. Equals to hashDetailing
    private byte colorsPerChannel;
    //amount of possible color for pixel following to current hashDetailing
    private int colorsPerPixel;
    //the size of the segment of additive primitive color values,
    //the colors from which will be simplified to the corresponding simplified color
    private int channelDivision;
    //compare images will be compressed to this square before getting hash and comparing
    private int sideOfCompressedSquaredImage;

    private byte percentageOfAllowableDifference;

    public void setSideOfCompressedSquaredImage(int sideOfCompressedSquaredImage) {
        this.sideOfCompressedSquaredImage = sideOfCompressedSquaredImage;
    }

    public int getSideOfCompressedSquaredImage() {
        return sideOfCompressedSquaredImage;
    }

    public int getColorsPerPixel() {
        return colorsPerPixel;
    }
    public int getColorsPerChannel() {
        return colorsPerChannel;
    }
    public int getChannelDivision() {
        return channelDivision;
    }

    /**
     * Set the number of possible colors additive primitive color after simplification
     * @param hashDetailing
     */
    public void setHashDetailing(HashDetailing hashDetailing) {
        this.hashDetailing = hashDetailing.getHashDetailing();
        colorsPerChannel = this.hashDetailing;
        colorsPerPixel = (int) Math.pow(this.hashDetailing, 3);
        channelDivision = 256 / colorsPerChannel;
    }

    /**
     * Set percentage of allowable number of different pixels in two pictures after simplification
     * @param percentageOfAllowableDifference
     */
    public void setPercentageOfAllowableDifference(int percentageOfAllowableDifference) {
        if (percentageOfAllowableDifference < 0 || percentageOfAllowableDifference > 100) {
            throw new InvalidParameterException(String.format("%s% is not valid value", percentageOfAllowableDifference));
        }
        this.percentageOfAllowableDifference = (byte) percentageOfAllowableDifference;
    }

    public Comparator() {
        this(HashDetailing.HASH_DETAILING_3, 10, 0);
    }

    public Comparator(HashDetailing hashDetailing, int sideOfCompressedSquaredImage, int percentageOfAllowableDifference) {
        setHashDetailing(hashDetailing);
        setSideOfCompressedSquaredImage(sideOfCompressedSquaredImage);
        setPercentageOfAllowableDifference(percentageOfAllowableDifference);
    }

    /**
     * Expecting color in RGB format, where each additive primitive color stores in int format:
     * 0 <= additivePrimitiveColor <= 255
     * default RGB color stores in 3 bytes(1 byte per additive primitive color)
     * The method simplifies color to store it in 1 byte and returns simplified color.
     * Later it be used to build whole image hash
     * Algorithm taken from
     * https://mihanentalpo.me/2016/08/php-%d1%85%d1%8d%d1%88-%d0%b8%d0%b7%d0%be%d0%b1%d1%80%d0%b0%d0%b6%d0%b5%d0%bd%d0%b8%d1%8f/#comment-4134
     * @param red
     * @param green
     * @param blue
     * @return hash value as byte
     */
    private byte simplifyColor(int red, int green, int blue) {
        int simpleRed = red / channelDivision;
        int simpleGreen = green / channelDivision;
        int simpleBlue = blue / channelDivision;
        int simpleColor = simpleRed + simpleGreen * colorsPerChannel + simpleBlue * colorsPerChannel * colorsPerChannel;
        if (simpleColor >= colorsPerPixel) {
            simpleColor = colorsPerPixel - 1;
        }
        return (byte) simpleColor;
    }

    private byte[] getImageHash(ImageRGBData imageRGBData) {
        byte[] imageHash = new byte[imageRGBData.getWidth() * imageRGBData.getHeight()];
        int hashIndex = 0;
        for (int w = 0; w < imageRGBData.getWidth(); w++) {
            for (int h = 0; h < imageRGBData.getHeight(); h++) {
                imageHash[hashIndex++] = simplifyColor(imageRGBData.getRed(w, h), imageRGBData.getGreen(w, h),
                        imageRGBData.getBlue(w, h));
            }
        }
        return imageHash;
    }

    private BufferedImage resizeImage(final BufferedImage originalImage, int newWidth, int newHeight) {
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage,0, 0, newWidth, newHeight, null);
        graphics.dispose();
        return resizedImage;
    }

    /**
     * Method will return it's parameter, in case it RGB type already
     * @param image
     * @return
     */
    private BufferedImage convertImageToRGB(final BufferedImage image) {
        final BufferedImage formattedImage;
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            formattedImage = image;
        } else {
            formattedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            formattedImage.createGraphics().drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        }
        return formattedImage;
    }

    private boolean compareImagesWithAllowableDifference(final byte[] firstImageHash, final byte[] secondImageHash) {
        int allowableAmountOfBadPixels = (int) ((firstImageHash.length / 100f) * percentageOfAllowableDifference) + 1;
        int n = firstImageHash.length;
        while (n-- != 0) {
            if (firstImageHash[n] != secondImageHash[n]) {
                if (0 == --allowableAmountOfBadPixels) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean compareImagesStrict(final byte[] firstImageHash, final byte[] secondImageHash) {
        int n = firstImageHash.length;
        while (n-- != 0) {
            if (firstImageHash[n] != secondImageHash[n]) {
                return false;
            }
        }
        return true;
    }

    public boolean compareImages(final File firstImage, final File secondImage) throws IOException {
        return compareImages(ImageIO.read(firstImage), ImageIO.read(secondImage));
    }

    public boolean compareImages(final BufferedImage firstImage, final BufferedImage secondImage) {
        final ImageRGBData firstImageRGBData = new ImageRGBData(convertImageToRGB(resizeImage(firstImage,
                sideOfCompressedSquaredImage, sideOfCompressedSquaredImage)));
        final ImageRGBData secondImageRGBData = new ImageRGBData(convertImageToRGB(resizeImage(secondImage,
                sideOfCompressedSquaredImage, sideOfCompressedSquaredImage)));
        final byte[] firstImageHash = getImageHash(firstImageRGBData);
        final byte[] secondImageHash = getImageHash(secondImageRGBData);
        if (percentageOfAllowableDifference == 0) {
            return compareImagesStrict(firstImageHash, secondImageHash);
        } else {
            return compareImagesWithAllowableDifference(firstImageHash, secondImageHash);
        }
    }
}