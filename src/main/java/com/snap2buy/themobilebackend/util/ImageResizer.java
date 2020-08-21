package com.snap2buy.themobilebackend.util;//package com.snap2buy.webservice.util;
//
//import com.drew.imaging.ImageMetadataReader;
//import com.drew.imaging.ImageProcessingException;
//import com.drew.metadata.Directory;
//import com.drew.metadata.Metadata;
//import com.drew.metadata.MetadataException;
//import com.drew.metadata.Tag;
//import com.drew.metadata.exif.ExifIFD0Directory;
//import org.imgscalr.Scalr;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//
//public class ImageResizer {
//
//    public static void resize(String inputImagePath, String outputImagePath, int scaledWidth, int scaledHeight) throws IOException {
//        // reads input image
//        File inputFile = new File(inputImagePath);
//        BufferedImage inputImage = ImageIO.read(inputFile);
//
//        // creates output image
//        BufferedImage outputImage = new BufferedImage(scaledWidth,
//                scaledHeight, inputImage.getType());
//
//        // scales the input image to the output image
//        Graphics2D g2d = outputImage.createGraphics();
//        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
//        g2d.dispose();
//
//        // extracts extension of output file
//        String formatName = outputImagePath.substring(outputImagePath
//                .lastIndexOf(".") + 1);
//
//        // writes to output file
//        ImageIO.write(outputImage, formatName, new File(outputImagePath));
//    }
//
//    public static void resize(String inputImagePath, String outputImagePath, double percent) throws IOException {
//        File inputFile = new File(inputImagePath);
//        BufferedImage inputImage = ImageIO.read(inputFile);
//        int scaledWidth = (int) (inputImage.getWidth() * percent);
//        int scaledHeight = (int) (inputImage.getHeight() * percent);
//        resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
//    }
//
//    public static void fixedWidthResize(String inputImagePath, String outputImagePath, int scaledWidth) throws IOException {
//        // reads input image
//        File inputFile = new File(inputImagePath);
//        BufferedImage inputImage = ImageIO.read(inputFile);
//
//        int originalHeight=inputImage.getHeight();
//        int originalWidth=inputImage.getWidth();
//
//        int scaledHeight=(int)(((double)scaledWidth/(double)originalWidth)*(double)originalHeight);
//
//        // creates output image
//        BufferedImage outputImage = new BufferedImage(scaledWidth,
//                scaledHeight, inputImage.getType());
//
//        // scales the input image to the output image
//        Graphics2D g2d = outputImage.createGraphics();
//        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
//        g2d.dispose();
//
//        // extracts extension of output file
//        String formatName = outputImagePath.substring(outputImagePath
//                .lastIndexOf(".") + 1);
//
//        // writes to output file
//        ImageIO.write(outputImage, formatName, new File(outputImagePath));
//    }
//
//    public static void origResize(String inputImagePath, String outputImagePath) throws IOException {
//        // reads input image
//        File inputFile = new File(inputImagePath);
//        BufferedImage inputImage = ImageIO.read(inputFile);
//
//        int originalHeight=inputImage.getHeight();
//        int originalWidth=inputImage.getWidth();
//
//        System.out.println("original :\noriginalHeight="+originalHeight+"\noriginalWidth="+originalWidth);
//
//        // creates output image
//        BufferedImage outputImage = new BufferedImage(originalWidth,
//                originalHeight, inputImage.getType());
//
//        // scales the input image to the output image
//        Graphics2D g2d = outputImage.createGraphics();
//        g2d.drawImage(inputImage, 0, 0, originalWidth, originalHeight, null);
//        g2d.dispose();
//
//        // extracts extension of output file
//        String formatName = outputImagePath.substring(outputImagePath
//                .lastIndexOf(".") + 1);
//
//        // writes to output file
//        ImageIO.write(outputImage, formatName, new File(outputImagePath));
//    }
//    public static void origRotateResize(String inputImagePath, String outputImagePath) throws IOException, ImageProcessingException, MetadataException {
//        // reads input image
//        File inputFile = new File(inputImagePath);
//        BufferedImage inputImage = ImageIO.read(inputFile);
//
//        int originalHeight=inputImage.getHeight();
//        int originalWidth=inputImage.getWidth();
//        System.out.println("before Rotation: \noriginalHeight="+originalHeight+"\noriginalWidth="+originalWidth);
//        if (originalHeight<originalWidth){
//            System.out.println("image is rotated ");
//            Scalr.Rotation rotation = rotateImage(inputFile);
//            rotation.
//        }
//        System.out.println("after Rotation: \noriginalHeight="+originalHeight+"\noriginalWidth="+originalWidth);
//
//        // creates output image
//        BufferedImage outputImage = new BufferedImage(originalWidth,
//                originalHeight, inputImage.getType());
//
//        // scales the input image to the output image
//        Graphics2D g2d = outputImage.createGraphics();
//        g2d.drawImage(inputImage, 0, 0, originalWidth, originalHeight, null);
//        g2d.dispose();
//
//        // extracts extension of output file
//        String formatName = outputImagePath.substring(outputImagePath
//                .lastIndexOf(".") + 1);
//
//        // writes to output file
//        ImageIO.write(outputImage, formatName, new File(outputImagePath));
//    }
//
//    public static Scalr.Rotation rotateImage(File inputFile) throws ImageProcessingException, IOException, MetadataException {
//        Metadata metadata = ImageMetadataReader.readMetadata(inputFile);
//        for (Directory directory : metadata.getDirectories()) {
//            for (Tag tag : directory.getTags()) {
//                System.out.println(tag);
//            }
//        }
//        ExifIFD0Directory exifIFD0 = metadata.getDirectory(ExifIFD0Directory.class);
//        int orientation = exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION);
//
//        switch (orientation) {
//            case 1: // [Exif IFD0] Orientation - Top, left side (Horizontal / normal)
//                return null;
//            case 6: // [Exif IFD0] Orientation - Right side, top (Rotate 90 CW)
//                return Scalr.Rotation.CW_90;
//            case 3: // [Exif IFD0] Orientation - Bottom, right side (Rotate 180)
//                return Scalr.Rotation.CW_180;
//            case 8: // [Exif IFD0] Orientation - Left side, bottom (Rotate 270 CW)
//                return Scalr.Rotation.CW_270;
//        }
//    }
//
//    public static void main(String[] args) throws ImageProcessingException, MetadataException {
//        String filename ="f89fcb55-ccc5-4e28-89d0-afcda50eb36d";
//        String inputImagePath = "/Users/sachin/Desktop/snap/"+filename+".jpg";
//        String outputImagePath = "/Users/sachin/Desktop/snap/"+filename+"-orig.jpg";
//        String outputImagePath1 = "/Users/sachin/Desktop/snap/"+filename+"-fixed.jpg";
//        String outputImagePath2 = "/Users/sachin/Desktop/snap/"+filename+"-fixedWidth.jpg";
//        String outputImagePath3 = "/Users/sachin/Desktop/snap/"+filename+"-smaller.jpg";
//        String outputImagePath4 = "/Users/sachin/Desktop/snap/"+filename+"-bigger.jpg";
//        String outputImagePath5 = "/Users/sachin/Desktop/snap/"+filename+"-rotate.jpg";
//
//        try {
//            ImageResizer.origResize(inputImagePath, outputImagePath1);
//            ImageResizer.origRotateResize(inputImagePath, outputImagePath5);
//
//            // resize to a fixed width (not proportional)
//            int scaledWidth = 2448;
//            int scaledHeight = 3264;
//            ImageResizer.resize(inputImagePath, outputImagePath1, scaledWidth, scaledHeight);
//
//            int scaledWidth1 = 900;
//            ImageResizer.fixedWidthResize(inputImagePath, outputImagePath2, scaledWidth1);
//
////            // resize smaller by 50%
////            double percent = 0.5;
////            ImageResizer.resize(inputImagePath, outputImagePath3, percent);
////
////            // resize bigger by 50%
////            percent = 1.5;
////            ImageResizer.resize(inputImagePath, outputImagePath4, percent);
//
//
//        } catch (IOException ex) {
//            System.out.println("Error resizing the image.");
//            ex.printStackTrace();
//        }
//    }
//
//
//}