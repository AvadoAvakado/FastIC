This library allows you to compare pictures by hash, and saves your time from comparing 2 image pixel by pixel.
Of course its not so accurate as pixel by pixel comparing, but on the other hand this approach give the significant win in speed.
Also comparing by hash give you a chance to see images, which differ only in resolution and small color shift as similar.

**Hash algorithm taken from:**

https://mihanentalpo.me/2016/08/php-%D1%85%D1%8D%D1%88-%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F

**The main algorithm variables are:**
1) Side of resized image:
  Each image will be resized to sqare with this value as side, before simplifying the image colors
  For example first image is 512x512 and second is 720x1024. Each will be resized to 10x10 square(or specified by you)
  Bigger value - bigger hash's length(hash's length = height * width) - more precision
2) Hash detailing:
  Determines the amount of colors for each additive primitive color(red, green, blue)
  Can take values from 2 to 6. That allows you to simplify pixel colors in a way to store information about each color in one byte
  thereby speeding up the comparison process
  Bigger value - Bigger colors in a simplified image - more precision

**Usage example:**

```java
Comparator comp = new Comparator(hashDetailing, sideOfCompressedSquaredImage, percentageOfAllowableDifference);
boolean result = comp.compareImages(new File("firstImagePath"), new File("firstImagePath"));
```

```java
Comparator comp = new Comparator(); // default values are: hashDetailing = HASH_DETAILING_3, sideOfCompressedSquaredImage = 10, percentageOfAllowableDifference = 0
comp.setHashDetailing(HASH_DETAILING_6);
comp.setPercentageOfAllowableDifference(10); // different pixels percentage
comp.setSideOfCompressedSquaredImage(20);
boolean result = comp.compareImages(bufferedFirstImage, bufferedSecondImage);
```

To see more samples of usage visit wiki:
https://github.com/AvadoAvakado/FastIC/wiki/FastIC-samples
